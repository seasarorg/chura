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
import java.lang.reflect.Method;

import org.eclipse.jdt.core.IJavaProject;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.framework.util.ResourceUtil;

/**
 * @author taichi
 * 
 */
public class S2ContainerUtil {

	public static final ComponentLoader MULTI_LOADER = new MultiComponentLoader();

	public static final ComponentLoader SINGLE_LOADER = new SingleComponentLoader();

	private static final String CLASS_NAME_FACTORY = "org.seasar.framework.container.factory.S2ContainerFactory";

	private static final String METHOD_NAME_CREATE = "create";

	private static final String METHOD_NAME_INIT = "init";

	private static final String METHOD_NAME_HAS_COMPONENT_DEF = "hasComponentDef";

	private static final String METHOD_NAME_FIND_COMPONENTS = "findComponents";

	private static final String METHOD_NAME_GET_COMPONENT = "getComponent";

	public static Object createS2Container(String path, ClassLoader loader)
			throws Exception {
		Object container = null;
		Class factoryClass = loader.loadClass(CLASS_NAME_FACTORY);
		Method create = factoryClass.getMethod(METHOD_NAME_CREATE, new Class[] {
				String.class, ClassLoader.class });
		container = create.invoke(null, new Object[] { path, loader });
		Class containerClass = container.getClass();
		Method init = containerClass.getMethod(METHOD_NAME_INIT, null);
		init.invoke(container, null);
		return container;
	}

	public static Object[] loadComponents(IJavaProject project,
			String diconPath, Object key) {
		return (Object[]) loadComponent(project, diconPath, key, MULTI_LOADER);
	}

	public static Object loadComponent(IJavaProject project, String diconPath,
			Object key) {
		return loadComponent(project, diconPath, key, SINGLE_LOADER);
	}

	public static Object loadComponent(IJavaProject project, String diconPath,
			Object key, ComponentLoader componentLoader) {
		ClassLoader current = Thread.currentThread().getContextClassLoader();
		Object object = null;
		try {
			JavaProjectClassLoader classloader = new JavaProjectClassLoader(
					project);
			Thread.currentThread().setContextClassLoader(classloader);
			File f = ResourceUtil.getResourceAsFileNoException(diconPath);
			if (f != null) {
				Object container = createS2Container(diconPath, classloader);
				Class containerClass = container.getClass();
				Method hasComponentDef = containerClass.getMethod(
						METHOD_NAME_HAS_COMPONENT_DEF,
						new Class[] { Object.class });
				Object is = hasComponentDef.invoke(container,
						new Object[] { key });
				if (((Boolean) is).booleanValue()) {
					object = componentLoader.loadComponent(container, key);
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
