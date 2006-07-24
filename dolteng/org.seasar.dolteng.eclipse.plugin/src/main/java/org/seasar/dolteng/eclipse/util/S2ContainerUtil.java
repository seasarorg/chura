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

import javax.sql.XADataSource;

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

	/**
	 * 
	 */
	public S2ContainerUtil() {
		super();
	}

	public static Object[] loadComponents(IJavaProject project,
			String diconPath, Object key) {
		ClassLoader ctx = Thread.currentThread().getContextClassLoader();
		Object[] objects = null;
		try {
			JavaProjectClassLoader loader = new JavaProjectClassLoader(project);
			Thread.currentThread().setContextClassLoader(loader);
			File f = ResourceUtil.getResourceAsFileNoException(diconPath);
			if (f != null) {
				S2Container container = S2ContainerFactory.create(diconPath);
				if (container.hasComponentDef(XADataSource.class)) {
					objects = container.findComponents(key);
				}
			}
		} catch (Exception e) {
			DoltengCore.log(e);
		} finally {
			Thread.currentThread().setContextClassLoader(ctx);
		}
		return objects;
	}
}
