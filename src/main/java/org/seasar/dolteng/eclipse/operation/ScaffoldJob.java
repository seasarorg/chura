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
package org.seasar.dolteng.eclipse.operation;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.seasar.dolteng.core.template.TemplateExecutor;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.template.DoltengTemplateHandler;

/**
 * @author taichi
 * 
 */
public class ScaffoldJob extends WorkspaceJob {

    private DoltengTemplateHandler handler;

    /**
     * @param name
     */
    public ScaffoldJob(DoltengTemplateHandler handler) {
        super("ScaffoldJob");
        this.handler = handler;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
     */
    public boolean belongsTo(Object family) {
        return family == ResourcesPlugin.FAMILY_AUTO_BUILD;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.WorkspaceJob#runInWorkspace(org.eclipse.core.runtime.IProgressMonitor)
     */
    public IStatus runInWorkspace(IProgressMonitor monitor)
            throws CoreException {
        TemplateExecutor executor = DoltengCore.getTemplateExecutor();
        handler.prepare(monitor);
        executor.proceed(handler);
        return Status.OK_STATUS;
    }

}
