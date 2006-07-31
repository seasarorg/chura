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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
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
import org.seasar.framework.util.MethodUtil;
import org.seasar.framework.util.ResourceUtil;

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

    public static Map loadNamingConvensions(ClassLoader classLoader) {
        Map result = new CaseInsensitiveMap();
        try {
            Class ncClass = classLoader.loadClass(NamingConvention.class
                    .getName());
            Object nc = loadComponent(classLoader, "convention.dicon", ncClass);
            convert(BeanDescFactory.getBeanDesc(ncClass), nc, result);
        } catch (Exception e) {
            DoltengCore.log(e);
        }
        return result;
    }

    private static void convert(BeanDesc desc, Object o, Map m) {
        for (int i = 0; i < desc.getPropertyDescSize(); i++) {
            PropertyDesc pd = desc.getPropertyDesc(i);
            m.put(pd.getPropertyName(), pd.getValue(o));
        }
    }

    public static String loadHotdeployRootPkg(ClassLoader classLoader,
            String path) {
        String result = "";
        try {
            createS2Container(path, classLoader);
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
        }
        return result;
    }

    public static String loadCooldeployRootPkg(ClassLoader classLoader,
            String path) {
        String result = "";
        try {
            Object container = createS2Container(path, classLoader);
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
        }
        return result;
    }

    public static Object createS2Container(String path, ClassLoader loader) {
        Object container = null;
        try {
            Class initializerClass = loader
                    .loadClass(GenericS2ContainerInitializer.class.getName());
            Method setConfigPath = initializerClass.getMethod("setConfigPath",
                    new Class[] { String.class });
            Object initializer = initializerClass.newInstance();
            setConfigPath.invoke(initializer, new Object[] { path });
            Method initialize = initializerClass.getMethod("initialize", null);
            container = initialize.invoke(initializer, null);
        } catch (InvocationTargetException e) {
            DoltengCore.log(e.getTargetException());
        } catch (Exception e) {
            DoltengCore.log(e);
        }
        return container;
    }

    public static Object[] loadComponents(ClassLoader classloader,
            String diconPath, Object key) {
        return (Object[]) loadComponent(classloader, diconPath, key,
                MULTI_LOADER);
    }

    public static Object loadComponent(ClassLoader classloader,
            String diconPath, Object key) {
        return loadComponent(classloader, diconPath, key, SINGLE_LOADER);
    }

    public static Object loadComponent(ClassLoader classloader,
            String diconPath, Object key, ComponentLoader componentLoader) {
        ClassLoader current = Thread.currentThread().getContextClassLoader();
        Object object = null;
        try {
            Thread.currentThread().setContextClassLoader(classloader);
            File f = ResourceUtil.getResourceAsFileNoException(diconPath);
            if (f != null) {
                Object container = createS2Container(diconPath, classloader);
                if (container != null) {
                    Method hasComponentDef = container.getClass().getMethod(
                            METHOD_NAME_HAS_COMPONENT_DEF,
                            new Class[] { Object.class });
                    Object is = hasComponentDef.invoke(container,
                            new Object[] { key });
                    if (((Boolean) is).booleanValue()) {
                        object = componentLoader.loadComponent(container, key);
                    }
                }
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        } catch (Error e) {
            DoltengCore.log(e);
        } finally {
            Thread.currentThread().setContextClassLoader(current);
        }
        return object;
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
