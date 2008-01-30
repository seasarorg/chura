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
import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;
import org.seasar.dolteng.projects.ProjectBuilder;
import org.seasar.dolteng.projects.handler.impl.dicon.DiconModel;

/**
 * @author taichi
 * 
 */
public class ChuraProjectWizard extends Wizard implements INewWizard {

    private ChuraProjectWizardPage page;

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
            	String[] projectTypes = page.getProjectTypeKeys();
            	
            	// TODO ここで処理しちゃあかんよなー…
            	if(Arrays.asList(projectTypes).contains("kuina")
            			|| Arrays.asList(projectTypes).contains("s2jmsOut")
            			|| Arrays.asList(projectTypes).contains("s2jmsInOut")) {
            		for(int i = 0; i < projectTypes.length; i++) {
            			if("teedaPage".equals(projectTypes[i]) || "teedaAction".equals(projectTypes[i])) {
            				projectTypes[i] = "teeda";
            			}
            		}
            	}
            	
                System.out.println("build: " + Arrays.toString(projectTypes));
    			DiconModel.init("app");
    			DiconModel.init("customizer");
                ProjectBuilder builder = page.getResolver().resolve(
                		projectTypes,
                        page.getProjectHandle(),
                        page.getLocationPath(),
                        page.getConfigureContext(),
                        page.getEditContext());
                builder.build(monitor);
            } catch (Exception e) {
                DoltengCore.log(e);
                throw new InterruptedException();
            }
        }
    }
}
