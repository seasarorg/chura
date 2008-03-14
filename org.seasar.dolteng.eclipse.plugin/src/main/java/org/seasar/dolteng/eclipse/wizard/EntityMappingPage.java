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
import org.seasar.dolteng.eclipse.model.impl.ProjectNode;
import org.seasar.dolteng.eclipse.model.impl.SqlTypeColumn;
import org.seasar.dolteng.eclipse.model.impl.TableNode;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.util.NameConverter;
import org.seasar.dolteng.eclipse.viewer.ComparableViewerSorter;
import org.seasar.dolteng.eclipse.viewer.TableProvider;
import org.seasar.dolteng.eclipse.wigets.ModifierGroup;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class EntityMappingPage extends WizardPage implements
        ModifierGroup.ModifierSelectionListener {

    private static final String NAME = PageMappingPage.class.getName();

    private static final String CONFIG_USE_PUBLIC_FIELD = "usePublicField";

    private TableViewer viewer;

    private TableNode currentSelection;

    private List<EntityMappingRow> mappingRows;

    private boolean canSelectPublicField;

    private boolean usePublicField = false;

    private DoltengPreferences pref;

    public EntityMappingPage(TableNode currentSelection,
            boolean canSelectPublicField) {
        super(Labels.WIZARD_PAGE_ENTITY_FIELD_SELECTION);
        setTitle(Labels.WIZARD_PAGE_ENTITY_FIELD_SELECTION);
        setDescription(Labels.WIZARD_ENTITY_CREATION_DESCRIPTION);
        this.currentSelection = currentSelection;
        this.mappingRows = new ArrayList<EntityMappingRow>();

        ProjectNode pn = (ProjectNode) currentSelection.getRoot();
        pref = DoltengCore.getPreferences(pn.getJavaProject());
        this.canSelectPublicField = canSelectPublicField;
        if (pref != null && this.canSelectPublicField) {
            this.usePublicField = pref.isUsePublicField();
        }
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

        if (this.canSelectPublicField) {
            createPartOfPublicField(composite);
        }

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

    private void createPartOfPublicField(Composite composite) {
        ModifierGroup mc = new ModifierGroup(composite, SWT.NONE,
                usePublicField);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        mc.setLayoutData(gd);
        mc.add(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.wigets.ModifierComposite.ModifierSelectionListener#privateSelected()
     */
    public void privateSelected() {
        usePublicField = false;
        setConfigUsePublicField(usePublicField);
        // TODO : Accessor Modifierの列をenable若しくはvisible
    }

    public boolean getUsePublicField() {
        return usePublicField;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.wigets.ModifierComposite.ModifierSelectionListener#publicSelected()
     */
    public void publicSelected() {
        usePublicField = true;
        setConfigUsePublicField(usePublicField);
        // TODO : Accessor Modifierの列をdisable若しくはinvisible
    }

    protected void setConfigUsePublicField(boolean use) {
        if (pref != null) {
            pref.setUsePublicField(use);
        }
    }

    private ColumnDescriptor[] createColumnDescs(Table table) {
        List<ColumnDescriptor> descs = new ArrayList<ColumnDescriptor>();
        descs.add(new SqlTypeColumn(table));
        descs.add(new ColumnNameColumn(table));
        descs.add(new ModifierColumn(table));
        descs.add(new JavaClassColumn(table, toItems()));
        descs.add(new FieldNameColumn(table));
        return descs.toArray(new ColumnDescriptor[descs
                .size()]);
    }

    private String[] toItems() {
        List<String> l = new ArrayList<String>();
        TableNode table = this.currentSelection;
        ProjectNode pn = (ProjectNode) table.getRoot();
        TypeMappingRegistry registry = DoltengCore.getTypeMappingRegistry(pn
                .getJavaProject());
        TypeMapping[] types = registry.findAllTypes();
        for (TypeMapping type : types) {
            l.add(type.getJavaClassName());
        }
        return l.toArray(new String[l.size()]);
    }

    public Object createRows() {
        this.currentSelection.findChildren();
        TableNode table = this.currentSelection;
        ProjectNode pn = (ProjectNode) table.getRoot();
        TypeMappingRegistry registry = DoltengCore.getTypeMappingRegistry(pn
                .getJavaProject());
        TreeContent[] children = table.getChildren();
        for (TreeContent child : children) {
            ColumnNode content = (ColumnNode) child;
            FieldMetaData field = new BasicFieldMetaData();
            setUpFieldMetaData(registry, content, field);
            EntityMappingRow row = new BasicEntityMappingRow(content
                    .getColumnMetaData(), field, registry);
            this.mappingRows.add(row);
        }
        Collections.sort(mappingRows);
        return this.mappingRows;
    }

    private void setUpFieldMetaData(TypeMappingRegistry registry,
            ColumnNode node, FieldMetaData field) {
        ColumnMetaData meta = node.getColumnMetaData();
        TypeMapping mapping = registry.toJavaClass(meta);
        field.setModifiers(Modifier.PUBLIC);
        field.setDeclaringClassName(mapping.getJavaClassName());
        field.setName(StringUtil.decapitalize(NameConverter.toCamelCase(meta
                .getName())));
    }

    public List<EntityMappingRow> getMappingRows() {
        return mappingRows;
    }
}
