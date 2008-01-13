/*
 * Copyright 2004-2008 the Seasar Foundation and the Others.
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

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.XAConnection;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.preferences.ConnectionConfig;
import org.seasar.dolteng.eclipse.util.JavaProjectClassLoader;
import org.seasar.extension.dbcp.impl.XAConnectionImpl;
import org.seasar.framework.util.DisposableUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class ConnectionConfigImpl implements ConnectionConfig {

    private IPersistentPreferenceStore store;

    public ConnectionConfigImpl() {
        this.store = new ScopedPreferenceStore(new InstanceScope(),
                Constants.ID_PLUGIN);
    }

    public ConnectionConfigImpl(IPersistentPreferenceStore store) {
        this.store = store;
    }

    public Properties toProperties(String user, String pass) {
        Properties prop = new Properties();
        prop.setProperty("user", user);
        prop.setProperty("password", pass);
        prop.setProperty("charSet", getCharset());
        prop.setProperty("characterEncoding", getCharset());
        prop.setProperty("useUnicode", "true");
        return prop;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.ConnectionConfig#toPreferenceStore()
     */
    public IPersistentPreferenceStore toPreferenceStore() {
        return this.store;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.configs.impl.ConnectionConfig#getCharset()
     */
    public String getCharset() {
        return this.store.getString(Constants.PREF_CHARSET);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.configs.impl.ConnectionConfig#setCharset(java.lang.String)
     */
    public void setCharset(String charSet) {
        store.setValue(Constants.PREF_CHARSET, charSet);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.configs.impl.ConnectionConfig#getConnectionUrl()
     */
    public String getConnectionUrl() {
        return store.getString(Constants.PREF_CONNECTION_URL);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.configs.impl.ConnectionConfig#setConnectionUrl(java.lang.String)
     */
    public void setConnectionUrl(String connectionUrl) {
        store.setValue(Constants.PREF_CONNECTION_URL, connectionUrl);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.configs.impl.ConnectionConfig#getDriverClass()
     */
    public String getDriverClass() {
        return store.getString(Constants.PREF_DRIVER_CLASS);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.configs.impl.ConnectionConfig#setDriverClass(java.lang.String)
     */
    public void setDriverClass(String driverClass) {
        if (StringUtil.isEmpty(driverClass) == false) {
            store.setValue(Constants.PREF_DRIVER_CLASS, driverClass);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.ConnectionConfig#getDriverPaths()
     */
    public String[] getDriverPaths() {
        String s = store.getString(Constants.PREF_DRIVER_PATH);
        if (StringUtil.isEmpty(s) == false) {
            return s.split("\\|");
        }
        return StringUtil.EMPTY_STRINGS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.ConnectionConfig#setDriverPaths(java.lang.String[])
     */
    public void setDriverPaths(String[] driverPaths) {
        StringBuffer stb = new StringBuffer();
        for (String driverPath : driverPaths) {
            stb.append(driverPath);
            stb.append('|');
        }
        store.setValue(Constants.PREF_DRIVER_PATH, stb.toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.configs.impl.ConnectionConfig#getName()
     */
    public String getName() {
        return store.getString(Constants.PREF_CONNECTION_NAME);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.configs.impl.ConnectionConfig#setName(java.lang.String)
     */
    public void setName(String name) {
        store.setValue(Constants.PREF_CONNECTION_NAME, name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.configs.impl.ConnectionConfig#getPass()
     */
    public String getPass() {
        return store.getString(Constants.PREF_PASS);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.configs.impl.ConnectionConfig#setPass(java.lang.String)
     */
    public void setPass(String pass) {
        store.setValue(Constants.PREF_PASS, pass);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.configs.impl.ConnectionConfig#getUser()
     */
    public String getUser() {
        return store.getString(Constants.PREF_USER);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.configs.impl.ConnectionConfig#setUser(java.lang.String)
     */
    public void setUser(String user) {
        store.setValue(Constants.PREF_USER, user);
    }

    public static final String[] TABLE_TYPES = new String[] { "TABLE", "VIEW",
            "ALIAS", "SYNONYM", "SYSTEM TABLE" };

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.configs.impl.ConnectionConfig#getTableTypes()
     */
    public String[] getTableTypes() {
        return null; // FIXME : 設定可能にする<del>か、DatabaseMetaDataから取ってくる</del>。
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.sql.XADataSource#getLoginTimeout()
     */
    public int getLoginTimeout() {
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.sql.XADataSource#setLoginTimeout(int)
     */
    public void setLoginTimeout(int seconds) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.sql.XADataSource#getLogWriter()
     */
    public PrintWriter getLogWriter() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.sql.XADataSource#setLogWriter(java.io.PrintWriter)
     */
    public void setLogWriter(PrintWriter out) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.sql.XADataSource#getXAConnection()
     */
    public XAConnection getXAConnection() throws SQLException {
        return this.getXAConnection(getUser(), getPass());
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.sql.XADataSource#getXAConnection(java.lang.String,
     *      java.lang.String)
     */
    public XAConnection getXAConnection(String user, String password)
            throws SQLException {
        Properties p = toProperties(user, password);
        Driver driver = null;
        ClassLoader loader = null;
        try {
            loader = createClassLoader();
            Class clazz = loader.loadClass(getDriverClass());
            driver = (Driver) clazz.newInstance();
            Connection con = driver.connect(getConnectionUrl(), p);
            return new XAConnectionImpl(con);
        } catch (SQLException e) {
            DoltengCore.log(e);
            throw e;
        } catch (Exception e) {
            DoltengCore.log(e);
            throw new IllegalStateException();
        } finally {
            JavaProjectClassLoader.dispose(loader);
            DisposableUtil.deregisterAllDrivers();
        }
    }

    protected ClassLoader createClassLoader() throws Exception {
        String[] path = getDriverPaths();
        List<URL> urls = new ArrayList<URL>(path.length);
        for (String p : path) {
            File f = new File(p);
            try {
                urls.add(f.toURI().toURL());
            } catch (Exception e) {
                DoltengCore.log(e);
            }
        }
        // HSQLDBのHSQLDB Timerを殺す方法が見つかるまでは、HSQLDBを使用すると、
        // コンテキストクラスローダーからクラスがロードされる事により、プロジェクトを削除出来る。
        // 要はHSQLDBを使う時には、Doltengが抱えているHSQLDBが動作する事になる。
        return new URLClassLoader(urls.toArray(new URL[urls.size()]),
                Thread.currentThread().getContextClassLoader());
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.sql.DataSource#getConnection()
     */
    public Connection getConnection() throws SQLException {
        return getXAConnection().getConnection();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.sql.DataSource#getConnection(java.lang.String,
     *      java.lang.String)
     */
    public Connection getConnection(String username, String password)
            throws SQLException {
        return getXAConnection(username, password).getConnection();
    }

}
