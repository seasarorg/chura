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
package org.seasar.dolteng.eclipse.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.seasar.dolteng.eclipse.nls.Images;
import org.seasar.dolteng.eclipse.nls.Labels;

/**
 * @author taichi
 * 
 */
public class RefreshDatabaseViewAction extends Action {

    public static final String ID = RefreshDatabaseViewAction.class.getName();

    private TreeViewer viewer;

    /**
     * 
     */
    public RefreshDatabaseViewAction(TreeViewer viewer) {
        super();
        this.viewer = viewer;
        setId(ID);
        setText(Labels.REFRESH);
        setImageDescriptor(Images.REFRESH);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        Display disp = this.viewer.getControl().getDisplay();
        disp.asyncExec(new Runnable() {
            public void run() {
                viewer.expandToLevel(3);
                viewer.refresh(true);
            }
        });
    }

}