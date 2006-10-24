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

import java.io.BufferedInputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import jp.aonir.fuzzyxml.FuzzyXMLAttribute;
import jp.aonir.fuzzyxml.FuzzyXMLDocument;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLNode;
import jp.aonir.fuzzyxml.FuzzyXMLParser;
import jp.aonir.fuzzyxml.FuzzyXMLText;
import jp.aonir.fuzzyxml.XPath;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.seasar.dolteng.core.convention.NamingConventionMirror;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.framework.container.external.GenericS2ContainerInitializer;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.MethodUtil;

/**
 * @author taichi
 * 
 */
public class S2ContainerUtil {

    public static final ComponentLoader MULTI_LOADER = new MultiComponentLoader();

    public static final ComponentLoader SINGLE_LOADER = new SingleComponentLoader();

    public static NamingConvention loadNamingConvensions(IProject project) {
        NamingConvention result = loadByXPath(project);
        if (result == null) {
            result = loadFromClassLoader(project);
        }

        if (result == null) {
            // TODO projectにエラーマーカー
        }

        return result;
    }

    private static NamingConvention loadByXPath(IProject project) {
        NamingConvention result = null;
        IFile f = ResourcesUtil.findFile("convention.dicon", project);
        if (f != null) {
            result = processXml(f);
        }
        return result;
    }

    public static NamingConvention processXml(IFile file) {
        NamingConventionMirror result = null;
        try {
            FuzzyXMLParser parser = new FuzzyXMLParser();
            FuzzyXMLDocument doc = parser.parse(new BufferedInputStream(file
                    .getContents()));

            // サフィックスのネーミングルール。
            result = processProperties(doc);

            // ルートパッケージ名
            result = processRootPackageNames(doc, result);
        } catch (Exception e) {
            DoltengCore.log(e);
        }
        return result;
    }

    private static NamingConventionMirror processProperties(FuzzyXMLDocument doc)
            throws CoreException {
        Map props = new HashMap();

        FuzzyXMLNode[] list = XPath.selectNodes(doc.getDocumentElement(),
                "//property[@name]");
        for (int i = 0; i < list.length; i++) {
            FuzzyXMLNode node = list[i];
            if ((node instanceof FuzzyXMLElement) == false) {
                continue;
            }
            FuzzyXMLElement elem = (FuzzyXMLElement) node;
            FuzzyXMLAttribute attr = elem.getAttributeNode("name");
            if (attr != null
                    && NamingConventionMirror.DEFAULT_VALUES.containsKey(attr
                            .getValue()) && elem.hasChildren()) {
                String s = FuzzyXMLUtil.getChildText(elem);
                if (isError(JavaConventions.validateJavaTypeName(s))) {
                    return null;
                }
                props.put(attr.getValue(), s);
            }
        }
        return new NamingConventionMirror(props);
    }

    private static NamingConventionMirror processRootPackageNames(
            FuzzyXMLDocument doc, NamingConventionMirror mirror)
            throws CoreException {
        FuzzyXMLNode[] list = XPath
                .selectNodes(doc.getDocumentElement(),
                        "//component/initMethod[@name=\"addRootPackageName\"]/arg/text()");

        for (int i = 0; mirror != null && i < list.length; i++) {
            FuzzyXMLText n = (FuzzyXMLText) list[i];
            String s = n.getValue().replaceAll("\"", "");
            if (isError(JavaConventions.validatePackageName(s))) {
                return null;
            }
            mirror.addRootPackageName(s);
        }
        return mirror;
    }

    private static boolean isError(IStatus status) {
        return status.getSeverity() == IStatus.ERROR
                || status.getSeverity() == IStatus.WARNING;
    }

    private static NamingConvention loadFromClassLoader(IProject project) {
        JavaProjectClassLoader classLoader = new JavaProjectClassLoader(
                JavaCore.create(project));
        NamingConvention result = null;
        Object container = null;
        ClassLoader current = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            container = createS2Container("convention.dicon", classLoader);
            Class ncClass = classLoader.loadClass(NamingConvention.class
                    .getName());
            Object nc = loadComponent(classLoader, container, ncClass);
            result = new NamingConventionMirror(ncClass, nc);
        } catch (Exception e) {
            DoltengCore.log(e);
        } catch (ClassFormatError e) {
            DoltengCore.log(e);
        } finally {
            destroyS2Container(container);
            JavaProjectClassLoader.dispose(classLoader);
            Thread.currentThread().setContextClassLoader(current);
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
                        .getClass(), "hasComponentDef",
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
                    "getComponent", new Class[] { Object.class });
            return getComponent.invoke(container, new Object[] { key });
        }
    }

    public static class MultiComponentLoader implements ComponentLoader {
        public Object loadComponent(Object container, Object key)
                throws Exception {
            Method getComponent = container.getClass().getMethod(
                    "findComponents", new Class[] { Object.class });
            return getComponent.invoke(container, new Object[] { key });
        }
    }

}
