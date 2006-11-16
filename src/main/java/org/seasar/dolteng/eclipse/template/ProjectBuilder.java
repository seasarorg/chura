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

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;
import org.seasar.dolteng.eclipse.util.ProjectUtil;

/**
 * @author taichi
 * 
 */
public class ProjectBuilder {

    private IProject project;

    private IPath location;

    private Map handlers = new HashMap();

    private List resourceRoots = new ArrayList();

    private int works = 1;

    public ProjectBuilder(IProject project, IPath location) {
        super();
        this.project = project;
        this.location = location;
    }

    public void addHandler(ResourceHandler handler) {
        ResourceHandler master = (ResourceHandler) handlers.get(handler
                .getType());
        if (handler == null) {
            works += handler.getNumberOfFiles();
            handlers.put(handler.getType(), handler);
        } else {
            works -= master.getNumberOfFiles();
            master.merge(handler);
            works += master.getNumberOfFiles();
        }
    }

    public void add(String path) {
        this.resourceRoots.add(new Path(path));
    }

    public IProject getProjectHandle() {
        return this.project;
    }

    public URL findResource(String path) {
        URL result = null;
        Bundle bundle = DoltengCore.getDefault().getBundle();
        for (final Iterator i = this.resourceRoots.iterator(); i.hasNext();) {
            IPath root = (IPath) i.next();
            result = bundle.getEntry(root.append(path).toString());
            if (result != null) {
                break;
            }
        }
        return result;
    }

    public void build(IProgressMonitor monitor) {
        try {
            monitor = ProgressMonitorUtil.care(monitor);
            monitor.beginTask(Messages.BEGINING_OF_CREATE, works);
            monitor.setTaskName(Messages.CREATE_BASE_PROJECT);

            ProjectUtil.createProject(project, location, null);
            ProgressMonitorUtil.isCanceled(monitor, 1);

            for (final Iterator i = handlers.keySet().iterator(); i.hasNext();) {
                ResourceHandler handler = (ResourceHandler) handlers.get(i
                        .next());
                handler.handle(this, monitor);
            }
        } catch (CoreException e) {
            DoltengCore.log(e);
        } finally {
            monitor.done();
        }
    }
}
