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
import org.seasar.dolteng.eclipse.action.ActionRegistry;
import org.seasar.dolteng.eclipse.action.FindChildrenAction;
import org.seasar.dolteng.eclipse.preferences.ConnectionConfig;

/**
 * @author taichi
 * 
 */
public class DiconConnectionNode extends ConnectionNode {

    /**
     * @param config
     */
    public DiconConnectionNode(ConnectionConfig config) {
        super(config);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.impl.ConnectionNode#fillContextMenu(org.eclipse.jface.action.IMenuManager,
     *      org.seasar.dolteng.eclipse.action.ActionRegistry)
     */
    @Override
    public void fillContextMenu(IMenuManager manager, ActionRegistry registry) {
        manager.add(registry.find(FindChildrenAction.ID));
    }

}
