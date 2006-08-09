/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package org.seasar.dolteng.eclipse.util;

import java.lang.reflect.Method;
import java.util.Map;

import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.framework.beans.BeanDesc;
import org.seasar.framework.beans.PropertyDesc;
import org.seasar.framework.beans.factory.BeanDescFactory;
import org.seasar.framework.container.cooldeploy.CoolComponentAutoRegister;
import org.seasar.framework.container.external.GenericS2ContainerInitializer;
import org.seasar.framework.container.hotdeploy.OndemandBehavior;
import org.seasar.framework.container.impl.S2ContainerBehavior;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.CaseInsensitiveMap;
import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.MethodUtil;

/**
 * @author taichi
 * 
 */
public class S2ContainerUtil {

    public static final ComponentLoader MULTI_LOADER = new MultiComponentLoader();

    public static final ComponentLoader SINGLE_LOADER = new SingleComponentLoader();

    private static final String METHOD_NAME_HAS_COMPONENT_DEF = "hasComponentDef";

    private static final String METHOD_NAME_FIND_COMPONENTS = "findComponents";

    private static final String METHOD_NAME_GET_COMPONENT = "getComponent";

    public static Map loadNamingConvensions(JavaProjectClassLoader classLoader) {
        Map result = new CaseInsensitiveMap();
        Object container = null;
        ClassLoader current = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            container = createS2Container("convention.dicon", classLoader);
            Class ncClass = classLoader.loadClass(NamingConvention.class
                    .getName());
            Object nc = loadComponent(classLoader, container, ncClass);
            BeanDesc desc = BeanDescFactory.getBeanDesc(ncClass);
            for (int i = 0; i < desc.getPropertyDescSize(); i++) {
                PropertyDesc pd = desc.getPropertyDesc(i);
                result.put(pd.getPropertyName(), pd.getValue(nc));
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        } finally {
            BeanDescFactory.clear();
            destroyS2Container(container);
            JavaProjectClassLoader.dispose(classLoader);
            Thread.currentThread().setContextClassLoader(current);
        }
        return result;
    }

    public static String loadHotdeployRootPkg(
            JavaProjectClassLoader classLoader, String path) {
        String result = "";
        Object container = null;
        try {
            container = createS2Container(path, classLoader);
            Class behaviorClass = classLoader
                    .loadClass(S2ContainerBehavior.class.getName());
            Object ondemand = MethodUtil.invoke(behaviorClass.getMethod(
                    "getProvider", null), null, null);
            Class ondemandClass = classLoader.loadClass(OndemandBehavior.class
                    .getName());
            if (ondemandClass.isAssignableFrom(ondemand.getClass())) {
                Object project = MethodUtil.invoke(ondemandClass.getMethod(
                        "getProject", new Class[] { int.class }), ondemand,
                        new Object[] { new Integer(0) });
                Object pkgName = MethodUtil.invoke(project.getClass()
                        .getMethod("getRootPackageName", null), project, null);
                if (pkgName != null) {
                    result = pkgName.toString();
                }
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        } finally {
            destroyS2Container(container);
            JavaProjectClassLoader.dispose(classLoader);
        }
        return result;
    }

    public static String loadCooldeployRootPkg(
            JavaProjectClassLoader classLoader, String path) {
        String result = "";
        Object container = null;
        try {
            container = createS2Container(path, classLoader);
            Class coolRegisterClass = classLoader
                    .loadClass(CoolComponentAutoRegister.class.getName());
            if (container != null) {
                Method getComponent = container.getClass()
                        .getMethod(METHOD_NAME_GET_COMPONENT,
                                new Class[] { Object.class });
                Object cool = getComponent.invoke(container,
                        new Object[] { coolRegisterClass });
                Object project = MethodUtil.invoke(coolRegisterClass.getMethod(
                        "getProject", new Class[] { int.class }), cool,
                        new Object[] { new Integer(0) });
                Object pkgName = MethodUtil.invoke(project.getClass()
                        .getMethod("getRootPackageName", null), project, null);
                if (pkgName != null) {
                    result = pkgName.toString();
                }

            }
        } catch (Exception e) {
            DoltengCore.log(e);
        } finally {
            destroyS2Container(container);
            JavaProjectClassLoader.dispose(classLoader);
        }
        return result;
    }

    public static Object createS2Container(String path, ClassLoader loader) {
        ClassLoader current = Thread.currentThread().getContextClassLoader();
        Object container = null;
        try {
            Thread.currentThread().setContextClassLoader(loader);
            Class initializerClass = loader
                    .loadClass(GenericS2ContainerInitializer.class.getName());
            Method setConfigPath = ClassUtil.getMethod(initializerClass,
                    "setConfigPath", new Class[] { String.class });
            Object initializer = initializerClass.newInstance();
            MethodUtil
                    .invoke(setConfigPath, initializer, new Object[] { path });
            Method initialize = initializerClass.getMethod("initialize", null);
            container = MethodUtil.invoke(initialize, initializer, null);
        } catch (Exception e) {
            DoltengCore.log(e);
        } finally {
            Thread.currentThread().setContextClassLoader(current);
        }
        return container;
    }

    public static Object[] loadComponents(ClassLoader classloader,
            Object container, Object key) {
        return (Object[]) loadComponent(classloader, container, key,
                MULTI_LOADER);
    }

    public static Object loadComponent(ClassLoader classloader,
            Object container, Object key) {
        return loadComponent(classloader, container, key, SINGLE_LOADER);
    }

    private static Object loadComponent(ClassLoader classloader,
            Object container, Object key, ComponentLoader componentLoader) {
        Object object = null;
        try {
            if (container != null) {
                Method hasComponentDef = ClassUtil.getMethod(container
                        .getClass(), METHOD_NAME_HAS_COMPONENT_DEF,
                        new Class[] { Object.class });
                Object is = MethodUtil.invoke(hasComponentDef, container,
                        new Object[] { key });
                if (((Boolean) is).booleanValue()) {
                    object = componentLoader.loadComponent(container, key);
                }
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        }
        return object;
    }

    public static void destroyS2Container(Object container) {
        if (container != null) {
            Method m = ClassUtil.getMethod(container.getClass(), "destroy",
                    null);
            MethodUtil.invoke(m, container, null);
        }
    }

    public interface ComponentLoader {
        Object loadComponent(Object container, Object key) throws Exception;
    }

    public static class SingleComponentLoader implements ComponentLoader {
        public Object loadComponent(Object container, Object key)
                throws Exception {
            Method getComponent = container.getClass().getMethod(
                    METHOD_NAME_GET_COMPONENT, new Class[] { Object.class });
            return getComponent.invoke(container, new Object[] { key });
        }
    }

    public static class MultiComponentLoader implements ComponentLoader {
        public Object loadComponent(Object container, Object key)
                throws Exception {
            Method getComponent = container.getClass().getMethod(
                    METHOD_NAME_FIND_COMPONENTS, new Class[] { Object.class });
            return getComponent.invoke(container, new Object[] { key });
        }
    }

}
