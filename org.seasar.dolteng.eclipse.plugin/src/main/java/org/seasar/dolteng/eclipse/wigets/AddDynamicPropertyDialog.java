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
package org.seasar.dolteng.eclipse.wigets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import jp.aonir.fuzzyxml.FuzzyXMLAttribute;
import jp.aonir.fuzzyxml.FuzzyXMLElement;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.seasar.dolteng.eclipse.model.ColumnDescriptor;
import org.seasar.dolteng.eclipse.model.impl.DynamicPropertyIsCreateColumn;
import org.seasar.dolteng.eclipse.model.impl.DynamicPropertyNameColumn;
import org.seasar.dolteng.eclipse.model.impl.DynamicPropertyValueColumn;
import org.seasar.dolteng.eclipse.model.impl.FuzzyXmlBasedDinamicPropertyRow;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.viewer.ComparableViewerSorter;
import org.seasar.dolteng.eclipse.viewer.TableProvider;

/**
 * @author taichi
 * 
 */
public class AddDynamicPropertyDialog extends TitleAreaDialog {

    private FuzzyXMLElement element;

    private int editorOffset;

    private int length;

    private TableViewer viewer;

    private FuzzyXMLAttribute[] selected;

    /**
     * @param parentShell
     */
    public AddDynamicPropertyDialog(Shell parentShell) {
        super(parentShell);
    }

    public void setCurrentElement(FuzzyXMLElement element) {
        this.element = element;
    }

    public void setEditorOffset(int offset, int length) {
        this.editorOffset = offset;
        this.length = length;
    }

    public FuzzyXMLAttribute[] getSelectedAttributes() {
        return selected;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = createMainLayout((Composite) super
                .createDialogArea(parent));
        setTitle(Labels.ADD_DYNAMIC_PROPERTY_SELECT_ATTRIBUTE);

        this.viewer = new TableViewer(composite, SWT.BORDER
                | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        Table table = viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new TableProvider(viewer,
                createColumnDescs(table)));
        viewer.setSorter(new ComparableViewerSorter());
        viewer.setInput(createInput());

        return composite;
    }

    private Composite createMainLayout(Composite rootComposite) {
        Composite composite = new Composite(rootComposite, SWT.NONE);
        composite.setLayout(new FillLayout());

        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        return composite;
    }

    protected ColumnDescriptor[] createColumnDescs(Table table) {
        List<ColumnDescriptor> descs = new ArrayList<ColumnDescriptor>();
        descs.add(new DynamicPropertyIsCreateColumn(table));
        descs.add(new DynamicPropertyNameColumn(table));
        descs.add(new DynamicPropertyValueColumn(table));
        return descs.toArray(new ColumnDescriptor[descs.size()]);

    }

    private List createInput() {
        List<FuzzyXmlBasedDinamicPropertyRow> result = new ArrayList<FuzzyXmlBasedDinamicPropertyRow>();
        for (FuzzyXMLAttribute attr : element.getAttributes()) {
            if (attr.getName().equalsIgnoreCase("id") == false) {
                FuzzyXmlBasedDinamicPropertyRow row = new FuzzyXmlBasedDinamicPropertyRow(
                        attr);
                row
                        .setCreate((attr.getOffset() <= editorOffset && editorOffset <= attr
                                .getOffset()
                                + attr.getLength())
                                || (attr.getOffset() <= editorOffset + length && editorOffset
                                        + length <= attr.getOffset()
                                        + attr.getLength()));
                result.add(row);
            }
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void okPressed() {
        List result = new ArrayList();
        List rows = (List) viewer.getInput();
        for (Iterator i = rows.iterator(); i.hasNext();) {
            FuzzyXmlBasedDinamicPropertyRow row = (FuzzyXmlBasedDinamicPropertyRow) i
                    .next();
            if (row.isCreate()) {
                result.add(row.getAttribute());
            }
        }
        selected = (FuzzyXMLAttribute[]) result
                .toArray(new FuzzyXMLAttribute[result.size()]);
        super.okPressed();
    }

}
