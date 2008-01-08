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

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.graphics.Image;
import org.seasar.dolteng.core.entity.TableMetaData;
import org.seasar.dolteng.eclipse.action.ActionRegistry;
import org.seasar.dolteng.eclipse.action.NewEntityAction;
import org.seasar.dolteng.eclipse.action.NewScaffoldAction;
import org.seasar.dolteng.eclipse.model.TreeContent;
import org.seasar.dolteng.eclipse.nls.Images;

/**
 * @author taichi
 * 
 */
public class TableNode extends AbstractFactoryDependentNode {

    private TableMetaData meta;

    public TableNode(TreeContentFactory factory, TableMetaData meta) {
        super(factory);
        this.meta = meta;
    }

    public TableMetaData getMetaData() {
        return this.meta;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.ContentDescriptor#getText()
     */
    public String getText() {
        return this.meta.getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.ContentDescriptor#getImage()
     */
    public Image getImage() {
        return "VIEW".equalsIgnoreCase(meta.getTableType()) ? Images.VIEW
                : Images.TABLE;
    }

    @Override
    protected TreeContent[] createChild() {
        return getFactory().createNode(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.impl.SchemaNode#fillContextMenu(org.eclipse.jface.action.IMenuManager,
     *      org.seasar.dolteng.ui.eclipse.actions.ActionRegistry)
     */
    @Override
    public void fillContextMenu(IMenuManager manager, ActionRegistry registry) {
        super.fillContextMenu(manager, registry);
        manager.add(new Separator());
        manager.add(registry.find(NewEntityAction.ID));
        manager.add(registry.find(NewScaffoldAction.ID));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.impl.AbstractLeaf#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Object o) {
        if (o instanceof TableNode) {
            TableNode tn = (TableNode) o;
            return this.meta.compareTo(tn.meta);
        }
        return super.compareTo(o);
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

}
