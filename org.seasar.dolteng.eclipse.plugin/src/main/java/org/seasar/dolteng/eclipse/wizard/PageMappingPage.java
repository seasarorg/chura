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
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.aonir.fuzzyxml.FuzzyXMLDocument;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLNode;
import jp.aonir.fuzzyxml.XPath;

import org.apache.commons.collections.map.ListOrderedMap;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.wizards.NewClassWizardPage;
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
import org.seasar.dolteng.core.entity.ColumnMetaData;
import org.seasar.dolteng.core.entity.FieldMetaData;
import org.seasar.dolteng.core.entity.impl.BasicFieldMetaData;
import org.seasar.dolteng.core.entity.impl.BasicMethodMetaData;
import org.seasar.dolteng.core.types.TypeMapping;
import org.seasar.dolteng.core.types.TypeMappingRegistry;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.ColumnDescriptor;
import org.seasar.dolteng.eclipse.model.PageMappingRow;
import org.seasar.dolteng.eclipse.model.TreeContent;
import org.seasar.dolteng.eclipse.model.impl.BasicPageMappingRow;
import org.seasar.dolteng.eclipse.model.impl.ColumnNode;
import org.seasar.dolteng.eclipse.model.impl.IsSuperGenerateColumn;
import org.seasar.dolteng.eclipse.model.impl.IsThisGenerateColumn;
import org.seasar.dolteng.eclipse.model.impl.PageClassColumn;
import org.seasar.dolteng.eclipse.model.impl.PageFieldNameColumn;
import org.seasar.dolteng.eclipse.model.impl.PageModifierColumn;
import org.seasar.dolteng.eclipse.model.impl.ProjectNode;
import org.seasar.dolteng.eclipse.model.impl.SchemaNode;
import org.seasar.dolteng.eclipse.model.impl.SrcClassColumn;
import org.seasar.dolteng.eclipse.model.impl.SrcFieldNameColumn;
import org.seasar.dolteng.eclipse.model.impl.TableNode;
import org.seasar.dolteng.eclipse.nls.Images;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.operation.TypeHierarchyFieldProcessor;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.util.DoltengProjectUtil;
import org.seasar.dolteng.eclipse.util.FuzzyXMLUtil;
import org.seasar.dolteng.eclipse.util.HtmlNodeAnalyzer;
import org.seasar.dolteng.eclipse.util.NameConverter;
import org.seasar.dolteng.eclipse.util.TypeUtil;
import org.seasar.dolteng.eclipse.viewer.TableProvider;
import org.seasar.dolteng.eclipse.wigets.ModifierGroup;
import org.seasar.dolteng.eclipse.wigets.TableDialog;
import org.seasar.framework.util.StringUtil;

/**
 * Pageのフィールド編集WizardPage
 * 
 * @author taichi
 */
