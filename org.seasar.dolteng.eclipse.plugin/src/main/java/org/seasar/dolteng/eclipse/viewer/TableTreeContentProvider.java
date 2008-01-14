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
package org.seasar.dolteng.eclipse.viewer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Event;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.action.FindChildrenAction;
import org.seasar.dolteng.eclipse.model.TreeContent;
import org.seasar.dolteng.eclipse.model.impl.BasicNode;
import org.seasar.dolteng.eclipse.model.impl.ProjectNode;
import org.seasar.dolteng.eclipse.util.ProjectUtil;

/**
 * @author taichi
 * 
 */
public class TableTreeContentProvider implements ITreeContentProvider {

    private TreeContent invisible;

    public TableTreeContentProvider() {
        this.invisible = new BasicNode("", null);
    }

    public void initialize() {
        try {
            invisible.clearChildren();
            IJavaProject[] projects = ProjectUtil.getDoltengProjects();
            for (int i = 0; projects != null && i < projects.length; i++) {
                initialize(projects[i]);
            }
        } catch (CoreException e) {
            DoltengCore.log(e);
        }
    }

    public void initialize(IJavaProject project) {
        IProject p = project.getProject();
        if (p.exists() && p.isAccessible()) {
            invisible.addChild(new ProjectNode(project));
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof TreeContent) {
            TreeContent tc = (TreeContent) inputElement;
            return tc.getChildren();
        }
        return invisible.getChildren();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {
        this.invisible.dispose();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (viewer instanceof AbstractTreeViewer
                && newInput instanceof IJavaProject) {
            IJavaProject proj = (IJavaProject) newInput;
            TreeContent[] tcs = this.invisible.getChildren();
            for (int i = 0; i < tcs.length; i++) {
                ProjectNode content = (ProjectNode) tcs[i];
                if (content.getJavaProject().equals(proj)) {
                    AbstractTreeViewer atv = (AbstractTreeViewer) viewer;
                    FindChildrenAction action = new FindChildrenAction(atv);
                    Event event = new Event();
                    event.data = content;
                    action.runWithEvent(event);
                    break;
                }
            }

        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof TreeContent) {
            TreeContent tc = (TreeContent) parentElement;
            return tc.getChildren();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    public Object getParent(Object element) {
        if (element instanceof TreeContent) {
            TreeContent tc = (TreeContent) element;
            return tc.getParent();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren(Object element) {
        if (element instanceof TreeContent) {
            TreeContent tc = (TreeContent) element;
            return tc.hasChildren();
        }
        return false;
    }

}
