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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IContentProvider;
import org.seasar.dolteng.eclipse.model.TreeContent;
import org.seasar.dolteng.eclipse.model.impl.ConnectionNode;
import org.seasar.dolteng.eclipse.model.impl.ProjectNode;
import org.seasar.dolteng.eclipse.nls.Images;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.util.SelectionUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.dolteng.eclipse.wigets.ConnectionDialog;

/**
 * @author taichi
 * 
 */
public class ConnectionConfigAction extends Action {

    public static final String ID = ConnectionConfigAction.class.getName();

    private AbstractTreeViewer viewer;

    public ConnectionConfigAction(AbstractTreeViewer viewer) {
        super();
        this.viewer = viewer;
        setId(ID);
        setText(Labels.ACTION_CONNECTION_CONFIG_ADD);
        setImageDescriptor(Images.ADD);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        Object elem = SelectionUtil.getCurrentSelection(this.viewer);
        if (elem instanceof TreeContent) {
            ConnectionDialog dialog = new ConnectionDialog(WorkbenchUtil
                    .getShell());
            TreeContent tc = (TreeContent) elem;
            ProjectNode pn = (ProjectNode) tc.getRoot();
            dialog.setDependentProject(pn.getJavaProject());

            TreeContent parent = tc.getParent();
            while (parent != null && tc != parent) {
                if (tc instanceof ConnectionNode) {
                    ConnectionNode cn = (ConnectionNode) tc;
                    dialog.setOldConfig(cn.getConfig());
                    break;
                }
                tc = parent;
                parent = tc.getParent();
            }

            if (IDialogConstants.OK_ID == dialog.open()) {
                IContentProvider cp = this.viewer.getContentProvider();
                cp
                        .inputChanged(this.viewer, null, dialog
                                .getDependentProject());
            }
        }
    }

}
