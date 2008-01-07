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
package org.seasar.dolteng.eclipse.model;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.graphics.Image;

public interface ColumnDescriptor {
    public String getName();

    public CellEditor getCellEditor();

    public String getText(Object element);

    public Image getImage(Object element);

    public Object getValue(Object element);

    public void setValue(Object element, Object value);

    public boolean canModify();
}