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
package org.seasar.dolteng.eclipse.model.impl;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.seasar.dolteng.eclipse.action.ActionRegistry;
import org.seasar.dolteng.eclipse.model.TreeContent;
import org.seasar.dolteng.eclipse.model.TreeContentState;

/**
 * @author taichi
 * 
 */
public abstract class AbstractLeaf implements TreeContent, IAdaptable {

    private TreeContent parent;

    private TreeContent root;

    private TreeContentState state = TreeContentState.BEGIN;

    public AbstractLeaf() {
    }

    public AbstractLeaf(TreeContent parent) {
        this.root = parent;
        this.parent = parent;
    }

    public AbstractLeaf(TreeContent root, TreeContent parent) {
        this.root = root;
        this.parent = parent;
    }

    public void updateState(TreeContentState state) {
        this.state = state;
    }

    public TreeContentState getState() {
        return this.state;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.TreeContent#getParent()
     */
    public TreeContent getParent() {
        return this.parent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.TreeContent#setParent(org.seasar.dolteng.ui.eclipse.models.TreeContent)
     */
    public void setParent(TreeContent tc) {
        this.parent = tc;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.TreeContent#setRoot(org.seasar.dolteng.ui.eclipse.models.TreeContent)
     */
    public void setRoot(TreeContent tc) {
        this.root = tc;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.TreeContent#getRoot()
     */
    public TreeContent getRoot() {
        return this.root;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.TreeContent#getChildren()
     */
    public TreeContent[] getChildren() {
        return EMPTY;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.TreeContent#findChildren()
     */
    public void findChildren() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.TreeContent#clearChildren()
     */
    public void clearChildren() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.TreeContent#addChild(org.seasar.dolteng.ui.eclipse.models.TreeContent)
     */
    public void addChild(TreeContent content) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.TreeContent#removeChild(org.seasar.dolteng.ui.eclipse.models.TreeContent)
     */
    public void removeChild(TreeContent content) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.TreeContent#hasChildren()
     */
    public boolean hasChildren() {
        return this.state.hasChildren();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.TreeContent#fillContextMenu(org.eclipse.jface.action.IMenuManager,
     *      org.seasar.dolteng.ui.eclipse.actions.ActionRegistry)
     */
    public void fillContextMenu(IMenuManager manager, ActionRegistry registry) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.TreeContent#doubleClick(org.seasar.dolteng.ui.eclipse.actions.ActionRegistry)
     */
    public void doubleClick(ActionRegistry registry) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.TreeContent#expanded(org.seasar.dolteng.ui.eclipse.actions.ActionRegistry)
     */
    public void expanded(AbstractTreeViewer viewer, ActionRegistry registry) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.TreeContent#collapsed(org.seasar.dolteng.ui.eclipse.actions.ActionRegistry)
     */
    public void collapsed(AbstractTreeViewer viewer, ActionRegistry registry) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o) {
        if (o instanceof TreeContent) {
            TreeContent cd = (TreeContent) o;
            return this.getText().compareTo(cd.getText());
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof TreeContent) {
            return this.equals((TreeContent) other);
        }
        return super.equals(other);
    }

    public boolean equals(TreeContent other) {
        return other != null ? this.getText().equals(other.getText()) : false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.getText().hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.getText();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter) {
        if (IJavaElement.class.isAssignableFrom(adapter)) {
            TreeContent tc = getRoot();
            if (tc instanceof ProjectNode) {
                ProjectNode pn = (ProjectNode) tc;
                return pn.getJavaProject();
            }
        }
        return null;
    }

}
