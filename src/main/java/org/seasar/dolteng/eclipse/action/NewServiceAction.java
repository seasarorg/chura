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
package org.seasar.dolteng.eclipse.action;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.dolteng.eclipse.wizard.NewServiceWizard;

/**
 * @author taichi
 * 
 */
public class NewServiceAction extends AbstractEditorActionDelegate {

    /**
     * 
     */
    public NewServiceAction() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.action.AbstractEditorActionDelegate#processJava(org.eclipse.core.resources.IProject,
     *      org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences,
     *      org.eclipse.jdt.core.IJavaElement)
     */
    protected void processJava(IProject project,
            DoltengProjectPreferences pref, IJavaElement element) {
        String name = element.getElementName();
        if (name.endsWith("Page.java")) {
            name = name.substring(0, name.lastIndexOf("Page.java"))
                    + "Action.java";
            IResource resource = element.getResource().getParent().findMember(
                    name);
            if (resource != null && resource.exists()) {
                WorkbenchUtil.showMessage(Messages.bind(
                        Messages.ACTION_HAS_SERVICE, resource
                                .getProjectRelativePath()),
                        MessageDialog.WARNING);
                return;
            }
        }
        if (element instanceof ICompilationUnit) {
            ICompilationUnit unit = (ICompilationUnit) element;
            NewServiceWizard wiz = new NewServiceWizard();
            wiz.setInjectionTarget(unit);
            WizardDialog dialog = new WizardDialog(WorkbenchUtil.getShell(),
                    wiz);
            dialog.open();
        }

    }

}
