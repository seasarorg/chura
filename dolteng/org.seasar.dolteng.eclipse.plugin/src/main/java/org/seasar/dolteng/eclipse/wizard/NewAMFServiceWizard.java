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
package org.seasar.dolteng.eclipse.wizard;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.seasar.dolteng.eclipse.DoltengCore;

/**
 * @author taichi
 * 
 */
public class NewAMFServiceWizard extends BasicNewResourceWizard {

    private IFile mxml;

    private NewClassWizardPage mainPage;

    // TODO 未実装
    /**
     * 
     */
    public NewAMFServiceWizard() {
        super();
        setNeedsProgressMonitor(true);
        setDialogSettings(DoltengCore.getDialogSettings());
    }

    public void setCallerMxml(IFile mxml) {
        this.mxml = mxml;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        mainPage = new NewClassWizardPage();

        mainPage.init(StructuredSelection.EMPTY);
        mainPage.setTypeName(mxml.getName() + "Service", false);

        addPage(mainPage);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        return false;
    }

}
