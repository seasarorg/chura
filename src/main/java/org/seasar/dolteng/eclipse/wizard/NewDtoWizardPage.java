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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.wizards.NewClassWizardPage;

/**
 * @author taichi
 * 
 */
public class NewDtoWizardPage extends NewClassWizardPage {

    private DtoMappingPage mappingPage;

    public NewDtoWizardPage(DtoMappingPage mappingPage) {
        super();
        this.mappingPage = mappingPage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.ui.wizards.NewClassWizardPage#createTypeMembers(org.eclipse.jdt.core.IType,
     *      org.eclipse.jdt.ui.wizards.NewTypeWizardPage.ImportsManager,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    protected void createTypeMembers(IType type, ImportsManager imports,
            IProgressMonitor monitor) throws CoreException {
        super.createTypeMembers(type, imports, monitor);
    }

}
