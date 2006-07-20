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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.swt.widgets.Event;
import org.seasar.dolteng.eclipse.action.ActionRegistry;
import org.seasar.dolteng.eclipse.action.FindChildrenAction;
import org.seasar.dolteng.eclipse.model.TreeContent;
import org.seasar.dolteng.eclipse.model.TreeContentEventExecutor;
import org.seasar.dolteng.eclipse.model.TreeContentState;

/**
 * @author taichi
 * 
 */
public abstract class AbstractNode extends AbstractLeaf {

    private Set children = new HashSet();

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
    public TreeContent[] getChildren() {
        return (TreeContent[]) this.children
                .toArray(new TreeContent[this.children.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.impl.AbstractLeaf#addChild(org.seasar.dolteng.ui.eclipse.models.TreeContent)
     */
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
    public void removeChild(TreeContent content) {
        this.children.remove(content);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.impl.AbstractLeaf#hasChildren()
     */
    public boolean hasChildren() {
        return 0 < this.children.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.impl.AbstractLeaf#clearChildren()
     */
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
        for (Iterator i = children.iterator(); i.hasNext();) {
            TreeContentEventExecutor tc = (TreeContentEventExecutor) i.next();
            tc.dispose();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.impl.AbstractLeaf#doubleClick(org.seasar.dolteng.ui.eclipse.actions.ActionRegistry)
     */
    public void doubleClick(ActionRegistry registry) {
        expanded(registry);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.impl.AbstractLeaf#expanded(org.seasar.dolteng.ui.eclipse.actions.ActionRegistry)
     */
    public void expanded(ActionRegistry registry) {
        Event event = new Event();
        event.data = this;
        getState().run(registry.find(FindChildrenAction.ID), event);
    }

}
