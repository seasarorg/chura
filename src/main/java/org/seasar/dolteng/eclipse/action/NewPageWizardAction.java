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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.dolteng.eclipse.wizard.NewPageWizard;

/**
 * @author taichi
 * 
 */
public class NewPageWizardAction implements IActionDelegate {

    private IStructuredSelection selection;

    /**
     * 
     */
    public NewPageWizardAction() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        Object o = this.selection.getFirstElement();
        if (o instanceof IFile) {
            IFile f = (IFile) o;
            IProject project = f.getProject();
            IJavaProject javap = JavaCore.create(project);
            if (javap.exists() && javap.isOpen()) {
                DoltengProjectPreferences pref = DoltengCore
                        .getPreferences(project);
                if (pref != null) {
                    String viewRoot = pref.getRawPreferences().getString(
                            Constants.PREF_DEFAULT_VIEW_ROOT_PATH);
                    IFolder fol = project.getFolder(pref.getWebContentsRoot());
                    fol = fol.getFolder(viewRoot);
                    IPath rootPath = fol.getFullPath();
                    IPath htmlPath = f.getParent().getFullPath();
                    String[] segroot = rootPath.segments();
                    String[] seghtml = htmlPath.segments();
                    boolean match = segroot != null && seghtml != null
                            && segroot.length < seghtml.length;
                    for (int i = 0; match && i < segroot.length; i++) {
                        if (segroot[i].equals(seghtml[i]) == false) {
                            match = false;
                            break;
                        }
                    }

                    if (match) {
                        NewPageWizard wiz = new NewPageWizard();
                        wiz.init(PlatformUI.getWorkbench(), this.selection);
                        WizardDialog diag = new WizardDialog(WorkbenchUtil
                                .getShell(), wiz);
                        diag.open();
                        return;
                    }
                }
            }
        }
        WorkbenchUtil.showMessage(Messages.INVALID_HTML_PATH);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection iss = (IStructuredSelection) selection;
            this.selection = iss;
        }
    }

}