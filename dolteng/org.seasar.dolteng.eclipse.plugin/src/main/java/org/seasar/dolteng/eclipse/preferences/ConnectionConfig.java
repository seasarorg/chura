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
package org.seasar.dolteng.eclipse.preferences;

import org.eclipse.jface.preference.IPersistentPreferenceStore;

/**
 * @author taichi
 * 
 */
public interface ConnectionConfig {

	public IPersistentPreferenceStore toPreferenceStore();

	/**
	 * @return Returns the charSet.
	 */
	public String getCharset();

	/**
	 * @param charSet
	 *            The charSet to set.
	 */
	public void setCharset(String charSet);

	/**
	 * @return Returns the connectionUrl.
	 */
	public String getConnectionUrl();

	/**
	 * @param connectionUrl
	 *            The connectionUrl to set.
	 */
	public void setConnectionUrl(String connectionUrl);

	/**
	 * @return Returns the driverClass.
	 */
	public String getDriverClass();

	/**
	 * @param driverClass
	 *            The driverClass to set.
	 */
	public void setDriverClass(String driverClass);

	/**
	 * @return Returns the driverPath.
	 */
	public String getDriverPath();

	/**
	 * @param driverPath
	 *            The driverPath to set.
	 */
	public void setDriverPath(String driverPath);

	/**
	 * @return Returns the name.
	 */
	public String getName();

	/**
	 * @param name
	 *            The name to set.
	 */
	public void setName(String name);

	/**
	 * @return Returns the pass.
	 */
	public String getPass();

	/**
	 * @param pass
	 *            The pass to set.
	 */
	public void setPass(String pass);

	/**
	 * @return Returns the user.
	 */
	public String getUser();

	/**
	 * @param user
	 *            The user to set.
	 */
	public void setUser(String user);

	public String[] getTableTypes();

}