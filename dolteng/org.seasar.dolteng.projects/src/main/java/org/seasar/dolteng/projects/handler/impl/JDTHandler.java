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

import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.ScriptingUtil;
import org.seasar.dolteng.projects.ProjectBuilder;
import org.seasar.dolteng.projects.model.Entry;
import org.seasar.framework.util.InputStreamUtil;

/**
 * @author taichi
 */
@SuppressWarnings("serial")
public class JDTHandler extends DefaultHandler {

    public JDTHandler() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.template.DefaultHandler#getType()
     */
    @Override
    public String getType() {
        return "jdt";
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handle(ProjectBuilder builder, IProgressMonitor monitor) {
        try {
            monitor.setTaskName(Messages.bind(Messages.ADD_NATURE_OF, "JDT"));

            builder.getProjectHandle().setDefaultCharset("UTF-8", null);
            ProjectUtil.addNature(builder.getProjectHandle(),
                    JavaCore.NATURE_ID);
            IJavaProject project = JavaCore.create(builder.getProjectHandle());
            Map<Object, Object> options = project.getOptions(false);
            for (Entry entry : entries) {
                URL url = builder.findResource(entry);
                if (url != null) {
                    Properties p = load(url);
                    for (Map.Entry<Object, Object> e : p.entrySet()) {
                        String key = (String) e.getKey();
                        String value = (String) e.getValue();
                        key = ScriptingUtil.resolveString(key, builder
                                .getConfigContext());
                        value = ScriptingUtil.resolveString(value, builder
                                .getConfigContext());
                        options.put(key, value);
                    }
                    project.setOptions(options);
                } else {
                    DoltengCore.log("missing ." + entry.getPath());
                }
            }
            ProgressMonitorUtil.isCanceled(monitor, 1);
        } catch (CoreException e) {
            DoltengCore.log(e);
        }
    }

    private Properties load(URL url) {
        Properties p = new Properties();
        InputStream in = null;
        try {
            in = url.openStream();
            p.load(in);
        } catch (Exception e) {
            DoltengCore.log(e);
        } finally {
            InputStreamUtil.close(in);
        }
        return p;
    }
}
