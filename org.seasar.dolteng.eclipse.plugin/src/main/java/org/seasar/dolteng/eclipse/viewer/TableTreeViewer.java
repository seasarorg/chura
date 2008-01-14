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

import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * @author taichi
 * 
 */
public class TableTreeViewer extends TreeViewer {

    public TableTreeViewer(Composite parent, TableTreeContentProvider provider) {
        this(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL, provider);
    }

    public TableTreeViewer(Composite parent, int style,
            TableTreeContentProvider provider) {
        super(parent, style);
        setContentProvider(provider);
        setLabelProvider(new TreeContentLabelProvider());
        setSorter(new ComparableViewerSorter());
        // Trick ...
        // AbstractLeafに実装されているequalsやhashCodeは、それぞれが属するNode内においてのみ、
        // 有効である様実装されている為。表示領域に対するイベントハンドリングでは、適切に動作しない為。
        setComparer(new IElementComparer() {
            public boolean equals(Object a, Object b) {
                return a == b;
            }

            public int hashCode(Object element) {
                return element.hashCode() ^ System.identityHashCode(element);
            }
        });

    }
}
