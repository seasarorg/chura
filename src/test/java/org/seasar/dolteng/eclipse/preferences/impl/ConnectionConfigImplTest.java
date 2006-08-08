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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sql.XAConnection;

import junit.framework.TestCase;

import org.eclipse.jface.preference.PreferenceStore;
import org.seasar.extension.jdbc.util.ConnectionUtil;
import org.seasar.framework.util.ResourceUtil;
import org.seasar.framework.util.ResultSetUtil;

/**
 * @author taichi
 * 
 */
public class ConnectionConfigImplTest extends TestCase {

    private ConnectionConfigImpl config;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        config = new ConnectionConfigImpl(new PreferenceStore());
        config.setDriverPath(ResourceUtil.getBuildDir(
                org.hsqldb.jdbcDriver.class).getAbsolutePath());
        config.setDriverClass(org.hsqldb.jdbcDriver.class.getName());
        config.setUser("sa");
        config.setConnectionUrl("jdbc:hsqldb:file:"
                + ResourceUtil.getBuildDir("data").getAbsolutePath()
                + "/data/demo");
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
     * Test method for
     * 'org.seasar.dolteng.eclipse.preferences.impl.ConnectionConfigImpl.getXAConnection()'
     */
    public void testGetXAConnection() throws Exception {
        connect(2);
        List thds = new ArrayList();
        for (int i = 0; i < 10; i++) {
            final int num = i;
            Runnable r = new Runnable() {
                public void run() {
                    connect(10 - num);
                    connect(10 - num + 1);
                }
            };
            Thread thd = new Thread(r);
            thd.start();
            thds.add(thd);
        }
        for (Iterator i = thds.iterator(); i.hasNext();) {
            Thread t = (Thread) i.next();
            t.join();
        }
    }

    protected void connect(int i) {
        XAConnection xa = null;
        Connection c = null;
        ResultSet rs = null;
        try {
            Thread.sleep(i * 1000);
            xa = config.getXAConnection();
            c = xa.getConnection();
            DatabaseMetaData dmd = c.getMetaData();
            rs = dmd.getSchemas();
            while (rs.next()) {
                String s = rs.getString(1);
                System.out.println(Thread.currentThread().getName() + " : "
                        + System.currentTimeMillis() + " : " + s);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ResultSetUtil.close(rs);
            ConnectionUtil.close(c);
            try {
                xa.close();
            } catch (Exception e) {
            }
        }
    }
}
