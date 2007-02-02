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
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.seasar.dolteng.eclipse.model.ColumnDescriptor;
import org.seasar.dolteng.eclipse.model.impl.IsGenerateColumn;
import org.seasar.dolteng.eclipse.viewer.ComparableViewerSorter;
import org.seasar.dolteng.eclipse.viewer.TableProvider;

/**
 * @author taichi
 * 
 */
public class AddBindingWizardPage extends WizardPage {

    private TableViewer viewer;

    private List mappingRows;

    /**
     * @param pageName
     * @param title
     * @param titleImage
     */
    public AddBindingWizardPage(String pageName, String title,
            ImageDescriptor titleImage) {
        super(pageName, title, titleImage);
    }

    /**
     * @param pageName
     */
    public AddBindingWizardPage(String pageName) {
        super(pageName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);

        this.viewer = new TableViewer(composite, SWT.BORDER
                | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        Table table = viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new TableProvider(viewer,
                createColumnDescs(table)));
        viewer.setSorter(new ComparableViewerSorter());
        viewer.setInput(this.mappingRows);
    }

    private ColumnDescriptor[] createColumnDescs(Table table) {
        List<ColumnDescriptor> descs = new ArrayList<ColumnDescriptor>();
        descs.add(new IsGenerateColumn(table));
        // TODO see MxBindingMappingRow
        return (ColumnDescriptor[]) descs.toArray(new ColumnDescriptor[descs
                .size()]);
    }

    private String[] toItems() {
        List l = new ArrayList();
        // TODO 未実装
        return (String[]) l.toArray(new String[l.size()]);
    }

}
