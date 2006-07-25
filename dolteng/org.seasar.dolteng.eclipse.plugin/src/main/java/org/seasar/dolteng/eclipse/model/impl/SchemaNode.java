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

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.graphics.Image;
import org.seasar.dolteng.core.dao.DatabaseMetaDataDao;
import org.seasar.dolteng.core.entity.TableMetaData;
import org.seasar.dolteng.eclipse.action.ActionRegistry;
import org.seasar.dolteng.eclipse.action.FindChildrenAction;
import org.seasar.dolteng.eclipse.model.TreeContentState;
import org.seasar.dolteng.eclipse.nls.Images;
import org.seasar.dolteng.eclipse.preferences.ConnectionConfig;
import org.seasar.framework.container.S2Container;

/**
 * @author taichi
 * 
 */
public class SchemaNode extends AbstractS2ContainerDependentNode {

    public static String COMPONENT_NAME = "schema";

    private String name;

    /**
     * @param container
     * @param metaDataDao
     * @param config
     */
    public SchemaNode(S2Container container, DatabaseMetaDataDao metaDataDao,
            ConnectionConfig config) {
        super(container, metaDataDao, config);
    }

    public void initialize(String name) {
        this.name = name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.ContentDescriptor#getText()
     */
    public String getText() {
        return this.name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.ContentDescriptor#getImage()
     */
    public Image getImage() {
        return Images.SCHEMA;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.impl.AbstractLeaf#fillContextMenu(org.eclipse.jface.action.IMenuManager,
     *      org.seasar.dolteng.ui.eclipse.actions.ActionRegistry)
     */
    public void fillContextMenu(IMenuManager manager, ActionRegistry registry) {
        manager.add(registry.find(FindChildrenAction.ID));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.impl.AbstractLeaf#findChildren()
     */
    public void findChildren() {
        TableMetaData[] metas = getMetaDataDao().getTables(this.name,
                getConfig().getTableTypes());
        for (int i = 0; i < metas.length; i++) {
            TableNode tc = (TableNode) newChild(TableNode.COMPONENT_NAME);
            tc.initialize(metas[i]);
            addChild(tc);
        }
        updateState(0 < metas.length ? TreeContentState.SEARCHED
                : TreeContentState.EMPTY);
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