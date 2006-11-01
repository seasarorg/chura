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

import org.seasar.dolteng.eclipse.model.TreeContent;
import org.seasar.dolteng.eclipse.model.TreeContentState;
import org.seasar.dolteng.eclipse.preferences.ConnectionConfig;

/**
 * @author taichi
 * 
 */
public abstract class AbstractFactoryDependentNode extends AbstractNode {

    private TreeContentFactory factory;

    private ConnectionConfig config;

    protected AbstractFactoryDependentNode(ConnectionConfig config) {
        this.factory = new TreeContentFactory(config);
        this.config = config;
    }

    protected AbstractFactoryDependentNode(TreeContentFactory factory) {
        this.factory = factory;
    }

    public TreeContentFactory getFactory() {
        return factory;
    }

    protected abstract TreeContent[] createChild();

    public void findChildren() {
        TreeContent[] nodes = createChild();
        for (int i = 0; i < nodes.length; i++) {
            addChild(nodes[i]);
        }
        updateState(0 < nodes.length ? TreeContentState.SEARCHED
                : TreeContentState.EMPTY);
    }

    /**
     * @return Returns the config.
     */
    public ConnectionConfig getConfig() {
        return config;
    }

    /**
     * @param config
     *            The config to set.
     */
    public void setConfig(ConnectionConfig config) {
        this.config = config;
    }

}