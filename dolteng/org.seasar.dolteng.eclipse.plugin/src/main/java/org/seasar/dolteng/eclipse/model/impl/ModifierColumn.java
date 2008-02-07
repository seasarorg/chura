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

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.seasar.dolteng.eclipse.model.ColumnDescriptor;
import org.seasar.dolteng.eclipse.model.EntityMappingRow;
import org.seasar.dolteng.eclipse.nls.Images;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class ModifierColumn implements ColumnDescriptor {

    private static final String NAME = ClassUtil
            .getShortClassName(ModifierColumn.class);

    protected static final String[] MODIFIER_ARRAY = { "public", "protected",
            "default", "private" };

    protected static final List MODIFIER_LIST = Arrays.asList(MODIFIER_ARRAY);

    protected static final int[] MODIFIERS = new int[] { Modifier.PUBLIC,
            Modifier.PROTECTED, 0, Modifier.PRIVATE };

    protected static final Image[] MODIFIER_ICONS = new Image[] {
            Images.PUBLIC_CO, Images.PROTECTED_CO, Images.DEFAULT_CO,
            Images.PRIVATE_CO };

    private CellEditor editor;

    public ModifierColumn(Table table) {
        this.editor = new ComboBoxCellEditor(table, MODIFIER_ARRAY);
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setText(Labels.COLUMN_MODIFIER);
        column.setWidth(70);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.ColumnDescriptor#getName()
     */
    public String getName() {
        return NAME;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.ColumnDescriptor#getCellEditor()
     */
    public CellEditor getCellEditor() {
        return this.editor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.ColumnDescriptor#getText(java.lang.Object)
     */
    public String getText(Object element) {
        if (element instanceof EntityMappingRow) {
            EntityMappingRow row = (EntityMappingRow) element;
            String txt = Modifier.toString(row.getJavaModifiers());
            if (StringUtil.isEmpty(txt)) {
                txt = MODIFIER_ARRAY[2];
            }
            return txt;
        }
        return "";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.ColumnDescriptor#getImage(java.lang.Object)
     */
    public Image getImage(Object element) {
        if (element instanceof EntityMappingRow) {
            int index = 2; // DEFAULTアイコン
            String txt = getText(element);
            if (StringUtil.isEmpty(txt) == false) {
                index = MODIFIER_LIST.indexOf(txt);
            }
            return MODIFIER_ICONS[index];
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.ColumnDescriptor#getValue(java.lang.Object)
     */
    public Object getValue(Object element) {
        return new Integer(MODIFIER_LIST.indexOf(getText(element)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.ColumnDescriptor#setValue(java.lang.Object,
     *      java.lang.Object)
     */
    public void setValue(Object element, Object value) {
        if (element instanceof EntityMappingRow && value instanceof Integer) {
            EntityMappingRow row = (EntityMappingRow) element;
            int index = ((Integer) value).intValue();
            row.setJavaModifiers(MODIFIERS[index]);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.ColumnDescriptor#canModify()
     */
    public boolean canModify() {
        return true;
    }

}
