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
package org.seasar.dolteng.eclipse.viewer;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.seasar.dolteng.eclipse.action.ActionRegistry;
import org.seasar.dolteng.eclipse.model.TreeContentEventExecutor;

/**
 * @author taichi
 * 
 */
public class TreeContentUtil {

    public static void hookDoubleClickAction(final StructuredViewer viewer,
            final ActionRegistry registry) {
        viewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
                Object element = ((IStructuredSelection) event.getSelection())
                        .getFirstElement();
                if (element instanceof TreeContentEventExecutor) {
                    TreeContentEventExecutor tee = (TreeContentEventExecutor) element;
                    tee.doubleClick(registry);
                }
            }
        });
    }

    public static void hookTreeEvent(final AbstractTreeViewer viewer,
            final ActionRegistry registry) {
        viewer.addTreeListener(new ITreeViewerListener() {

            public void treeCollapsed(TreeExpansionEvent event) {
                Object element = event.getElement();
                if (element instanceof TreeContentEventExecutor) {
                    TreeContentEventExecutor tee = (TreeContentEventExecutor) element;
                    tee.collapsed(viewer, registry);
                }
            }

            public void treeExpanded(TreeExpansionEvent event) {
                Object element = event.getElement();
                if (element instanceof TreeContentEventExecutor) {
                    TreeContentEventExecutor tee = (TreeContentEventExecutor) element;
                    tee.expanded(viewer, registry);
                }
            }
        });
    }

}
