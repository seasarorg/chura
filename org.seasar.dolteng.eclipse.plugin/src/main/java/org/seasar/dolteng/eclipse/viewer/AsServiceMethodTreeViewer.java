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

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * @author taichi
 * 
 */
public class AsServiceMethodTreeViewer extends CheckboxTreeViewer {
    // FIXME : 未完成

    public AsServiceMethodTreeViewer(Composite parent,
            AsServiceMethodTreeContentProvider provider) {
        this(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL, provider);
    }

    public AsServiceMethodTreeViewer(Composite parent, int style,
            AsServiceMethodTreeContentProvider provider) {
        super(parent, style);
        setContentProvider(provider);
        setLabelProvider(new TreeContentLabelProvider());
        addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                Object o = event.getElement();
                setSubtreeChecked(o, event.getChecked());
                Widget widget = internalExpand(o, false);
                if (widget instanceof TreeItem) {
                    TreeItem item = (TreeItem) widget;
                    TreeItem parent = item.getParentItem();
                    if (parent != null) {
                        if (parent.getChecked() == false && item.getChecked()) {
                            parent.setChecked(true);
                        }
                    }
                }
            }
        });
    }
}
