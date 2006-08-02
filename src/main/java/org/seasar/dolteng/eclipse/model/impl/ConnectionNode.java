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
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.graphics.Image;
import org.seasar.dolteng.core.dao.DatabaseMetaDataDao;
import org.seasar.dolteng.core.entity.TableMetaData;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.action.ActionRegistry;
import org.seasar.dolteng.eclipse.action.ConnectionConfigAction;
import org.seasar.dolteng.eclipse.action.DeleteConnectionConfigAction;
import org.seasar.dolteng.eclipse.action.FindChildrenAction;
import org.seasar.dolteng.eclipse.model.TreeContent;
import org.seasar.dolteng.eclipse.model.TreeContentState;
import org.seasar.dolteng.eclipse.nls.Images;
import org.seasar.dolteng.eclipse.preferences.ConnectionConfig;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory;

/**
 * @author taichi
 * 
 */
public class ConnectionNode extends AbstractS2ContainerDependentNode {

    public static String COMPONENT_NAME = "connection";

    public ConnectionNode(ConnectionConfig config) {
        S2Container container = S2ContainerFactory.create("jdbc.dicon");
        container.register(config);
        container.init();
        setConfig(config);
        setContainer(container);
        setMetaDataDao((DatabaseMetaDataDao) container
                .getComponent(DatabaseMetaDataDao.class));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.impl.AbstractLeaf#fillContextMenu(org.eclipse.jface.action.IMenuManager,
     *      org.seasar.dolteng.ui.eclipse.actions.ActionRegistry)
     */
    public void fillContextMenu(IMenuManager manager, ActionRegistry registry) {
        manager.add(registry.find(ConnectionConfigAction.ID));
        manager.add(registry.find(DeleteConnectionConfigAction.ID));
        manager.add(new Separator());
        manager.add(registry.find(FindChildrenAction.ID));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.impl.AbstractNode#hasChildren()
     */
    public boolean hasChildren() {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.impl.AbstractLeaf#findChildren()
     */
    public void findChildren() {
        String[] schemas = getMetaDataDao().getSchemas();
        if (0 < schemas.length) {
            for (int i = 0; i < schemas.length; i++) {
                SchemaNode tc = (SchemaNode) newChild(SchemaNode.COMPONENT_NAME);
                tc.initialize(schemas[i]);
                addChild(tc);
            }
            updateState(TreeContentState.SEARCHED);
        } else { // スキーマの取れないDBなら、テーブルを直接取りにいく。
            TableMetaData[] metas = getMetaDataDao().getTables("%",
                    getConfig().getTableTypes());
            for (int i = 0; i < metas.length; i++) {
                TableNode tc = (TableNode) newChild(TableNode.COMPONENT_NAME);
                tc.initialize(metas[i]);
                addChild(tc);
            }
            updateState(0 < metas.length ? TreeContentState.SEARCHED
                    : TreeContentState.EMPTY);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.ContentDescriptor#getText()
     */
    public String getText() {
        return getConfig().getName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.ContentDescriptor#getImage()
     */
    public Image getImage() {
        return Images.CONNECTION;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.impl.AbstractNode#removeChild(org.seasar.dolteng.eclipse.model.TreeContent)
     */
    public void removeChild(TreeContent content) {
        super.removeChild(content);
        try {
            ProjectNode node = (ProjectNode) getRoot();
            DoltengProjectPreferences pref = DoltengCore.getPreferences(node
                    .getJavaProject());
            pref.getRawPreferences().save();
        } catch (Exception e) {
            DoltengCore.log(e);
        }
    }

}
