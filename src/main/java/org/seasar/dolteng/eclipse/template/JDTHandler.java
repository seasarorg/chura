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

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.template.ProjectBuildConfigResolver.Entry;
import org.seasar.framework.util.InputStreamUtil;

/**
 * @author taichi
 * 
 */
public class JDTHandler extends DefaultHandler {

    public JDTHandler() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.template.DefaultHandler#getType()
     */
    public String getType() {
        return "jdt";
    }

    public void handle(ProjectBuilder builder, IProgressMonitor monitor) {
        IJavaProject project = JavaCore.create(builder.getProjectHandle());
        Map options = project.getOptions(false);
        for (final Iterator i = this.entries.iterator(); i.hasNext();) {
            Entry entry = (Entry) i.next();
            Properties p = load(builder.findResource(entry.path));
            for (Enumeration e = p.propertyNames(); e.hasMoreElements();) {
                String key = e.nextElement().toString();
                options.put(key, p.getProperty(key));
            }
            project.setOptions(options);
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