public class PageMappingPage extends WizardPage implements
        ModifierGroup.ModifierSelectionListener {

    private static final String NAME = PageMappingPage.class.getName();

    private static final String CONFIG_USE_PUBLIC_FIELD = "usePublicField";

    private NewClassWizardPage wizardPage;

    /** フィールドテーブルのビュアー */
    private TableViewer viewer;

    /** HTMLアナライザ */
    protected HtmlNodeAnalyzer analyzer;

    private List<PageMappingRow> mappingRows;

    private Map<String, PageMappingRow> rowFieldMapping;

    /** 元となるHTMLファイル */
    private IFile htmlfile;

    private List<String> multiItemBase;

    private Text mappingTypeName;

    private TableNode selectedTable = null;

    private boolean usePublicField = true;

    private DoltengPreferences pref;

    /**
     * @param wizard
     * @param resource
     */
    public PageMappingPage(IFile resource) {
        this(resource, NAME);
        setTitle(Labels.WIZARD_PAGE_PAGE_FIELD_SELECTION);
        setDescription(Labels.WIZARD_PAGE_CREATION_DESCRIPTION);
    }

    @SuppressWarnings("unchecked")
    protected PageMappingPage(IFile resource, String name) {
        super(name);
        this.analyzer = new HtmlNodeAnalyzer(resource);
        this.mappingRows = new ArrayList<PageMappingRow>();
        this.htmlfile = resource;
        this.rowFieldMapping = new ListOrderedMap();

        pref = DoltengCore.getPreferences(resource.getProject());
        if (pref != null) {
            this.usePublicField = pref.isUsePublicField();
        }
    }

    public void setWizardPage(NewClassWizardPage page) {
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
        createPartOfPublicField(composite);

        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        Label label = new Label(composite, SWT.LEFT | SWT.WRAP);
        label.setText(Labels.WIZARD_PAGE_PAGE_TREE_LABEL);
        label.setLayoutData(gd);

        createRows();
        this.multiItemBase = DoltengProjectUtil.findDtoNames(htmlfile,
                wizardPage.getPackageText());

        this.viewer = new TableViewer(composite, SWT.BORDER
                | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        Table table = viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new TableProvider(viewer,
                createColumnDescs(table)));
        // viewer.setSorter(new ComparableViewerSorter());
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
            @Override
            public void widgetSelected(SelectionEvent e) {
                typeIcon.setImage(Images.TYPE);
                strategy = classStrategy;
                mappingTypeName.setText("");
                selectedTable = null;
            }
        });
        tableRadio.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                typeIcon.setImage(Images.TABLE);
                strategy = tableStrategy;
                mappingTypeName.setText("");
                selectedTable = null;
            }
        });

        mappingTypeName = new Text(comp, SWT.SINGLE | SWT.BORDER
                | SWT.READ_ONLY);
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
            @Override
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
            @Override
            public void widgetSelected(SelectionEvent event) {
                IRunnableWithProgress op = new IRunnableWithProgress() {
                    public void run(IProgressMonitor monitor) {
                        if (monitor == null) {
                            monitor = new NullProgressMonitor();
                        }
                        try {
                            monitor.beginTask("", 2);
                            strategy.refresh();
                            monitor.worked(1);
                            viewer.refresh(true);
                            monitor.worked(1);
                        } finally {
                            monitor.done();
                        }
                    }
                };
                try {
                    getContainer().run(false, false, op);
                } catch (Exception e) {
                    DoltengCore.log(e);
                }
            }
        });
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
        IJavaProject javap = this.wizardPage.getPackageFragment()
                .getJavaProject();
        TableDialog dialog = new TableDialog(getShell(), javap);
        selectedTable = null;
        if (dialog.open() == Window.OK) {
            TableNode node = dialog.getTableNode();
            if (node != null) {
                TreeContent tc = node.getParent();
                StringBuffer stb = new StringBuffer();
                if (tc instanceof SchemaNode) {
                    SchemaNode sn = (SchemaNode) tc;
                    stb.append(sn.getText());
                    stb.append('.');
                }
                stb.append(node.getText());
                mappingTypeName.setText(stb.toString());
                selectedTable = node;
            }
        }
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
        if (selectedTable == null) {
            return;
        }
        ProjectNode pn = (ProjectNode) selectedTable.getRoot();
        TypeMappingRegistry registry = DoltengCore.getTypeMappingRegistry(pn
                .getJavaProject());
        TreeContent[] columns = selectedTable.getChildren();
        for (TreeContent column : columns) {
            ColumnNode cn = (ColumnNode) column;
            ColumnMetaData meta = cn.getColumnMetaData();

            String s = StringUtil.decapitalize(NameConverter.toCamelCase(meta
                    .getName()));
            PageMappingRow row = rowFieldMapping.get(s);
            if (row != null) {
                TypeMapping mapping = registry.toJavaClass(meta);
                row.setSrcClassName(meta.getSqlTypeName());
                row.setSrcFieldName(meta.getName());
                row.setPageClassName(mapping.getJavaClassName());
            }
        }
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
                                PageMappingRow meta = PageMappingPage.this.rowFieldMapping
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

    protected ColumnDescriptor[] createColumnDescs(Table table) {
        List<ColumnDescriptor> descs = new ArrayList<ColumnDescriptor>();
        descs.add(new IsSuperGenerateColumn(table));
        descs.add(new IsThisGenerateColumn(table));
        descs.add(new PageModifierColumn(table));
        descs.add(new PageClassColumn(viewer, multiItemBase, htmlfile, this));
        descs.add(new PageFieldNameColumn(table, this));
        descs.add(new SrcClassColumn(table));
        descs.add(new SrcFieldNameColumn(table));
        return descs.toArray(new ColumnDescriptor[descs.size()]);
    }

    protected void createRows() {
        analyzer.analyze();
        Map<String, FieldMetaData> pageFields = analyzer.getPageFields();
        FuzzyXMLElement root = getHtmlRootElement();
        for (Map.Entry<String, FieldMetaData> e : pageFields.entrySet()) {
            FieldMetaData meta = e.getValue();
            BasicPageMappingRow row = new BasicPageMappingRow(
                    new BasicFieldMetaData(), meta);
            row.setThisGenerate(true);
            this.mappingRows.add(row);
            this.rowFieldMapping.put(meta.getName(), row);
            if (root != null && meta.getName().endsWith("Save")) {
                FuzzyXMLNode[] list = XPath.selectNodes(root, "//input[@id=\""
                        + meta.getName() + "\"][@type=\"hidden\"]");
                if (list != null && 0 < list.length) {
                    row.setThisGenerate(false);
                }
            }
        }
        // Collections.sort(this.mappingRows);
    }

    private FuzzyXMLElement getHtmlRootElement() {
        try {
            FuzzyXMLDocument doc = FuzzyXMLUtil.parse(this.htmlfile);
            if (doc != null) {
                return doc.getDocumentElement();
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        }
        return null;
    }

    public Map<String, PageMappingRow> getRowFieldMapping() {
        return this.rowFieldMapping;
    }

    public List<PageMappingRow> getMappingRows() {
        return this.mappingRows;
    }

    public Set<BasicMethodMetaData> getActionMethods() {
        return analyzer.getActionMethods();
    }

    public Set<BasicMethodMetaData> getConditionMethods() {
        return analyzer.getConditionMethods();
    }

    public List getMultiItemBase() {
        return DoltengProjectUtil.findDtoNames(htmlfile, wizardPage
                .getPackageText());
    }

    public boolean getUsePublicField() {
        return usePublicField;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        try {
            if (visible) {
                IJavaProject project = this.wizardPage.getPackageFragment()
                        .getJavaProject();
                String typename = this.wizardPage.getSuperClass();
                IType type = project.findType(typename);
                if (type.getFullyQualifiedName().startsWith("java") == false) {
                    IRunnableWithProgress runnable = new TypeHierarchyFieldProcessor(
                            type,
                            new TypeHierarchyFieldProcessor.FieldHandler() {
                                public void begin() {
                                }

                                public void process(IField field) {
                                    PageMappingRow meta = PageMappingPage.this.rowFieldMapping
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
            }
        } catch (Exception e) {
        }
        super.setVisible(visible);
    }

}