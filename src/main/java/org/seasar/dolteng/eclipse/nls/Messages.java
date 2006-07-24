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
package org.seasar.dolteng.eclipse.nls;

import org.eclipse.osgi.util.NLS;

/**
 * @author taichi
 * 
 */
public class Messages extends NLS {

	public static String SELECT_PROJECT;

	public static String JDBC_DRIVER_FINDING;

	public static String JDBC_DRIVER_FINDING_CANCELLED;

	public static String CONNECTION_TEST_SUCCEED;

	public static String CONNECTION_TEST_FAILED;

	public static String UNSUPPORTED_ENCODING;

	public static String FILE_NOT_FOUND;

	public static String DRIVER_CLASS_NOT_FOUND;

	public static String PROJECT_NOT_FOUND;

	public static String NAME_IS_EMPTY;

	public static String CONNECTION_URL_EMPTY;

	public static String PLUGIN_INITIALIZING;

	public static String JDBC_DICON_LOADING;

	static {
		Class clazz = Messages.class;
		NLS.initializeMessages(clazz.getName(), clazz);
	}
}
