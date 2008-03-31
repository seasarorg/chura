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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.h2.util.IOUtils;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;
import org.seasar.dolteng.projects.ProjectBuilder;
import org.seasar.dolteng.projects.handler.impl.DiconHandler;
import org.seasar.dolteng.projects.utils.XStreamSerializer;

/**
 * @author taichi
 */
public class KickstartProjectWizard extends Wizard implements INewWizard {

    private KickstartProjectWizardPage page;

    public KickstartProjectWizard() {
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
        page = new KickstartProjectWizardPage();
        addPage(page);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        try {
            getContainer().run(false, false, new KickstartProjectCreation());
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

    private class KickstartProjectCreation implements IRunnableWithProgress {
        public void run(IProgressMonitor monitor) throws InterruptedException {
            monitor = ProgressMonitorUtil.care(monitor);
            DiconHandler.init(); // 前回生成時の設定をクリア
            
            InputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(page.getKickstartFileName()));
                ProjectBuilder builder = readKickstartFile(is);
                builder.build(monitor);
            } catch (Exception e) {
                DoltengCore.log(e);
                throw new InterruptedException();
            } finally {
                IOUtils.closeSilently(is);
            }
        }

        private ProjectBuilder readKickstartFile(InputStream is) throws UnsupportedEncodingException {
            ProjectBuilder builder = (ProjectBuilder) XStreamSerializer.deserialize(is, getClass().getClassLoader());
            builder.setProject(page.getProjectHandle());
            builder.setLocation(page.getLocationPath());
            return builder;
        }
    }
}
