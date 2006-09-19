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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.seasar.dolteng.core.convention.NamingConventionMirror;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.framework.container.external.GenericS2ContainerInitializer;
import org.seasar.framework.container.external.servlet.HttpServletExternalContext;
import org.seasar.framework.container.external.servlet.HttpServletExternalContextComponentDefRegister;
import org.seasar.framework.container.factory.SingletonS2ContainerFactory;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.mock.servlet.MockHttpServletRequestImpl;
import org.seasar.framework.mock.servlet.MockHttpServletResponseImpl;
import org.seasar.framework.mock.servlet.MockServletContext;
import org.seasar.framework.mock.servlet.MockServletContextImpl;
import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.DocumentBuilderFactoryUtil;
import org.seasar.framework.util.MethodUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
            DocumentBuilder builder = DocumentBuilderFactoryUtil
                    .newDocumentBuilder();
            builder.setEntityResolver(new ClassLoaderEntityResolver());
            Document doc = builder.parse(new BufferedInputStream(file
                    .getContents()));
            // FIXME fuzzyXMLを使う様にする。
            XPath xpath = XPathFactory.newInstance().newXPath();

            // サフィックスのネーミングルール。
            result = processProperties(doc, xpath);

            // ルートパッケージ名
            result = processRootPackageNames(doc, xpath, result);
        } catch (Exception e) {
            DoltengCore.log(e);
        }
        return result;
    }

    private static NamingConventionMirror processProperties(Document doc,
            XPath xpath) throws XPathExpressionException, CoreException {
        Map props = new HashMap();
        NodeList list = (NodeList) xpath.evaluate("//property[@name]", doc,
                XPathConstants.NODESET);
        for (int i = 0; list != null && i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element elem = (Element) node;
            Attr attr = elem.getAttributeNode("name");
            if (attr != null
                    && NamingConventionMirror.DEFAULT_VALUES.containsKey(attr
                            .getValue()) && elem.hasChildNodes()) {
                NodeList children = node.getChildNodes();
                if (children.getLength() == 1) {
                    Node n = children.item(0);
                    if (n.getNodeType() == Node.TEXT_NODE) {
                        String s = n.getNodeValue().replaceAll("\"", "");
                        if (isError(JavaConventions.validateJavaTypeName(s))) {
                            return null;
                        }
                        props.put(attr.getValue(), s);
                    }
                }
            }
        }
        return new NamingConventionMirror(props);
    }

    private static NamingConventionMirror processRootPackageNames(Document doc,
            XPath xpath, NamingConventionMirror mirror)
            throws XPathExpressionException, CoreException {
        NodeList list = (NodeList) xpath
                .evaluate(
                        "//component/initMethod[@name=\"addRootPackageName\"]/arg/text()",
                        doc, XPathConstants.NODESET);
        for (int i = 0; mirror != null && i < list.getLength(); i++) {
            Node n = list.item(i);
            String s = n.getNodeValue().replaceAll("\"", "");
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

    public static void initializeSingletonTeeda() {
        try {
            HttpServletExternalContext context = new HttpServletExternalContext();
            MockServletContext sc = new MockServletContextImpl("/dolteng");
            context.setApplication(sc);

            MockHttpServletRequestImpl request = sc
                    .createRequest("/index.html");
            context.setRequest(request);
            context.setResponse(new MockHttpServletResponseImpl(request));

            HttpServletExternalContextComponentDefRegister register = new HttpServletExternalContextComponentDefRegister();
            GenericS2ContainerInitializer initializer = new GenericS2ContainerInitializer(
                    context, register);
            initializer.setConfigPath("dolteng-teedaExtension.dicon");
            initializer.initialize();
        } catch (Exception e) {
            DoltengCore.log(e);
        }
    }

    public static void destroySingletonTeeda() {
        SingletonS2ContainerFactory.destroy();
    }
}
