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
package org.seasar.dolteng.eclipse.template;

import java.io.OutputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.seasar.dolteng.core.template.TemplateConfig;
import org.seasar.dolteng.core.template.TemplateHandler;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.RootModel;
import org.seasar.framework.util.OutputStreamUtil;

/**
 * @author taichi
 * 
 */
public abstract class AbstractTemplateHandler implements TemplateHandler {

    protected IProject project;

    // FIXME : monitor と TemplateHandlerのライフサイクル差分が超微妙。
    protected IProgressMonitor monitor;

    protected RootModel baseModel;

    protected AbstractTemplateHandler(IProject project,
            IProgressMonitor monitor, RootModel baseModel) {
        this.project = project;
        this.monitor = monitor;
        this.baseModel = baseModel;
        this.baseModel.setNamingConvention(DoltengCore.getPreferences(project)
                .getNamingConvention());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.template.TemplateHandler#close(java.io.OutputStream)
     */
    public void close(OutputStream stream) {
        OutputStreamUtil.close(stream);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.template.TemplateHandler#done()
     */
    public void done() {
        try {
            project.refreshLocal(IResource.DEPTH_INFINITE, null);
            monitor.done();
        } catch (CoreException e) {
            DoltengCore.log(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.template.TemplateHandler#fail(org.seasar.dolteng.eclipse.model.RootModel,
     *      java.lang.Exception)
     */
    public void fail(RootModel model, Exception e) {
        DoltengCore.log(e);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.template.TemplateHandler#getProcessModel(org.seasar.dolteng.core.template.TemplateConfig)
     */
    public RootModel getProcessModel(TemplateConfig config) {
        return baseModel;
    }
}
