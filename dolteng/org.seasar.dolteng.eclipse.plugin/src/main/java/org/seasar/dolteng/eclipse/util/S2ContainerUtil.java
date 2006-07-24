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

import org.eclipse.jdt.core.IJavaProject;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory;
import org.seasar.framework.util.ResourceUtil;

/**
 * @author taichi
 * 
 */
public class S2ContainerUtil {

	// public static final ComponentLoader MULTI_LOADER = new
	// MultiComponentLoader();
	//
	// public static final ComponentLoader SINGLE_LOADER = new
	// SingleComponentLoader();

	public static Object[] loadComponents(IJavaProject project,
			String diconPath, Object key) {
		ClassLoader current = Thread.currentThread().getContextClassLoader();
		Object[] objects = null;
		try {
			JavaProjectClassLoader classloader = new JavaProjectClassLoader(
					project);
			Thread.currentThread().setContextClassLoader(classloader);
			File f = ResourceUtil.getResourceAsFileNoException(diconPath);
			if (f != null) {
				S2Container container = S2ContainerFactory.create(diconPath,
						classloader);
				// container.init();
				if (container.hasComponentDef(key)) {
					objects = container.findComponents(key);
				}
			}
		} catch (Exception e) {
			DoltengCore.log(e);
		} catch (Error e) {
			DoltengCore.log(e);
		} finally {
			Thread.currentThread().setContextClassLoader(current);
		}
		return objects;
	}

	public static Object loadComponent(IJavaProject project, String diconPath,
			Object key) {
		ClassLoader current = Thread.currentThread().getContextClassLoader();
		Object object = null;
		try {
			JavaProjectClassLoader classloader = new JavaProjectClassLoader(
					project);
			Thread.currentThread().setContextClassLoader(classloader);
			File f = ResourceUtil.getResourceAsFileNoException(diconPath);
			if (f != null) {
				S2Container container = S2ContainerFactory.create(diconPath);
				// container.init();
				if (container.hasComponentDef(key)) {
					object = container.getComponent(key);
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

	// public static Object loadComponent(IJavaProject project, String
	// diconPath,
	// Object key, ComponentLoader loader) {
	// ClassLoader ctx = Thread.currentThread().getContextClassLoader();
	// Object object = null;
	// try {
	// JavaProjectClassLoader classloader = new JavaProjectClassLoader(
	// project);
	// Thread.currentThread().setContextClassLoader(classloader);
	// File f = ResourceUtil.getResourceAsFileNoException(diconPath);
	// if (f != null) {
	// S2Container container = S2ContainerFactory.create(diconPath,
	// classloader);
	// // container.init();
	// if (container.hasComponentDef(key)) {
	// object = loader.loadComponent(container, key);
	// }
	// }
	// } catch (Exception e) {
	// DoltengCore.log(e);
	// } catch (Error e) {
	// DoltengCore.log(e);
	// } finally {
	// Thread.currentThread().setContextClassLoader(ctx);
	// }
	// return object;
	// }

	// public interface ComponentLoader {
	// Object loadComponent(S2Container container, Object key);
	// }
	//
	// public static class SingleComponentLoader implements ComponentLoader {
	// public Object loadComponent(S2Container container, Object key) {
	// return container.getComponent(key);
	// }
	// }
	//
	// public static class MultiComponentLoader implements ComponentLoader {
	// public Object loadComponent(S2Container container, Object key) {
	// return container.findComponents(key);
	// }
	// }
}
