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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.seasar.dolteng.core.entity.impl.BasicFieldMetaData;
import org.seasar.dolteng.core.entity.impl.BasicMethodMetaData;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.ColumnDescriptor;
import org.seasar.dolteng.eclipse.model.PageMappingRow;
import org.seasar.dolteng.eclipse.model.impl.BasicPageMappingRow;
import org.seasar.dolteng.eclipse.model.impl.IsGenerateColumn;
import org.seasar.dolteng.eclipse.model.impl.PageClassColumn;
import org.seasar.dolteng.eclipse.model.impl.PageFieldNameColumn;
import org.seasar.dolteng.eclipse.model.impl.PageModifierColumn;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.viewer.ComparableViewerSorter;
import org.seasar.dolteng.eclipse.viewer.TableProvider;
import org.seasar.framework.util.StringUtil;
import org.seasar.teeda.core.JsfConstants;
import org.seasar.teeda.extension.ExtensionConstants;
import org.seasar.teeda.extension.html.DocumentNode;
import org.seasar.teeda.extension.html.ElementNode;
import org.seasar.teeda.extension.html.HtmlNode;
import org.seasar.teeda.extension.html.HtmlParser;
import org.seasar.teeda.extension.html.impl.HtmlParserImpl;

/**
 * @author taichi
 * 
 */
public class PageMappingPage extends WizardPage {

    private NewPageWizardPage wizardPage;

    private TableViewer viewer;

    private List actionMethods;

    private List mappingRows;

    private Map pageFields;

    private Map rowFieldMapping;

    private IFile htmlfile;

    /**
     * @param pageName
     */
    public PageMappingPage(IFile resource) {
        super("PageMappingPage");
        setTitle(Labels.WIZARD_PAGE_PAGE_FIELD_SELECTION);
        setDescription(Labels.WIZARD_PAGE_CREATION_DESCRIPTION);
        this.mappingRows = new ArrayList();
        this.htmlfile = resource;
        this.actionMethods = new ArrayList();
        this.pageFields = new HashMap();
        this.rowFieldMapping = new HashMap();
    }

    public void setWizardPage(NewPageWizardPage page) {
        this.wizardPage = page;
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
        label.setText(Labels.WIZARD_PAGE_PAGE_TREE_LABEL);

        createRows();
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
        gd.heightHint = 60;
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
        descs.add(new IsGenerateColumn(table));
        // TODO マッピングの機能を実装する。
        // descs.add(new EntityClassColumn(table));
        // descs.add(new EntityFieldNameColumn(table));
        descs.add(new PageModifierColumn(table));
        descs.add(new PageClassColumn(table, this.mappingRows, JavaCore
                .create(this.htmlfile.getProject())));
        descs.add(new PageFieldNameColumn(table));
        return (ColumnDescriptor[]) descs.toArray(new ColumnDescriptor[descs
                .size()]);
    }

    private void createRows() {
        try {
            HtmlParser parser = new HtmlParserImpl();
            parser.setEncoding(this.htmlfile.getCharset());
            HtmlNode node = parser.parse(htmlfile.getContents());
            proceed(node);
            for (Iterator i = this.pageFields.values().iterator(); i.hasNext();) {
                BasicFieldMetaData meta = (BasicFieldMetaData) i.next();
                // TODO 型推論の為のエンティティ or DTO選択機能を実装する。
                BasicPageMappingRow row = new BasicPageMappingRow(
                        new BasicFieldMetaData(), meta);
                row.setGenerate(true);
                this.mappingRows.add(row);
                this.rowFieldMapping.put(meta.getName(), row);
            }
        } catch (CoreException e) {
            DoltengCore.log(e);
        }
        Collections.sort(this.mappingRows);
    }

    private void proceed(HtmlNode node) {
        if (node instanceof DocumentNode) {
            proceed((DocumentNode) node);
        } else if (node instanceof ElementNode) {
            proceed((ElementNode) node);
        }
    }

    private void proceed(DocumentNode node) {
        for (int i = 0; i < node.getChildSize(); i++) {
            HtmlNode child = node.getChild(i);
            proceed(child);
        }
    }

    private void proceed(ElementNode node) {
        for (int i = 0; i < node.getChildSize(); i++) {
            HtmlNode child = node.getChild(i);
            proceed(child);
        }
        String id = node.getId();
        if (StringUtil.isEmpty(id)) {
            id = node.getProperty(JsfConstants.CLASS_ATTR);
        }
        if (StringUtil.isEmpty(id)) {
            return;
        }
        if (0 == id.indexOf(ExtensionConstants.DO_PREFIX)) {
            BasicMethodMetaData meta = new BasicMethodMetaData();
            meta.setModifiers(Modifier.PUBLIC);
            meta.setName(id);
            this.actionMethods.add(meta);
        } else if (id.equalsIgnoreCase(JsfConstants.MESSAGES) == false
                && id.endsWith(ExtensionConstants.FORM_SUFFIX) == false
                && id.endsWith(ExtensionConstants.MESSAGE_SUFFIX) == false
                && id.startsWith(ExtensionConstants.GO_PREFIX) == false) {
            BasicFieldMetaData meta = new BasicFieldMetaData();
            meta.setModifiers(Modifier.PUBLIC);
            if (PageClassColumn.multiItemRegx.matcher(id).matches()) {
                // meta.setDeclaringClassName(PageClassColumn.toDtoArrayName(id));
                meta.setDeclaringClassName("java.util.List");
            } else {
                meta.setDeclaringClassName("java.lang.String");
            }
            meta.setName(id);
            this.pageFields.put(id, meta);
        }
    }

    public List getMappingRows() {
        return this.mappingRows;
    }

    public List getActionMethods() {
        return this.actionMethods;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    public void setVisible(boolean visible) {
        try {
            if (visible) {
                IJavaProject project = this.wizardPage.getPackageFragment()
                        .getJavaProject();
                IType superType = project.findType(this.wizardPage
                        .getSuperClass());
                IField[] fields = superType.getFields();
                for (int i = 0; i < fields.length; i++) {
                    IField f = fields[i];
                    PageMappingRow meta = (PageMappingRow) this.rowFieldMapping
                            .get(f.getElementName());
                    if (meta != null) {
                        meta.setGenerate(false);
                    }
                }
                this.viewer.refresh();
            }
        } catch (CoreException e) {
        }
        super.setVisible(visible);
    }

}