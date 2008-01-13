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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.action.ActionRegistry;
import org.seasar.dolteng.eclipse.action.ConnectionConfigAction;
import org.seasar.dolteng.eclipse.action.FindChildrenAction;
import org.seasar.dolteng.eclipse.model.TreeContent;
import org.seasar.dolteng.eclipse.model.TreeContentState;
import org.seasar.dolteng.eclipse.nls.Images;
import org.seasar.dolteng.eclipse.preferences.ConnectionConfig;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.util.JdbcDiconResourceVisitor;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.JdbcDiconResourceVisitor.ConnectionConfigHandler;

/**
 * @author taichi
 * 
 */
public class ProjectNode extends AbstractNode implements
        ConnectionConfigHandler {

    private IJavaProject project;

    public ProjectNode(IJavaProject project) {
        this.project = project;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.ContentDescriptor#getText()
     */
    public String getText() {
        return project.getElementName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.ContentDescriptor#getImage()
     */
    public Image getImage() {
        IProject p = project.getProject();
        IWorkbenchAdapter adapter = (IWorkbenchAdapter) p
                .getAdapter(IWorkbenchAdapter.class);
        if (adapter != null) {
            ImageDescriptor desc = adapter.getImageDescriptor(p);
            if (desc != null) {
                return desc.createImage();
            }
        }
        return Images.JAVA_PROJECT;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.impl.AbstractLeaf#getRoot()
     */
    @Override
    public TreeContent getRoot() {
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.impl.AbstractLeaf#fillContextMenu(org.eclipse.jface.action.IMenuManager,
     *      org.seasar.dolteng.ui.eclipse.actions.ActionRegistry)
     */
    @Override
    public void fillContextMenu(IMenuManager manager, ActionRegistry registry) {
        manager.add(registry.find(ConnectionConfigAction.ID));
        manager.add(new Separator());
        manager.add(registry.find(FindChildrenAction.ID));
    }

    @Override
    public void findChildren() {
        DoltengPreferences pref = DoltengCore
                .getPreferences(this.project);
        if (pref == null) {
            return;
        }
        ConnectionConfig[] configs = pref.getAllOfConnectionConfig();
        for (ConnectionConfig config : configs) {
            TreeContent tc = new ConnectionNode(config);
            addChild(tc);
        }

        loadFromProject();

        updateState(0 < getChildren().length ? TreeContentState.SEARCHED
                : TreeContentState.EMPTY);
    }

    protected void loadFromProject() {
        try {
            IPackageFragmentRoot[] roots = ProjectUtil
                    .findSrcFragmentRoots(this.project);
            IResourceVisitor visitor = new JdbcDiconResourceVisitor(
                    this.project, this);
            for (IPackageFragmentRoot root : roots) {
                root.getResource().accept(visitor, IResource.DEPTH_ONE,
                        false);
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        }
    }

    public void handle(ConnectionConfig cc) {
        addChild(new ConnectionNode(cc));
    }

    public IJavaProject getJavaProject() {
        return this.project;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.impl.AbstractNode#hasChildren()
     */
    @Override
    public boolean hasChildren() {
        return super.getState().hasChildren();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.impl.AbstractNode#dispose()
     */
    @Override
    public void dispose() {
        TreeContent[] children = getChildren();
        for (TreeContent child : children) {
            child.dispose();
        }
    }

}
