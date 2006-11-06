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

import org.eclipse.core.resources.IProject;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.TreeContent;
import org.seasar.dolteng.eclipse.model.TreeContentState;
import org.seasar.dolteng.eclipse.operation.BootDbJob;
import org.seasar.dolteng.eclipse.preferences.ConnectionConfig;
import org.seasar.framework.exception.SQLRuntimeException;

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
        TreeContent[] nodes = handleChildCreation();
        for (int i = 0; i < nodes.length; i++) {
            addChild(nodes[i]);
        }
        updateState(0 < nodes.length ? TreeContentState.SEARCHED
                : TreeContentState.EMPTY);
    }

    protected TreeContent[] handleChildCreation() {
        TreeContent[] result = null;
        try {
            result = createChild();
        } catch (SQLRuntimeException e) {
            result = new TreeContent[0];
            ProjectNode pn = (ProjectNode) this.getRoot();
            IProject p = pn.getJavaProject().getProject();
            if (BootDbJob.enableFor(p)) {
                BootDbJob job = new BootDbJob(p);
                job.schedule();
            } else {
                DoltengCore.log(e);
            }
        }
        return result;
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
