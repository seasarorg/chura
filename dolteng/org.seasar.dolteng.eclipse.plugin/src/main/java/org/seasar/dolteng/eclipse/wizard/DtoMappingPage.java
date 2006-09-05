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
package org.seasar.dolteng.eclipse.wizard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Table;
import org.seasar.dolteng.core.entity.FieldMetaData;
import org.seasar.dolteng.core.entity.impl.BasicFieldMetaData;
import org.seasar.dolteng.eclipse.model.ColumnDescriptor;
import org.seasar.dolteng.eclipse.model.impl.BasicPageMappingRow;
import org.seasar.dolteng.eclipse.model.impl.DtoClassColumn;
import org.seasar.dolteng.eclipse.model.impl.IsThisGenerateColumn;
import org.seasar.dolteng.eclipse.model.impl.PageClassColumn;
import org.seasar.dolteng.eclipse.model.impl.PageFieldNameColumn;
import org.seasar.dolteng.eclipse.model.impl.PageModifierColumn;
import org.seasar.dolteng.eclipse.model.impl.SrcClassColumn;
import org.seasar.dolteng.eclipse.model.impl.SrcFieldNameColumn;

/**
 * @author taichi
 * 
 */
public class DtoMappingPage extends PageMappingPage {

    private static final String NAME = DtoMappingPage.class.getName();

    public DtoMappingPage(IFile resource) {
        super(resource, NAME);
    }

    protected ColumnDescriptor[] createColumnDescs(Table table) {
        List descs = new ArrayList();
        descs.add(new IsThisGenerateColumn(table));
        descs.add(new PageModifierColumn(table));
        descs.add(new DtoClassColumn(table));
        descs.add(new PageFieldNameColumn(table));
        descs.add(new SrcClassColumn(table));
        descs.add(new SrcFieldNameColumn(table));
        return (ColumnDescriptor[]) descs.toArray(new ColumnDescriptor[descs
                .size()]);
    }

    protected void createRows() {
        // TODO PageMappingPageの型を決めた結果を引っ張ってくるし、こっちで決めた型をPageMappingPageに反映する。
        analyzer.analyze();
        Map pageFields = analyzer.getPageFields();
        for (Iterator i = pageFields.values().iterator(); i.hasNext();) {
            FieldMetaData meta = (FieldMetaData) i.next();
            if (PageClassColumn.multiItemRegx.matcher(meta.getName()).matches() == false) {
                BasicPageMappingRow row = new BasicPageMappingRow(
                        new BasicFieldMetaData(), meta);
                row.setThisGenerate(false);
                getMappingRows().add(row);
                getRowFieldMapping().put(meta.getName(), row);
            }
        }
        Collections.sort(getMappingRows());
    }

    public void setVisible(boolean visible) {
        getControl().setVisible(visible);
    }

}
