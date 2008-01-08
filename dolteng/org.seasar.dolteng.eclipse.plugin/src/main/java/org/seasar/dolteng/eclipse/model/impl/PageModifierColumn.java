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

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.seasar.dolteng.eclipse.model.PageMappingRow;
import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class PageModifierColumn extends ModifierColumn {

    private static final String NAME = ClassUtil
            .getShortClassName(PageModifierColumn.class);

    /**
     * @param table
     */
    public PageModifierColumn(Table table) {
        super(table);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.model.impl.ModifierColumn#getName()
     */
    @Override
    public String getName() {
        return NAME;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.ColumnDescriptor#getText(java.lang.Object)
     */
    @Override
    public String getText(Object element) {
        if (element instanceof PageMappingRow) {
            PageMappingRow row = (PageMappingRow) element;
            String txt = Modifier.toString(row.getPageModifiers());
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
    @Override
    public Image getImage(Object element) {
        if (element instanceof PageMappingRow) {
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
    @Override
    public Object getValue(Object element) {
        return new Integer(MODIFIER_LIST.indexOf(getText(element)));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.ui.eclipse.models.ColumnDescriptor#setValue(java.lang.Object,
     *      java.lang.Object)
     */
    @Override
    public void setValue(Object element, Object value) {
        if (element instanceof PageMappingRow && value instanceof Integer) {
            PageMappingRow row = (PageMappingRow) element;
            int index = ((Integer) value).intValue();
            row.setPageModifiers(MODIFIERS[index]);
        }
    }
}
