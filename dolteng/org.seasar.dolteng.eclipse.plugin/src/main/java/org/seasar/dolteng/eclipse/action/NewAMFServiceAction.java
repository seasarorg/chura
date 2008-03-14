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
package org.seasar.dolteng.eclipse.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.util.ResourcesUtil;
import org.seasar.dolteng.eclipse.util.TextEditorUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.dolteng.eclipse.wizard.NewAMFServiceWizard;

/**
 * @author taichi
 * 
 */
public class NewAMFServiceAction implements IEditorActionDelegate {

    private IFile mxml;

    protected ITextEditor txtEditor;

    /**
     * 
     */
    public NewAMFServiceAction() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction,
     *      org.eclipse.ui.IEditorPart)
     */
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        if (targetEditor != null) {
            IEditorInput input = targetEditor.getEditorInput();
            this.mxml = ResourcesUtil.toFile(input);
        }
        this.txtEditor = TextEditorUtil.toTextEditor(targetEditor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        try {
            if (selection instanceof IStructuredSelection) {
                IStructuredSelection struct = (IStructuredSelection) selection;
                Object obj = struct.getFirstElement();
                this.mxml = ResourcesUtil.toFile(obj);
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        if (this.mxml == null) {
            return;
        }
        if (this.txtEditor == null) {
            this.txtEditor = TextEditorUtil.toTextEditor(WorkbenchUtil
                    .getActiveEditor());
        }

        NewAMFServiceWizard wiz = new NewAMFServiceWizard();
        wiz.setCallerMxml(mxml);
        WorkbenchUtil.startWizard(wiz);
    }

}
