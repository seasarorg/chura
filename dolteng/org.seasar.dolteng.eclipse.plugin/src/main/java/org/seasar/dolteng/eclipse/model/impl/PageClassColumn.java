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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TableColumn;
import org.seasar.dolteng.core.teeda.TeedaEmulator;
import org.seasar.dolteng.eclipse.model.ColumnDescriptor;
import org.seasar.dolteng.eclipse.model.PageMappingRow;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.dolteng.eclipse.viewer.ComboBoxDialogCellEditor;
import org.seasar.dolteng.eclipse.wizard.NewWebDtoWizard;
import org.seasar.dolteng.eclipse.wizard.PageMappingPage;
import org.seasar.framework.util.ArrayMap;
import org.seasar.framework.util.ArrayUtil;
import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
@SuppressWarnings("unchecked")
public class PageClassColumn implements ColumnDescriptor {

    private static final String[] BASIC_ITEMS = { "boolean", "int", "long",
            "short", "boolean[]", "int[]", "long[]", "short[]", "double",
            "float", "java.lang.Boolean", "java.math.BigDecimal",
            "java.lang.Double", "java.lang.Float", "java.lang.Integer",
            "java.lang.Long", "java.lang.Short", "java.lang.String",
            "java.util.Date", "java.lang.Boolean[]", "java.lang.BigDecimal[]",
            "java.lang.Integer[]", "java.lang.Long[]", "java.lang.Short[]",
            "java.lang.String[]" };

    private static final String NAME = ClassUtil
            .getShortClassName(PageClassColumn.class);

    private DtoCellEditor editor;

    private List<String> basic;

    private List<String> items;

    private Map<String, String> multiItemMap = new ArrayMap();

    private IFile resource;

    private PageMappingPage mappingPage;

    public PageClassColumn(TableViewer viewer, List<String> typeNames,
            IFile resource, PageMappingPage mappingPage) {
        super();
        this.editor = new DtoCellEditor(viewer);
        this.editor.setItems(BASIC_ITEMS);
        this.basic = Arrays.asList(BASIC_ITEMS);
        this.items = this.basic;
        TableColumn column = new TableColumn(viewer.getTable(), SWT.READ_ONLY);
        column.setText(Labels.COLUMN_JAVA_CLASS);
        column.setWidth(120);
        for (String s : typeNames) {
            multiItemMap.put(ClassUtil.getShortClassName(s), s);
        }
        this.resource = resource;
        this.mappingPage = mappingPage;
    }

    private class DtoCellEditor extends ComboBoxDialogCellEditor {
        private TableViewer viewer;

        public DtoCellEditor(TableViewer viewer) {
            super(viewer.getTable());
            this.viewer = viewer;
        }

        @Override
        protected Object openDialogBox(Control cellEditorWindow) {
            PageMappingRow row = (PageMappingRow) viewer.getTable()
                    .getSelection()[0].getData();
            String fieldName = row.getPageFieldName();
            if (TeedaEmulator.MAPPING_MULTI_ITEM.matcher(fieldName).matches()) {
                fieldName = fieldName.replaceAll("Items", "");
                NewWebDtoWizard wiz = new NewWebDtoWizard(resource,
                        mappingPage, fieldName);
                if (WorkbenchUtil.startWizard(wiz) == Window.OK) {
                    IType type = wiz.getCreatedType();
                    String fqName = type.getFullyQualifiedName() + "[]";
                    String shortName = ClassUtil.getShortClassName(fqName);
                    int i = PageClassColumn.this.items.indexOf(shortName);
                    if (i < 0) {
                        PageClassColumn.this.multiItemMap
                                .put(shortName, fqName);
                        PageClassColumn.this.items.add(shortName);
                        setItems((String[]) ArrayUtil.add(items, shortName));
                        Integer num = new Integer(PageClassColumn.this.items
                                .size() - 1);
                        doSetValue(num);
                        row.setPageClassName(fqName);
                        viewer.refresh(true);
                        return num;
                    }
                    return new Integer(i);
                }
            }
            return PageClassColumn.this.getValue(row);
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
                && TeedaEmulator.MAPPING_MULTI_ITEM.matcher(fieldName)
                        .matches()) {
            Set<String> set = multiItemMap.keySet();
            String[] ary = set.toArray(new String[set.size()]);
            this.editor.setItems(ary);
            this.items = new ArrayList<String>(set);
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
