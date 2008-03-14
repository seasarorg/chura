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
package org.seasar.dolteng.eclipse.wizard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.widgets.Table;
import org.seasar.dolteng.core.entity.impl.BasicFieldMetaData;
import org.seasar.dolteng.core.teeda.TeedaEmulator;
import org.seasar.dolteng.eclipse.model.ColumnDescriptor;
import org.seasar.dolteng.eclipse.model.PageMappingRow;
import org.seasar.dolteng.eclipse.model.impl.BasicPageMappingRow;
import org.seasar.dolteng.eclipse.model.impl.DtoClassColumn;
import org.seasar.dolteng.eclipse.model.impl.IsThisGenerateColumn;
import org.seasar.dolteng.eclipse.model.impl.PageFieldNameColumn;
import org.seasar.dolteng.eclipse.model.impl.PageModifierColumn;
import org.seasar.dolteng.eclipse.model.impl.SrcClassColumn;
import org.seasar.dolteng.eclipse.model.impl.SrcFieldNameColumn;
import org.seasar.dolteng.eclipse.nls.Labels;

/**
 * @author taichi
 * 
 */
public class DtoMappingPage extends PageMappingPage {

    private static final String NAME = DtoMappingPage.class.getName();

    private PageMappingPage pageMapping;

    public DtoMappingPage(IWizard wizard, IFile resource,
            PageMappingPage pageMapping) {
        super(resource, NAME);
        setTitle(Labels.WIZARD_PAGE_DTO_FIELD_SELECTION);
        setDescription(Labels.WIZARD_PAGE_CREATION_DESCRIPTION);
        this.pageMapping = pageMapping;
    }

    @Override
    protected ColumnDescriptor[] createColumnDescs(Table table) {
        List<ColumnDescriptor> descs = new ArrayList<ColumnDescriptor>();
        descs.add(new IsThisGenerateColumn(table));
        descs.add(new PageModifierColumn(table));
        descs.add(new DtoClassColumn(table));
        descs.add(new PageFieldNameColumn(table, this));
        descs.add(new SrcClassColumn(table));
        descs.add(new SrcFieldNameColumn(table));
        return descs.toArray(new ColumnDescriptor[descs
                .size()]);
    }

    @Override
    protected void createRows() {
        for (PageMappingRow original : pageMapping.getMappingRows()) {
            if (TeedaEmulator.MAPPING_MULTI_ITEM.matcher(
                    original.getPageFieldName()).matches() == false) {
                PageMappingRow row = new BasicPageMappingRow(
                        new BasicFieldMetaData(), new BasicFieldMetaData());
                row.setThisGenerate(false);
                row.setPageModifiers(original.getPageModifiers());
                row.setPageClassName(original.getPageClassName());
                row.setPageFieldName(original.getPageFieldName());
                getMappingRows().add(row);
                getRowFieldMapping().put(original.getPageFieldName(), row);
            }
        }
        Collections.sort(getMappingRows());
    }

    @Override
    public void setVisible(boolean visible) {
        getControl().setVisible(visible);
    }

    public void reMapping() {
        Map<String, PageMappingRow> parentRows = pageMapping.getRowFieldMapping();
        for (Iterator i = getMappingRows().iterator(); i.hasNext();) {
            PageMappingRow myrow = (PageMappingRow) i.next();
            PageMappingRow parentRow = parentRows.get(myrow.getPageFieldName());
            if (myrow.isThisGenerate() && parentRow != null) {
                parentRow.setPageClassName(myrow.getPageClassName());
            }
        }
    }
}
