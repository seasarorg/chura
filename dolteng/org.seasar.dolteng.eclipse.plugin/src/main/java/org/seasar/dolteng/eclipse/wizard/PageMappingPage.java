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
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.seasar.dolteng.core.entity.impl.BasicFieldMetaData;
import org.seasar.dolteng.core.entity.impl.BasicMethodMetaData;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.ColumnDescriptor;
import org.seasar.dolteng.eclipse.model.PageMappingRow;
import org.seasar.dolteng.eclipse.model.impl.BasicPageMappingRow;
import org.seasar.dolteng.eclipse.model.impl.IsSuperGenerateColumn;
import org.seasar.dolteng.eclipse.model.impl.IsThisGenerateColumn;
import org.seasar.dolteng.eclipse.model.impl.PageClassColumn;
import org.seasar.dolteng.eclipse.model.impl.PageFieldNameColumn;
import org.seasar.dolteng.eclipse.model.impl.PageModifierColumn;
import org.seasar.dolteng.eclipse.model.impl.SrcClassColumn;
import org.seasar.dolteng.eclipse.model.impl.SrcFieldNameColumn;
import org.seasar.dolteng.eclipse.nls.Images;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.operation.TypeHierarchyFieldProcessor;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.dolteng.eclipse.util.TypeUtil;
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

    private ArrayList multiItemBase = new ArrayList();

    private Text mappingTypeName;

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

        createPartOfMappingSelector(composite);

        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        Label label = new Label(composite, SWT.LEFT | SWT.WRAP);
        label.setText(Labels.WIZARD_PAGE_PAGE_TREE_LABEL);
        label.setLayoutData(gd);

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

    /**
     * @param composite
     */
    private void createPartOfMappingSelector(Composite composite) {
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        Group group = new Group(composite, SWT.NONE);
        group.setLayout(new GridLayout(5, false));
        group.setLayoutData(gd);
        group.setText(Labels.WIZARD_PAGE_SELECT_TYPE);

        Composite radios = new Composite(group, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 5;
        radios.setLayoutData(gd);
        radios.setLayout(new FillLayout(SWT.HORIZONTAL));

        Button classRadio = new Button(radios, SWT.RADIO);
        classRadio.setText(Labels.WIZARD_PAGE_CLASS_MAPPING);
        classRadio.setSelection(true);
        Button tableRadio = new Button(radios, SWT.RADIO);
        tableRadio.setText(Labels.WIZARD_PAGE_TABLE_MAPPING);

        Composite comp = new Composite(group, SWT.NONE);
        comp.setLayout(new GridLayout(7, false));
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 3;
        comp.setLayoutData(gd);

        final Label typeIcon = new Label(comp, SWT.NONE);
        typeIcon.setImage(Images.TYPE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 1;
        typeIcon.setLayoutData(gd);
        classRadio.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                typeIcon.setImage(Images.TYPE);
                strategy = classStrategy;
                mappingTypeName.setText("");
            }
        });
        tableRadio.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                typeIcon.setImage(Images.TABLE);
                strategy = tableStrategy;
                mappingTypeName.setText("");
            }
        });

        mappingTypeName = new Text(comp, SWT.SINGLE | SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 6;
        gd.widthHint = 300;
        mappingTypeName.setLayoutData(gd);

        Button browse = new Button(group, SWT.PUSH);
        browse.setText(Labels.BROWSE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 1;
        browse.setLayoutData(gd);
        browse.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                strategy.chooseType();
            }
        });

        Button refresh = new Button(group, SWT.PUSH);
        refresh.setText(Labels.REFRESH);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 1;
        refresh.setLayoutData(gd);
        refresh.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                strategy.refresh();
                viewer.refresh(true);
            }
        });
    }

    private SelectionStrategy tableStrategy = new SelectionStrategy() {
        public void chooseType() {
            chooseTableTypes();
        }

        public void refresh() {
            processTableMapping();
        }
    };

    private SelectionStrategy classStrategy = new SelectionStrategy() {
        public void chooseType() {
            chooseClassTypes();
        }

        public void refresh() {
            processTypeMapping();
        }
    };

    private SelectionStrategy strategy = classStrategy;

    private interface SelectionStrategy {
        void chooseType();

        void refresh();
    }

    public void chooseTableTypes() {

    }

    public void chooseClassTypes() {
        try {
            IJavaProject javap = this.wizardPage.getPackageFragment()
                    .getJavaProject();
            IJavaSearchScope scope = SearchEngine.createJavaSearchScope(
                    new IJavaElement[] { javap }, true);
            SelectionDialog dialog = JavaUI.createTypeDialog(getShell(),
                    getContainer(), scope,
                    IJavaElementSearchConstants.CONSIDER_CLASSES, false);
            if (dialog.open() == Window.OK) {
                Object[] result = dialog.getResult();
                if (result != null && 0 < result.length) {
                    IType type = (IType) result[0];
                    mappingTypeName.setText(type.getFullyQualifiedName());
                }
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        }
    }

    private void processTableMapping() {

    }

    private void processTypeMapping() {
        try {
            String typeName = mappingTypeName.getText();
            if (StringUtil.isEmpty(typeName)) {
                return;
            }
            IJavaProject javap = this.wizardPage.getPackageFragment()
                    .getJavaProject();
            IType type = javap.findType(typeName);
            IRunnableWithProgress runnable = new TypeHierarchyFieldProcessor(
                    type, new TypeHierarchyFieldProcessor.FieldHandler() {
                        public void begin() {
                        }

                        public void process(IField field) {
                            try {
                                PageMappingRow meta = (PageMappingRow) PageMappingPage.this.rowFieldMapping
                                        .get(field.getElementName());
                                if (meta != null) {
                                    IType t = field.getDeclaringType();
                                    String typeName = TypeUtil
                                            .getResolvedTypeName(field
                                                    .getTypeSignature(), t);
                                    meta.setSrcClassName(typeName);
                                    meta
                                            .setSrcFieldName(field
                                                    .getElementName());
                                    meta.setPageClassName(typeName);
                                }
                            } catch (Exception e) {
                                DoltengCore.log(e);
                            }
                        }

                        public void done() {
                            PageMappingPage.this.viewer.refresh();
                        }
                    });
            getContainer().run(false, false, runnable);
        } catch (Exception e) {
            DoltengCore.log(e);
        }
    }

    private ColumnDescriptor[] createColumnDescs(Table table) {
        List descs = new ArrayList();
        descs.add(new IsSuperGenerateColumn(table));
        descs.add(new IsThisGenerateColumn(table));
        descs.add(new PageModifierColumn(table));
        descs.add(createPageClassColumn(table));
        descs.add(new PageFieldNameColumn(table));
        descs.add(new SrcClassColumn(table));
        descs.add(new SrcFieldNameColumn(table));
        return (ColumnDescriptor[]) descs.toArray(new ColumnDescriptor[descs
                .size()]);
    }

    /**
     * @param table
     * @return
     */
    private PageClassColumn createPageClassColumn(Table table) {
        IJavaProject javap = JavaCore.create(this.htmlfile.getProject());
        multiItemBase.add("java.util.List");
        DoltengProjectPreferences pref = DoltengCore.getPreferences(javap);
        try {
            if (pref != null) {
                String pkgName = pref.getRawPreferences().getString(
                        Constants.PREF_DEFAULT_DTO_PACKAGE);
                multiItemBase.addAll(TypeUtil.getTypeNamesUnderPkg(javap,
                        pkgName));
                List types = TypeUtil.getTypeNamesUnderPkg(javap, wizardPage
                        .getPackageText());
                for (Iterator i = types.iterator(); i.hasNext();) {
                    String s = (String) i.next();
                    if (s.endsWith("Dto") || s.endsWith("DTO")) {
                        multiItemBase.add(s);
                    }
                }
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        }
        return new PageClassColumn(table, multiItemBase);
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
                row.setThisGenerate(true);
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
        } else if (skipIds.matcher(id).matches() == false) {
            // TODO ElementProcessorFactoryを使う様にする。
            BasicFieldMetaData meta = new BasicFieldMetaData();
            meta.setModifiers(Modifier.PUBLIC);
            if (PageClassColumn.multiItemRegx.matcher(id).matches()) {
                meta.setDeclaringClassName("java.util.List");
            } else {
                meta.setDeclaringClassName("java.lang.String");
            }
            meta.setName(id);
            this.pageFields.put(id, meta);
        }
    }

    private static final Pattern skipIds = Pattern.compile(
            JsfConstants.MESSAGES + "|" + ".*" + ExtensionConstants.FORM_SUFFIX
                    + "|" + ".*" + ExtensionConstants.MESSAGE_SUFFIX + "|"
                    + "|" + ExtensionConstants.GO_PREFIX + ".*" + "|"
                    + ExtensionConstants.MESSAGE_SUFFIX + ".*",
            Pattern.CASE_INSENSITIVE);

    public List getMappingRows() {
        return this.mappingRows;
    }

    public List getActionMethods() {
        return this.actionMethods;
    }

    public List getMultiItemBase() {
        return this.multiItemBase;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    public void setVisible(boolean visible) {
        try {
            if (visible) {
                final IJavaProject project = this.wizardPage
                        .getPackageFragment().getJavaProject();
                String typename = this.wizardPage.getSuperClass();
                IType type = project.findType(typename);
                IRunnableWithProgress runnable = new TypeHierarchyFieldProcessor(
                        type, new TypeHierarchyFieldProcessor.FieldHandler() {
                            public void begin() {
                            }

                            public void process(IField field) {
                                PageMappingRow meta = (PageMappingRow) PageMappingPage.this.rowFieldMapping
                                        .get(field.getElementName());
                                if (meta != null) {
                                    meta.setThisGenerate(false);
                                }
                            }

                            public void done() {
                                PageMappingPage.this.viewer.refresh();
                            }
                        });
                getContainer().run(false, false, runnable);
            }
        } catch (Exception e) {
        }
        super.setVisible(visible);
    }

}