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

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.seasar.dolteng.eclipse.model.ColumnDescriptor;
import org.seasar.dolteng.eclipse.model.RegisterMocksRow;
import org.seasar.dolteng.eclipse.nls.Images;
import org.seasar.framework.util.ClassUtil;

/**
 * @author taichi
 * 
 */
public class MockRegisterColumn implements ColumnDescriptor {

    private static String NAME = ClassUtil
            .getShortClassName(MockRegisterColumn.class);

    private CellEditor editor;

    public MockRegisterColumn(Table table) {
        super();
        editor = new CheckboxCellEditor(table);
        TableColumn column = new TableColumn(table, SWT.CENTER);
        column.setImage(Images.CHECK);
        column.setWidth(20);
        column.setResizable(false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.ColumnDescriptor#getName()
     */
    public String getName() {
        return NAME;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.ColumnDescriptor#getCellEditor()
     */
    public CellEditor getCellEditor() {
        return editor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.ColumnDescriptor#getText(java.lang.Object)
     */
    public String getText(Object element) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.ColumnDescriptor#getImage(java.lang.Object)
     */
    public Image getImage(Object element) {
        if (element instanceof RegisterMocksRow) {
            RegisterMocksRow row = (RegisterMocksRow) element;
            return row.isRegister() ? Images.CHECKED : Images.UNCHECKED;
        }
        return Images.UNCHECKED;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.ColumnDescriptor#getValue(java.lang.Object)
     */
    public Object getValue(Object element) {
        if (element instanceof RegisterMocksRow) {
            RegisterMocksRow row = (RegisterMocksRow) element;
            return Boolean.valueOf(row.isRegister());
        }
        return Boolean.FALSE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.ColumnDescriptor#setValue(java.lang.Object,
     *      java.lang.Object)
     */
    public void setValue(Object element, Object value) {
        if (element instanceof RegisterMocksRow) {
            RegisterMocksRow row = (RegisterMocksRow) element;
            row.setRegister(((Boolean) value).booleanValue());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.ColumnDescriptor#canModify()
     */
    public boolean canModify() {
        return true;
    }

}
