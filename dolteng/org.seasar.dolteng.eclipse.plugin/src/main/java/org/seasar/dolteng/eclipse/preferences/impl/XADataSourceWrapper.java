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

package org.seasar.dolteng.eclipse.preferences.impl;

import java.io.PrintWriter;
import java.sql.SQLException;

import javax.sql.XAConnection;
import javax.sql.XADataSource;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * @author taichi
 * 
 */
public class XADataSourceWrapper extends ConnectionConfigImpl {

	private XADataSource dataSource;

	/**
	 * @param store
	 */
	public XADataSourceWrapper(String name, XADataSource dataSource) {
		super(new ScopedPreferenceStore(new InstanceScope(), ""));
		setName(name);
		this.dataSource = dataSource;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.seasar.dolteng.eclipse.preferences.impl.ConnectionConfigImpl#getCharset()
	 */
	public String getCharset() {
		return super.getCharset();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.seasar.dolteng.eclipse.preferences.impl.ConnectionConfigImpl#getConnectionUrl()
	 */
	public String getConnectionUrl() {
		return super.getConnectionUrl();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.seasar.dolteng.eclipse.preferences.impl.ConnectionConfigImpl#getDriverClass()
	 */
	public String getDriverClass() {
		return super.getDriverClass();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.seasar.dolteng.eclipse.preferences.impl.ConnectionConfigImpl#getDriverPath()
	 */
	public String getDriverPath() {
		return super.getDriverPath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.seasar.dolteng.eclipse.preferences.impl.ConnectionConfigImpl#getPass()
	 */
	public String getPass() {
		return super.getPass();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.seasar.dolteng.eclipse.preferences.impl.ConnectionConfigImpl#getUser()
	 */
	public String getUser() {
		return super.getUser();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.XADataSource#getLoginTimeout()
	 */
	public int getLoginTimeout() throws SQLException {
		return dataSource.getLoginTimeout();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.XADataSource#getLogWriter()
	 */
	public PrintWriter getLogWriter() throws SQLException {
		return dataSource.getLogWriter();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.XADataSource#getXAConnection()
	 */
	public XAConnection getXAConnection() throws SQLException {
		return dataSource.getXAConnection();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.XADataSource#getXAConnection(java.lang.String,
	 *      java.lang.String)
	 */
	public XAConnection getXAConnection(String user, String password)
			throws SQLException {
		return dataSource.getXAConnection(user, password);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.XADataSource#setLoginTimeout(int)
	 */
	public void setLoginTimeout(int seconds) throws SQLException {
		dataSource.setLoginTimeout(seconds);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sql.XADataSource#setLogWriter(java.io.PrintWriter)
	 */
	public void setLogWriter(PrintWriter out) throws SQLException {
		dataSource.setLogWriter(out);
	}

}
