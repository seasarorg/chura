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

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;
import org.seasar.dolteng.projects.ProjectBuilder;

/**
 * @author taichi
 * 
 */
public class ChuraProjectWizard extends Wizard implements INewWizard {

    private ChuraProjectWizardPage creationPage;

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
        creationPage = new ChuraProjectWizardPage();
        addPage(creationPage);
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
        public NewChuraProjectCreation() {
        }

        public void run(IProgressMonitor monitor) throws InterruptedException {
            monitor = ProgressMonitorUtil.care(monitor);
            try {
                Map<String, String> ctx = new HashMap<String, String>();
                ctx.put(Constants.CTX_PROJECT_NAME, creationPage
                        .getProjectName());
                ctx.put(Constants.CTX_PACKAGE_NAME, creationPage
                        .getRootPackageName());
                ctx.put(Constants.CTX_PACKAGE_PATH, creationPage
                        .getRootPackagePath());
                ctx.put(Constants.CTX_JRE_CONTAINER, creationPage
                        .getJREContainer());
                ctx.put(Constants.CTX_LIB_PATH, creationPage
                		.getLibraryPath());
                ctx.put(Constants.CTX_LIB_SRC_PATH, creationPage
                		.getLibrarySourcePath());
                ctx.put(Constants.CTX_TEST_LIB_PATH, creationPage
                		.getTestLibraryPath());
                ctx.put(Constants.CTX_TEST_LIB_SRC_PATH, creationPage
                		.getTestLibrarySourcePath());
                ctx.put(Constants.CTX_MAIN_JAVA_PATH, creationPage
                		.getMainJavaPath());
                ctx.put(Constants.CTX_MAIN_RESOURCE_PATH, creationPage
                		.getMainResourcePath());
                ctx.put(Constants.CTX_MAIN_OUT_PATH, creationPage
                		.getMainOutputPath());
                ctx.put(Constants.CTX_WEBAPP_ROOT, creationPage
                		.getWebappRootPath());
                ctx.put(Constants.CTX_TEST_JAVA_PATH, creationPage
                		.getTestJavaPath());
                ctx.put(Constants.CTX_TEST_RESOURCE_PATH, creationPage
                		.getTestResourcePath());
                ctx.put(Constants.CTX_TEST_OUT_PATH, creationPage
                		.getTestOutputPath());

                ProjectBuilder builder = creationPage.getResolver().resolve(
                        creationPage.getProjectTypeKey(),
                        creationPage.getProjectHandle(),
                        creationPage.getLocationPath(), ctx);
                builder.build(monitor);
            } catch (Exception e) {
                DoltengCore.log(e);
                throw new InterruptedException();
            }
        }
    }
}
