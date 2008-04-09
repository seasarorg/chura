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
package org.seasar.dolteng.projects.wizard;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;
import org.seasar.dolteng.projects.ProjectBuildConfigResolver;
import org.seasar.dolteng.projects.ProjectBuilder;
import org.seasar.dolteng.projects.handler.impl.DiconHandler;
import org.seasar.dolteng.projects.utils.XStreamSerializer;
import org.seasar.framework.util.InputStreamUtil;

/**
 * @author taichi
 */
public class ChuraProjectWizard extends Wizard implements INewWizard {

    private ChuraProjectWizardPage page;

    private ChuraProjectWizardDirectoryPage directoryPage;

    // private ConnectionWizardPage connectionPage;

    public ChuraProjectWizard() {
        super();
        setNeedsProgressMonitor(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        super.addPages();
        page = new ChuraProjectWizardPage();
        addPage(page);
        directoryPage = new ChuraProjectWizardDirectoryPage();
        addPage(directoryPage);
        // connectionPage = new ConnectionWizardPage(creationPage);
        // addPage(connectionPage);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        try {
            getContainer().run(false, false, new NewChuraProjectCreation());
            return true;
        } catch (InvocationTargetException e) {
            DoltengCore.log(e.getTargetException());
        } catch (Exception e) {
            DoltengCore.log(e);
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
     *      org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {

    }

    private class NewChuraProjectCreation implements IRunnableWithProgress {
        public void run(IProgressMonitor monitor) throws InterruptedException {
            monitor = ProgressMonitorUtil.care(monitor);
            try {
                DiconHandler.init(); // 前回生成時の設定をクリア
                ProjectBuildConfigResolver resolver = page.getResolver();

                String[] facetIds = page.getSelectedFacetIds();

                Map<String, String> ctx = new HashMap<String, String>();
                ctx.putAll(page.getConfigureContext());
                ctx.putAll(directoryPage.getConfigureContext());

                ProjectBuilder builder = resolver.resolve(facetIds, page
                        .getProjectHandle(), page.getLocationPath(), ctx);
                builder.build(monitor);
                
                // 機能隠しのコメントアウト
//                createKickstartFile(builder);
            } catch (Exception e) {
                DoltengCore.log(e);
                throw new InterruptedException();
            } finally {
                monitor.done();
            }
        }

        @SuppressWarnings("unused")
        private void createKickstartFile(ProjectBuilder builder) {
            String txt = XStreamSerializer.serialize(builder, getClass().getClassLoader());
            IFile handle = builder.getProjectHandle().getFile("kickstart.xml");
            
            InputStream src = null;
            try {
                byte[] bytes = txt.getBytes("UTF-8");
                if (handle.exists() == false) {
                    src = new ByteArrayInputStream(bytes);
                    handle.create(src, IResource.FORCE, null);
                }
            } catch (Exception e) {
                DoltengCore.log(e);
            } finally {
                InputStreamUtil.close(src);
            }
        }
    }
}
