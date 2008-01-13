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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Item;
import org.seasar.dolteng.eclipse.model.ColumnDescriptor;
import org.seasar.framework.util.ArrayMap;

/**
 * @author taichi
 * 
 */
public class TableProvider extends LabelProvider implements
        ITableLabelProvider, ICellModifier {

    private ArrayMap columnDescs;

    private TableViewer viewer;

    public TableProvider(TableViewer viewer, ColumnDescriptor[] cds) {
        this.viewer = viewer;
        this.columnDescs = new ArrayMap(cds.length);
        List<String> keys = new ArrayList<String>(cds.length);
        List<CellEditor> editors = new ArrayList<CellEditor>(cds.length);
        for (ColumnDescriptor cd : cds) {
            String name = cd.getName();
            if (columnDescs.containsKey(name)) {
                name = name + "@" + System.identityHashCode(cd);
            }
            this.columnDescs.put(name, cd);
            keys.add(name);
            editors.add(cd.getCellEditor());
        }
        this.viewer.setColumnProperties(keys.toArray(new String[keys.size()]));
        this.viewer.setCellEditors(editors.toArray(new CellEditor[editors.size()]));
        this.viewer.setCellModifier(this);
    }

    protected ColumnDescriptor getDescriptor(String name) {
        return (ColumnDescriptor) this.columnDescs.get(name);
    }

    protected ColumnDescriptor getDescriptor(int index) {
        return (ColumnDescriptor) this.columnDescs.get(index);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
     *      int)
     */
    public Image getColumnImage(Object element, int columnIndex) {
        ColumnDescriptor cd = getDescriptor(columnIndex);
        return cd != null ? cd.getImage(element) : null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
     *      int)
     */
    public String getColumnText(Object element, int columnIndex) {
        ColumnDescriptor cd = getDescriptor(columnIndex);
        return cd != null ? cd.getText(element) : "";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object,
     *      java.lang.String)
     */
    public boolean canModify(Object element, String property) {
        ColumnDescriptor cd = getDescriptor(property);
        return cd != null ? cd.canModify() : false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object,
     *      java.lang.String)
     */
    public Object getValue(Object element, String property) {
        ColumnDescriptor cd = getDescriptor(property);
        return cd != null ? cd.getValue(element) : null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object,
     *      java.lang.String, java.lang.Object)
     */
    public void modify(Object element, String property, Object value) {
        if (element instanceof Item) {
            element = ((Item) element).getData();
        }
        ColumnDescriptor cd = getDescriptor(property);
        if (cd != null) {
            cd.setValue(element, value);
            this.viewer.update(element, new String[] { property });
        }
    }

}
