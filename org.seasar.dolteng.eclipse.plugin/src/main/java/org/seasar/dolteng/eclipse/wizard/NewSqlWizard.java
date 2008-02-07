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
package org.seasar.dolteng.eclipse.wizard;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;

/**
 * @author taichi
 * 
 */
public class NewSqlWizard extends BasicNewResourceWizard {

    private WizardNewFileCreationPage mainPage;

    private String fileName = "";

    private IPath containerPath;

    /**
     * 
     */
    public NewSqlWizard() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        this.mainPage = new WizardNewFileCreationPage("NewSqlWizardPage",
                getSelection());
        this.mainPage.setFileName(fileName);
        this.mainPage.setContainerFullPath(this.containerPath);
        addPage(this.mainPage);
    }

    public void setFileName(String name) {
        this.fileName = name;
    }

    public void setContainerFullPath(IPath path) {
        containerPath = path;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        IFile file = this.mainPage.createNewFile();
        if (file != null) {
            WorkbenchUtil.openResource(file);
            return true;
        }
        return false;
    }

}
