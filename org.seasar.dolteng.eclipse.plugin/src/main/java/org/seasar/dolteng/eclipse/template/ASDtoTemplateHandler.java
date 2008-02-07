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

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.seasar.dolteng.core.template.TemplateConfig;
import org.seasar.dolteng.core.template.TemplateHandler;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.RootModel;
import org.seasar.dolteng.eclipse.model.impl.AsModel;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.ResourcesUtil;
import org.seasar.framework.util.OutputStreamUtil;

/**
 * @author taichi
 * 
 */
public class ASDtoTemplateHandler implements TemplateHandler {

    private AsModel baseModel;

    private IProgressMonitor monitor;

    private IPath outputpath;

    private String outputFile;

    private IFile created;

    public ASDtoTemplateHandler(ICompilationUnit unit,
            IProgressMonitor monitor, IPath outputpath) {
        super();
        this.monitor = monitor;
        this.outputpath = outputpath;
        baseModel = new AsModel(unit);
        baseModel.initialize();

        IType type = unit.findPrimaryType();
        this.outputFile = type.getElementName() + ".as";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.template.TemplateHandler#begin()
     */
    public void begin() {
        monitor = ProgressMonitorUtil.care(monitor);
        monitor.beginTask(Messages.GENERATE_CODES, 1);
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
            IWorkspaceRoot root = ProjectUtil.getWorkspaceRoot();
            IResource r = root.findMember(outputpath.segment(0));
            if (r != null && r.exists() && r.getType() == IResource.PROJECT) {
                r.refreshLocal(IResource.DEPTH_INFINITE, null);

            }
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
    public AsModel getProcessModel(TemplateConfig config) {
        return this.baseModel;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.template.TemplateHandler#getTemplateConfigs()
     */
    public TemplateConfig[] getTemplateConfigs() {
        TemplateConfig config = new TemplateConfig();
        config.setTemplatePath("template/fm/flex2/asdto.ftl");
        config.setOverride(true);
        config.setOutputPath(outputpath.toString());
        config.setOutputFile(outputFile);
        return new TemplateConfig[] { config };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.template.TemplateHandler#open(org.seasar.dolteng.core.template.TemplateConfig)
     */
    public OutputStream open(TemplateConfig config) {
        monitor.subTask(outputpath.toString());
        IWorkspaceRoot root = ProjectUtil.getWorkspaceRoot();

        try {
            ResourcesUtil.createDir(root, outputpath.toString());
            IPath p = outputpath.append(outputFile);
            created = root.getFile(p);
            if (created.exists()) {
                created.delete(true, null);
            }
            created.create(new ByteArrayInputStream(new byte[0]), true, null);
            return new FileOutputStream(created.getLocation().toFile());
        } catch (Exception e) {
            DoltengCore.log(e);
            throw new RuntimeException(e);
        } finally {
            monitor.worked(1);
        }
    }

    public IFile getCreated() {
        return created;
    }
}
