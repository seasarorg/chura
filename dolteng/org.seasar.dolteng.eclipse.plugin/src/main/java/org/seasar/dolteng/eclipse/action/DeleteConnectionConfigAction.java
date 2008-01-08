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
package org.seasar.dolteng.eclipse.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredViewer;
import org.seasar.dolteng.eclipse.model.TreeContent;
import org.seasar.dolteng.eclipse.model.impl.ConnectionNode;
import org.seasar.dolteng.eclipse.nls.Images;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.util.SelectionUtil;

/**
 * @author taichi
 * 
 */
public class DeleteConnectionConfigAction extends Action {

    public static final String ID = DeleteConnectionConfigAction.class
            .getName();

    private StructuredViewer viewer;

    public DeleteConnectionConfigAction(StructuredViewer viewer) {
        this.viewer = viewer;
        setId(ID);
        setText(Labels.ACTION_CONNECTION_CONFIG_DELETE);
        setImageDescriptor(Images.DELETE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        Object element = SelectionUtil.getCurrentSelection(this.viewer);
        if (element instanceof ConnectionNode) {
            ConnectionNode tc = (ConnectionNode) element;
            TreeContent parent = tc.getParent();
            parent.removeChild(tc);
            this.viewer.refresh(parent);
        }
    }

}
