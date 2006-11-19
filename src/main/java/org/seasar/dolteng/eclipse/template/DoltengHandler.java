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
import java.util.Properties;

import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.template.ProjectBuildConfigResolver.Entry;
import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.framework.util.InputStreamUtil;

/**
 * @author taichi
 * 
 */
public class DoltengHandler extends DefaultHandler {

    /**
     * 
     */
    public DoltengHandler() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.template.DefaultHandler#getType()
     */
    public String getType() {
        return "dolteng";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.template.DefaultHandler#handle(org.seasar.dolteng.eclipse.template.ProjectBuilder,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public void handle(ProjectBuilder builder, IProgressMonitor monitor) {
        try {
            monitor.setTaskName(Messages
                    .bind(Messages.ADD_NATURE_OF, "Dolteng"));
            ProjectUtil.addNature(builder.getProjectHandle(),
                    Constants.ID_NATURE);
            ProgressMonitorUtil.isCanceled(monitor, 1);
            IPersistentPreferenceStore store = new ScopedPreferenceStore(
                    new ProjectScope(builder.getProjectHandle()),
                    Constants.ID_PLUGIN);
            for (final Iterator i = this.entries.iterator(); i.hasNext();) {
                Entry entry = (Entry) i.next();
                Properties p = load(builder.findResource(entry.path));
                for (Enumeration e = p.propertyNames(); e.hasMoreElements();) {
                    String key = e.nextElement().toString();
                    store.setValue(key, p.getProperty(key));
                }
            }
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
