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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.progress.WorkbenchJob;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.action.ActionRegistry;
import org.seasar.dolteng.eclipse.action.FindChildrenAction;
import org.seasar.dolteng.eclipse.model.TreeContent;
import org.seasar.dolteng.eclipse.model.TreeContentState;

/**
 * @author taichi
 * 
 */
public abstract class AbstractNode extends AbstractLeaf {

    private Set<TreeContent> children = new HashSet<TreeContent>();

    public AbstractNode() {
    }

    /**
     * @param root
     * @param parent
     */
    public AbstractNode(TreeContent root, TreeContent parent) {
        super(root, parent);
    }

    /**
     * @param parent
     */
    public AbstractNode(TreeContent parent) {
        super(parent);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.impl.AbstractLeaf#getChildren()
     */
    @Override
    public TreeContent[] getChildren() {
        return this.children
                .toArray(new TreeContent[this.children.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.impl.AbstractLeaf#addChild(org.seasar.dolteng.ui.eclipse.models.TreeContent)
     */
    @Override
    public void addChild(TreeContent content) {
        content.setParent(this);
        content.setRoot(getRoot());
        this.children.add(content);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.impl.AbstractLeaf#removeChild(org.seasar.dolteng.ui.eclipse.models.TreeContent)
     */
    @Override
    public void removeChild(TreeContent content) {
        this.children.remove(content);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.impl.AbstractLeaf#hasChildren()
     */
    @Override
    public boolean hasChildren() {
        return 0 < this.children.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.impl.AbstractLeaf#clearChildren()
     */
    @Override
    public void clearChildren() {
        this.dispose();
        this.children.clear();
        updateState(TreeContentState.BEGIN);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.ContentEventExecutor#dispose()
     */
    public void dispose() {
        for (TreeContent child : children) {
            child.dispose();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.impl.AbstractLeaf#doubleClick(org.seasar.dolteng.ui.eclipse.actions.ActionRegistry)
     */
    @Override
    public void doubleClick(ActionRegistry registry) {
        expanded(null, registry);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.impl.AbstractLeaf#expanded(org.seasar.dolteng.ui.eclipse.actions.ActionRegistry)
     */
    @Override
    public void expanded(AbstractTreeViewer viewer, ActionRegistry registry) {
        Event event = new Event();
        event.data = this;
        getState().run(registry.find(FindChildrenAction.ID), event);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.impl.AbstractLeaf#collapsed(org.eclipse.jface.viewers.AbstractTreeViewer,
     *      org.seasar.dolteng.eclipse.action.ActionRegistry)
     */
    @Override
    public void collapsed(final AbstractTreeViewer viewer,
            ActionRegistry registry) {
        final QualifiedName qn = new QualifiedName(Constants.ID_PLUGIN,
                "clearkids");
        Job job = new WorkbenchJob("clear children") {
            @Override
            public boolean belongsTo(Object family) {
                if (family instanceof Job) {
                    Job job = (Job) family;
                    return AbstractNode.this == job.getProperty(qn);
                }
                return false;
            }

            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                // それなりに時間が経過しても折りたたみ状態なら、取得済みの子供を消す。
                if (viewer.getExpandedState(AbstractNode.this) == false
                        && hasChildren()) {
                    viewer.remove(getChildren());
                    clearChildren();
                    IJobManager manager = Job.getJobManager();
                    manager.cancel(this);
                }
                return Status.OK_STATUS;
            }
        };
        job.setProperty(qn, this);
        job.setProperty(IProgressConstants.KEEPONE_PROPERTY, Boolean.TRUE);
        job.schedule(6000L);
    }
}
