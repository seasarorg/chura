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

import java.io.File;
import java.lang.reflect.Method;

import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.MethodUtil;
import org.seasar.framework.util.ResourceUtil;

/**
 * @author taichi
 * 
 */
public class ReflectiveConnectionConfig extends ConnectionConfigImpl {

    private String name;

    private String connectionUrl;

    private String driverClass;

    private String driverPath;

    private String user;

    private String pass;

    /**
     * 
     */
    public ReflectiveConnectionConfig(Object xadsImpl) throws Exception {
        super();
        parse(xadsImpl);
    }

    protected void parse(Object xadsImpl) throws Exception {
        Class clazz = xadsImpl.getClass();
        Method m = ClassUtil.getMethod(clazz, "getURL", null);
        this.connectionUrl = (String) MethodUtil.invoke(m, xadsImpl, null);
        m = ClassUtil.getMethod(clazz, "getDriverClassName", null);
        this.driverClass = (String) MethodUtil.invoke(m, xadsImpl, null);
        m = ClassUtil.getMethod(clazz, "getUser", null);
        this.user = (String) MethodUtil.invoke(m, xadsImpl, null);
        m = ClassUtil.getMethod(clazz, "getPassword", null);
        this.pass = (String) MethodUtil.invoke(m, xadsImpl, null);

        ClassLoader current = Thread.currentThread().getContextClassLoader();
        ClassLoader loader = clazz.getClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(loader);
            Class driver = loader.loadClass(this.driverClass);
            File f = ResourceUtil.getBuildDir(driver);
            this.driverPath = f.getAbsolutePath();
        } finally {
            Thread.currentThread().setContextClassLoader(current);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.ConnectionConfig#getCharset()
     */
    public String getCharset() {
        return "UTF-8";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.ConnectionConfig#setCharset(java.lang.String)
     */
    public void setCharset(String charSet) {
        // n/a
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.ConnectionConfig#getConnectionUrl()
     */
    public String getConnectionUrl() {
        return connectionUrl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.ConnectionConfig#setConnectionUrl(java.lang.String)
     */
    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.ConnectionConfig#getDriverClass()
     */
    public String getDriverClass() {
        return driverClass;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.ConnectionConfig#setDriverClass(java.lang.String)
     */
    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.ConnectionConfig#getDriverPath()
     */
    public String getDriverPath() {
        return driverPath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.ConnectionConfig#setDriverPath(java.lang.String)
     */
    public void setDriverPath(String driverPath) {
        this.driverPath = driverPath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.ConnectionConfig#getName()
     */
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.ConnectionConfig#setName(java.lang.String)
     */
    public void setName(String name) {
        this.name = name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.ConnectionConfig#getPass()
     */
    public String getPass() {
        return pass;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.ConnectionConfig#setPass(java.lang.String)
     */
    public void setPass(String pass) {
        this.pass = pass;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.ConnectionConfig#getUser()
     */
    public String getUser() {
        return user;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.preferences.ConnectionConfig#setUser(java.lang.String)
     */
    public void setUser(String user) {
        this.user = user;
    }

}
