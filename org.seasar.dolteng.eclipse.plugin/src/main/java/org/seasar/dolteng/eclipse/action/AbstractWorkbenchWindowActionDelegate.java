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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.util.TextEditorUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.eclipse.common.util.AdaptableUtil;
import org.seasar.eclipse.common.util.ResouceUtil;

/**
 * @author taichi
 * 
 */
public abstract class AbstractWorkbenchWindowActionDelegate implements
        IWorkbenchWindowActionDelegate {

    public AbstractWorkbenchWindowActionDelegate() {
        super();
    }

    public void selectionChanged(IAction action, ISelection selection) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        IResource resource = ResouceUtil.getCurrentSelectedResouce();
        if (resource == null) {
            return;
        }
        IProject project = resource.getProject();

        DoltengPreferences pref = DoltengCore.getPreferences(project);
        if (pref == null) {
            return;
        }

        try {
            if (JavaCore.isJavaLikeFileName(resource.getName())) {
                processJava(project, pref, JavaCore.create(resource));
            } else {
                processResource(project, pref, resource);
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        }
    }

    protected IJavaElement getSelectionElement() throws JavaModelException {
        IJavaElement result = null;
        ITextEditor txtEditor = TextEditorUtil.toTextEditor(WorkbenchUtil
                .getActiveEditor());
        if (txtEditor != null) {
            IResource resource = AdaptableUtil.toResource(txtEditor
                    .getEditorInput());
            if (resource != null) {
                IJavaElement javaElement = JavaCore.create(resource);
                if (javaElement instanceof ICompilationUnit) {
                    ICompilationUnit unit = (ICompilationUnit) javaElement;
                    ISelectionProvider provider = txtEditor
                            .getSelectionProvider();
                    if (provider != null) {
                        ISelection selection = provider.getSelection();
                        if (selection instanceof ITextSelection) {
                            ITextSelection ts = (ITextSelection) selection;
                            result = unit.getElementAt(ts.getOffset());
                        }
                    }
                }
            }
        }
        return result;
    }

    protected void processJava(IProject project, DoltengPreferences pref,
            IJavaElement element) throws Exception {
    }

    protected void processResource(IProject project, DoltengPreferences pref,
            IResource resource) throws Exception {
    }

    public void dispose() {

    }

    public void init(IWorkbenchWindow window) {

    }
}
