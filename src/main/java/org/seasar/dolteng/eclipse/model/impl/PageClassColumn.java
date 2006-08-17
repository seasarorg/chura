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
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.ColumnDescriptor;
import org.seasar.dolteng.eclipse.model.PageMappingRow;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.StringUtil;
import org.seasar.teeda.extension.ExtensionConstants;

/**
 * @author taichi
 * 
 */
public class PageClassColumn implements ColumnDescriptor {

    public static final Pattern multiItemRegx = Pattern.compile(".*("
            + ExtensionConstants.ITEMS_SUFFIX + "|Grid)$");

    private static final String[] BASIC_ITEMS = { "java.lang.String",
            "java.lang.Integer", "java.lang.Float", "int", "float", "long" };

    private static final String NAME = ClassUtil
            .getShortClassName(PageClassColumn.class);

    private ComboBoxCellEditor editor;

    private List basic;

    private List items;

    private ArrayList multiItemBase = new ArrayList();

    public PageClassColumn(final Table table, final List allOf,
            IJavaProject project) {
        super();
        this.editor = new ComboBoxCellEditor(table, BASIC_ITEMS);
        this.basic = Arrays.asList(BASIC_ITEMS);
        this.items = this.basic;
        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setText(Labels.COLUMN_JAVA_CLASS);
        column.setWidth(150);
        analyzeDto(project);
    }

    private void analyzeDto(IJavaProject project) {
        try {
            multiItemBase.add("java.util.List");
            IPackageFragmentRoot root = ProjectUtil
                    .getFirstSrcPackageFragmentRoot(project);
            DoltengProjectPreferences pref = DoltengCore
                    .getPreferences(project);
            if (root == null || pref == null) {
                return;
            }
            IPackageFragment fragment = root.getPackageFragment(pref
                    .getRawPreferences().getString(
                            Constants.PREF_DEFAULT_DTO_PACKAGE));
            ICompilationUnit[] classes = fragment.getCompilationUnits();
            for (int i = 0; i < classes.length; i++) {
                IType type = classes[i].findPrimaryType();
                if (type != null) {
                    multiItemBase.add(type.getElementName());
                }
            }
        } catch (Exception e) {
            DoltengCore.log(e);
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
            return row.getPageClassName();
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
            ArrayList list = (ArrayList) multiItemBase.clone();
            list.add(0, toDtoArrayName(fieldName));
            String[] ary = (String[]) list.toArray(new String[list.size()]);
            this.editor.setItems(ary);
            this.items = list;
        } else {
            this.editor.setItems(BASIC_ITEMS);
            this.items = basic;
        }
    }

    public static String toDtoArrayName(String fieldName) {
        int pos = fieldName.lastIndexOf(ExtensionConstants.ITEMS_SUFFIX);
        return StringUtil.capitalize(fieldName.substring(0, pos)) + "[]";
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
                row.setPageClassName(this.editor.getItems()[i.intValue()]);
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
