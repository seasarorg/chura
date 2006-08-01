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
package org.seasar.dolteng.eclipse.model.impl;

import java.util.regex.Pattern;

import javax.sql.XADataSource;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
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
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.dolteng.eclipse.preferences.impl.XADataSourceWrapper;
import org.seasar.dolteng.eclipse.util.JavaProjectClassLoader;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.S2ContainerUtil;

/**
 * @author taichi
 * 
 */
public class ProjectNode extends AbstractNode {

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
    public TreeContent getRoot() {
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.impl.AbstractLeaf#fillContextMenu(org.eclipse.jface.action.IMenuManager,
     *      org.seasar.dolteng.ui.eclipse.actions.ActionRegistry)
     */
    public void fillContextMenu(IMenuManager manager, ActionRegistry registry) {
        manager.add(registry.find(ConnectionConfigAction.ID));
        manager.add(new Separator());
        manager.add(registry.find(FindChildrenAction.ID));
    }

    public void findChildren() {
        DoltengProjectPreferences pref = DoltengCore
                .getPreferences(this.project);
        ConnectionConfig[] configs = pref.getAllOfConnectionConfig();
        for (int i = 0; i < configs.length; i++) {
            TreeContent tc = new ConnectionNode(configs[i]);
            addChild(tc);
        }

        loadFromProject();

        updateState(0 < configs.length ? TreeContentState.SEARCHED
                : TreeContentState.EMPTY);
    }

    protected void loadFromProject() {
        try {
            final JavaProjectClassLoader loader = new JavaProjectClassLoader(
                    this.project);
            final Pattern ptn = Pattern.compile(".*jdbc.dicon");
            IPackageFragmentRoot[] roots = ProjectUtil
                    .findSrcFragmentRoots(this.project);
            for (int i = 0; i < roots.length; i++) {
                roots[i].getResource().accept(new IResourceVisitor() {
                    public boolean visit(IResource resource)
                            throws CoreException {
                        if (resource instanceof IFile
                                && ptn.matcher(resource.getName()).matches()) {
                            String diconPath = resource.getName();
                            XADataSource[] sources = (XADataSource[]) S2ContainerUtil
                                    .loadComponents(loader, diconPath,
                                            XADataSource.class);
                            if (sources != null && 0 < sources.length) {
                                XADataSourceWrapper wrapper = new XADataSourceWrapper(
                                        diconPath, sources[0]);
                                addChild(new DiconConnectionNode(wrapper));
                            }
                        }
                        return false;
                    }
                }, IResource.DEPTH_ONE, false);
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        }
    }

    public IJavaProject getJavaProject() {
        return this.project;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.impl.AbstractNode#hasChildren()
     */
    public boolean hasChildren() {
        return super.getState().hasChildren();
    }
}
