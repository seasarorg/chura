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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;

/**
 * @author taichi
 * 
 */
public abstract class AbstractEditorActionDelegate implements
        IEditorActionDelegate {

    private IResource resource;

    private IJavaElement javaElement;

    public AbstractEditorActionDelegate() {
        super();
    }

    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        if (targetEditor != null) {
            IEditorInput input = targetEditor.getEditorInput();
            this.resource = (IResource) input.getAdapter(IResource.class);
            this.javaElement = (IJavaElement) input
                    .getAdapter(IJavaElement.class);
        }
    }

    public void selectionChanged(IAction action, ISelection selection) {
        try {
            if (selection instanceof IStructuredSelection) {
                IStructuredSelection struct = (IStructuredSelection) selection;
                Object obj = struct.getFirstElement();
                if (obj instanceof IResource) {
                    this.resource = (IResource) obj;
                    if ("java".equals(resource.getFileExtension())) {
                        this.javaElement = JavaCore.create(this.resource);
                    }
                } else if (obj instanceof IJavaElement) {
                    this.javaElement = (IJavaElement) obj;
                }
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
        if (resource == null) {
            return;
        }
        IProject project = resource.getProject();

        DoltengProjectPreferences pref = DoltengCore.getPreferences(project);
        if (pref == null) {
            return;
        }
        if (this.javaElement != null) {
            processJava(project, pref, this.javaElement);
        } else {
            processResource(project, pref, this.resource);
        }
    }

    protected void processJava(IProject project,
            DoltengProjectPreferences pref, IJavaElement element) {
    }

    protected void processResource(IProject project,
            DoltengProjectPreferences pref, IResource resource) {
    }
}
