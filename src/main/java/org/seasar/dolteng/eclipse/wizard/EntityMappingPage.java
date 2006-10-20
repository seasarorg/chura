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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.seasar.dolteng.core.entity.ColumnMetaData;
import org.seasar.dolteng.core.entity.FieldMetaData;
import org.seasar.dolteng.core.entity.impl.BasicFieldMetaData;
import org.seasar.dolteng.core.types.TypeMapping;
import org.seasar.dolteng.core.types.TypeMappingRegistry;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.ColumnDescriptor;
import org.seasar.dolteng.eclipse.model.EntityMappingRow;
import org.seasar.dolteng.eclipse.model.TreeContent;
import org.seasar.dolteng.eclipse.model.impl.BasicEntityMappingRow;
import org.seasar.dolteng.eclipse.model.impl.ColumnNameColumn;
import org.seasar.dolteng.eclipse.model.impl.ColumnNode;
import org.seasar.dolteng.eclipse.model.impl.FieldNameColumn;
import org.seasar.dolteng.eclipse.model.impl.JavaClassColumn;
import org.seasar.dolteng.eclipse.model.impl.ModifierColumn;
import org.seasar.dolteng.eclipse.model.impl.SqlTypeColumn;
import org.seasar.dolteng.eclipse.model.impl.TableNode;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.viewer.ComparableViewerSorter;
import org.seasar.dolteng.eclipse.viewer.TableProvider;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class EntityMappingPage extends WizardPage {

    private TableViewer viewer;

    private TableNode currentSelection;

    private List mappingRows;

    public EntityMappingPage(TableNode currentSelection) {
        super(Labels.WIZARD_PAGE_ENTITY_FIELD_SELECTION);
        setTitle(Labels.WIZARD_PAGE_ENTITY_FIELD_SELECTION);
        setDescription(Labels.WIZARD_ENTITY_CREATION_DESCRIPTION);
        this.currentSelection = currentSelection;
        this.mappingRows = new ArrayList();
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

        Label label = new Label(composite, SWT.LEFT | SWT.WRAP);
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        label.setText(Labels.WIZARD_PAGE_ENTITY_TREE_LABEL);

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

        gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
                | GridData.GRAB_VERTICAL);
        gd.heightHint = 180;
        gd.horizontalSpan = 2;
        table.setLayoutData(gd);

        Label spacer = new Label(composite, SWT.NONE);
        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalAlignment = GridData.BEGINNING;
        gd.heightHint = 4;
        gd.horizontalSpan = 2;
        spacer.setLayoutData(gd);

        setControl(composite);
    }

    private ColumnDescriptor[] createColumnDescs(Table table) {
        List descs = new ArrayList();
        descs.add(new SqlTypeColumn(table));
        descs.add(new ColumnNameColumn(table));
        descs.add(new ModifierColumn(table));
        descs.add(new JavaClassColumn(table, toItems()));
        descs.add(new FieldNameColumn(table));
        return (ColumnDescriptor[]) descs.toArray(new ColumnDescriptor[descs
                .size()]);
    }

    private String[] toItems() {
        List l = new ArrayList();
        TypeMappingRegistry registry = DoltengCore.getTypeMappingRegistry();
        TypeMapping[] types = registry.findAllTypes();
        for (int i = 0; i < types.length; i++) {
            l.add(types[i].getJavaClassName());
        }
        return (String[]) l.toArray(new String[l.size()]);
    }

    /**
     * @return
     */
    public Object createRows() {
        this.currentSelection.findChildren();
        TableNode table = this.currentSelection;
        TypeMappingRegistry registry = DoltengCore.getTypeMappingRegistry();
        TreeContent[] children = table.getChildren();
        for (int i = 0; i < children.length; i++) {
            ColumnNode content = (ColumnNode) children[i];
            FieldMetaData field = new BasicFieldMetaData();
            setUpFieldMetaData(registry, content, field);
            EntityMappingRow row = new BasicEntityMappingRow(content
                    .getColumnMetaData(), field);
            this.mappingRows.add(row);
        }
        Collections.sort(this.mappingRows);
        return this.mappingRows;
    }

    private void setUpFieldMetaData(TypeMappingRegistry registry,
            ColumnNode node, FieldMetaData field) {
        ColumnMetaData meta = node.getColumnMetaData();
        TypeMapping mapping = registry.toJavaClass(meta);
        field.setModifiers(Modifier.PUBLIC);
        field.setDeclaringClassName(mapping.getJavaClassName());
        field.setName(convertText(meta.getName()));
    }

    public static String convertText(String name) {
        String[] ary = name.toLowerCase().split("_");
        StringBuffer stb = new StringBuffer();
        stb.append(ary[0]);
        for (int i = 1; i < ary.length; i++) {
            stb.append(StringUtil.capitalize(ary[i]));
        }
        return stb.toString();
    }

    public List getMappingRows() {
        return this.mappingRows;
    }
}