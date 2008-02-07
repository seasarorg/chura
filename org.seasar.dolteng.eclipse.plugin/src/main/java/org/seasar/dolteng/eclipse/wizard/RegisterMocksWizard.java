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

import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.wizard.Wizard;

/**
 * @author taichi
 * 
 */
public class RegisterMocksWizard extends Wizard {

    private IPackageFragmentRoot root;

    private RegisterMocksWizardPage page;

    public RegisterMocksWizard(IPackageFragmentRoot root) {
        super();
        setNeedsProgressMonitor(true);
        this.root = root;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        page = new RegisterMocksWizardPage();
        page.setPackageFragmentRoot(root);
        addPage(page);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        return page.registerMocks();
    }

}
