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
package org.seasar.dolteng.eclipse.model.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.seasar.dolteng.eclipse.model.ColumnDescriptor;
import org.seasar.dolteng.eclipse.model.PageMappingRow;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.StringUtil;
import org.seasar.teeda.extension.ExtensionConstants;

/**
 * @author taichi
 * 
 */
public class PageClassColumn implements ColumnDescriptor {

    public static final Pattern multiItemRegx = Pattern.compile(".*("
            + ExtensionConstants.ITEMS_SUFFIX + "|Grid)$",
            Pattern.CASE_INSENSITIVE);

    private static final String[] BASIC_ITEMS = { "boolean", "double", "float",
            "int", "long", "short", "java.lang.Boolean",
            "java.math.BigDecimal", "java.lang.Double", "java.lang.Float",
            "java.lang.Integer", "java.lang.Long", "java.lang.Short",
            "java.lang.String", "java.util.Date" };

    private static final String NAME = ClassUtil
            .getShortClassName(PageClassColumn.class);

    private ComboBoxCellEditor editor;

    private List basic;

    private List items;

    private Map multiItemMap = new HashMap();

    public PageClassColumn(final Table table, final ArrayList typeNames) {
        super();
        this.editor = new ComboBoxCellEditor(table, BASIC_ITEMS);
        this.basic = Arrays.asList(BASIC_ITEMS);
        this.items = this.basic;
        TableColumn column = new TableColumn(table, SWT.READ_ONLY);
        column.setText(Labels.COLUMN_JAVA_CLASS);
        column.setWidth(150);
        for (Iterator i = typeNames.iterator(); i.hasNext();) {
            String s = i.next().toString();
            multiItemMap.put(ClassUtil.getShortClassName(s), s);
        }
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
        return this.editor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.ColumnDescriptor#getText(java.lang.Object)
     */
    public String getText(Object element) {
        if (element instanceof PageMappingRow) {
            PageMappingRow row = (PageMappingRow) element;
            String name = row.getPageClassName();
            return this.items == this.basic ? name : ClassUtil
                    .getShortClassName(name);
        }
        return "";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.ColumnDescriptor#getImage(java.lang.Object)
     */
    public Image getImage(Object element) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.ColumnDescriptor#getValue(java.lang.Object)
     */
    public Object getValue(Object element) {
        int i = this.items.indexOf(getText(element));
        if (i < 0) {
            processCombo((PageMappingRow) element);
        }
        return new Integer(this.items.indexOf(getText(element)));
    }

    private void processCombo(PageMappingRow row) {
        String fieldName = row.getPageFieldName();
        if (StringUtil.isEmpty(fieldName) == false
                && multiItemRegx.matcher(fieldName).matches()) {
            Set set = multiItemMap.keySet();
            String[] ary = (String[]) set.toArray(new String[set.size()]);
            this.editor.setItems(ary);
            this.items = new ArrayList(set);
        } else {
            this.editor.setItems(BASIC_ITEMS);
            this.items = basic;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.ColumnDescriptor#setValue(java.lang.Object,
     *      java.lang.Object)
     */
    public void setValue(Object element, Object value) {
        if (element instanceof PageMappingRow && value != null) {
            PageMappingRow row = (PageMappingRow) element;
            Integer i = (Integer) value;
            if (0 <= i.intValue()) {
                String name = this.editor.getItems()[i.intValue()];
                if (this.items != this.basic) {
                    name = multiItemMap.get(name).toString();
                }
                row.setPageClassName(name);
            }
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
