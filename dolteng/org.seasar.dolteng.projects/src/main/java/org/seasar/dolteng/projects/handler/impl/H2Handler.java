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
package org.seasar.dolteng.projects.handler.impl;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.Driver;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.h2.tools.RunScript;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.projects.ProjectBuilder;
import org.seasar.dolteng.projects.model.Entry;
import org.seasar.extension.jdbc.util.ConnectionUtil;
import org.seasar.framework.util.InputStreamUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
@SuppressWarnings("serial")
public class H2Handler extends DefaultHandler {

    private transient Connection connection;

    public H2Handler() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.template.DefaultHandler#getType()
     */
    @Override
    public String getType() {
        return "h2";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.template.DefaultHandler#handle(org.seasar.dolteng.eclipse.template.ProjectBuilder,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void handle(ProjectBuilder builder, IProgressMonitor monitor) {
        try {
            if (ProjectUtil.hasNature(builder.getProjectHandle(),
                    Constants.ID_DB_LAUNCHER_NATURE)) {
                ScopedPreferenceStore store = new ScopedPreferenceStore(
                        new ProjectScope(builder.getProjectHandle()),
                        Constants.ID_DB_LAUNCHER_PLUGIN);
                String s = store.getString("initDB");
                if (StringUtil.isEmpty(s) == false) {
                    IWorkspaceRoot root = ProjectUtil.getWorkspaceRoot();
                    IPath p = root.getFolder(new Path(s)).getLocation();
                    String url = "jdbc:h2:file:" + p.append("demo").toString();
                    Class clazz = Class.forName("org.h2.Driver");
                    Driver driver = (Driver) clazz.newInstance();
                    Properties conf = new Properties();
                    conf.put("user", "sa");
                    conf.put("password", "");
                    connection = driver.connect(url, conf);
                }
            }
            super.handle(builder, monitor);
        } catch (Exception e) {
            DoltengCore.log(e);
        } finally {
            ConnectionUtil.close(connection);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.template.DefaultHandler#processTxt(org.seasar.dolteng.eclipse.template.ProjectBuilder,
     *      org.seasar.dolteng.eclipse.template.ProjectBuildConfigResolver.Entry)
     */
    @Override
    protected void processTxt(ProjectBuilder builder, Entry entry) {
        InputStream in = null;
        try {
            super.processTxt(builder, entry);
            if (entry.getPath().endsWith(".sql") && connection != null) {
                IFile query = builder.getProjectHandle().getFile(
                        entry.getPath());
                in = query.getContents();
                Reader r = new InputStreamReader(new BufferedInputStream(in),
                        "UTF-8");
                RunScript.execute(connection, r);
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        } finally {
            InputStreamUtil.close(in);
        }
    }

}
