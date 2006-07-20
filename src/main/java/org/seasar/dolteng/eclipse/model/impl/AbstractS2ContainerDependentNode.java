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

import org.seasar.dolteng.core.dao.DatabaseMetaDataDao;
import org.seasar.dolteng.eclipse.model.TreeContent;
import org.seasar.dolteng.eclipse.preferences.ConnectionConfig;
import org.seasar.framework.container.S2Container;

/**
 * @author taichi
 * 
 */
public abstract class AbstractS2ContainerDependentNode extends AbstractNode {

    private S2Container container;

    private DatabaseMetaDataDao metaDataDao;

    private ConnectionConfig config;

    protected AbstractS2ContainerDependentNode() {

    }

    protected AbstractS2ContainerDependentNode(S2Container container,
            DatabaseMetaDataDao metaDataDao, ConnectionConfig config) {
        this.container = container;
        this.metaDataDao = metaDataDao;
        this.config = config;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.impl.AbstractNode#dispose()
     */
    public void dispose() {
        this.container.destroy();
        super.dispose();
    }

    protected TreeContent newChild(String name) {
        return (TreeContent) getContainer().getComponent(name);
    }

    public S2Container getContainer() {
        return this.container;
    }

    public void setContainer(S2Container container) {
        this.container = container;
    }

    public DatabaseMetaDataDao getMetaDataDao() {
        return this.metaDataDao;
    }

    public void setMetaDataDao(DatabaseMetaDataDao metaDataDao) {
        this.metaDataDao = metaDataDao;
    }

    public ConnectionConfig getConfig() {
        return this.config;
    }

    public void setConfig(ConnectionConfig config) {
        this.config = config;
    }
}
