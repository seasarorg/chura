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
package org.seasar.dolteng.eclipse.template;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.h2.tools.RunScript;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.template.ProjectBuildConfigResolver.Entry;
import org.seasar.extension.jdbc.util.ConnectionUtil;
import org.seasar.framework.util.InputStreamUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class H2Handler extends DefaultHandler {

    private Connection connection;

    /**
     * 
     */
    public H2Handler() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.template.DefaultHandler#getType()
     */
    public String getType() {
        return "h2";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.template.DefaultHandler#handle(org.seasar.dolteng.eclipse.template.ProjectBuilder,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public void handle(ProjectBuilder builder, IProgressMonitor monitor) {
        try {
            String s = (String) builder.getConfigContext().get(
                    Constants.CTX_MAIN_RESOURCE_PATH);
            if (StringUtil.isEmpty(s) == false) {
                IPath p = builder.getProjectHandle().getFolder(
                        new Path(s).append("data")).getLocation();
                String url = "jdbc:h2:file:" + p.append("demo").toString();
                Class.forName("org.h2.Driver");
                connection = DriverManager.getConnection(url, "sa", "");
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
    protected void processTxt(ProjectBuilder builder, Entry entry) {
        InputStream in = null;
        try {
            super.processTxt(builder, entry);
            if (entry.path.endsWith(".sql")) {
                IFile query = builder.getProjectHandle().getFile(entry.path);
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
