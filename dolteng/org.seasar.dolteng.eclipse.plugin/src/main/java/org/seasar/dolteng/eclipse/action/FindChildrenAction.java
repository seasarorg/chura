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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.TreeContent;
import org.seasar.dolteng.eclipse.model.impl.BasicNode;
import org.seasar.dolteng.eclipse.nls.Images;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.util.SelectionUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.framework.exception.SQLRuntimeException;

/**
 * @author taichi
 * 
 */
public class FindChildrenAction extends Action {

    public static final String ID = FindChildrenAction.class.getName();

    private AbstractTreeViewer viewer;

    public FindChildrenAction(AbstractTreeViewer viewer) {
        this.viewer = viewer;
        setId(ID);
        setText(Labels.ACTION_FIND_CHILDREN);
        setImageDescriptor(Images.REFRESH);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        this.execute(null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
     */
    @Override
    public void runWithEvent(Event event) {
        this.execute(event.data);
    }

    protected void execute(Object element) {
        if (element == null) {
            element = SelectionUtil.getCurrentSelection(this.viewer);
        }
        if (element instanceof TreeContent) {
            TreeContent tc = (TreeContent) element;
            tc.clearChildren();
            tc.addChild(new BasicNode(Labels.NODE_FINDING, Images.DOTS));
            this.viewer.refresh(tc);
            this.viewer.expandToLevel(tc, 2);

            Display disp = this.viewer.getControl().getDisplay();
            disp.asyncExec(new FindChildrenThread(this.viewer, tc));
        }
    }

    private class FindChildrenThread implements Runnable {

        private AbstractTreeViewer viewer;

        private TreeContent tc;

        public FindChildrenThread(AbstractTreeViewer viewer, TreeContent tc) {
            this.viewer = viewer;
            this.tc = tc;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Runnable#run()
         */
        public void run() {
            this.tc.clearChildren();
            try {
                this.tc.findChildren();
            } catch (SQLRuntimeException e) {
                DoltengCore.log(e);
                WorkbenchUtil.showMessage(e.getMessage(), MessageDialog.ERROR);
            } finally {
                this.viewer.refresh(tc, true);
            }
        }
    }

}
