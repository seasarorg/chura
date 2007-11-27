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

import java.lang.reflect.InvocationTargetException;
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
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
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
import org.seasar.dolteng.eclipse.util.DoltengProjectUtil;
import org.seasar.dolteng.eclipse.util.FuzzyXMLUtil;
import org.seasar.dolteng.eclipse.util.HtmlNodeAnalyzer;
import org.seasar.dolteng.eclipse.util.NameConverter;
import org.seasar.dolteng.eclipse.util.TypeUtil;
import org.seasar.dolteng.eclipse.viewer.TableProvider;
import org.seasar.dolteng.eclipse.wigets.TableDialog;
import org.seasar.framework.util.StringUtil;

/**
 * Pageのフィールド編集WizardPage
 * 
 * @author taichi
 */
public class PageMappingPage extends WizardPage {

    private static final String NAME = PageMappingPage.class.getName();

    private static final String CONFIG_USE_PUBLIC_FIELD = "usePublicField";

    private NewClassWizardPage wizardPage;

    /** フィールドテーブルのビュアー */
    private TableViewer viewer;

    /** HTMLアナライザ */
    protected HtmlNodeAnalyzer analyzer;

    private final List<PageMappingRow> mappingRows;

    private final Map<String, PageMappingRow> rowFieldMapping;

    /** 元となるHTMLファイル */
    private final IFile htmlfile;

    private ArrayList multiItemBase;

    private Text mappingTypeName;

    private TableNode selectedTable = null;
    
    private boolean usePublicField = true;

    /**
     * @param resource
     */
    public PageMappingPage(final IWizard wizard, final IFile resource) {
        this(wizard, resource, NAME);
        setTitle(Labels.WIZARD_PAGE_PAGE_FIELD_SELECTION);
        setDescription(Labels.WIZARD_PAGE_CREATION_DESCRIPTION);
    }

    protected PageMappingPage(final IWizard wizard, final IFile resource, final String name) {
        super(name);
        this.analyzer = new HtmlNodeAnalyzer(resource);
        this.mappingRows = new ArrayList<PageMappingRow>();
        this.htmlfile = resource;
        this.rowFieldMapping = new ListOrderedMap();
        
        setWizard(wizard);
        
        final IDialogSettings section = getDialogSettings().getSection(NAME);
        if (section != null) {
            this.usePublicField = section.getBoolean(CONFIG_USE_PUBLIC_FIELD);
        }
    }

    public void setWizardPage(final NewClassWizardPage page) {
        this.wizardPage = page;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(final Composite parent) {
        initializeDialogUnits(parent);

        final Composite composite = new Composite(parent, SWT.NONE);
        final GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);

        createPartOfMappingSelector(composite);
        createPartOfPublicField(composite);

        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        final Label label = new Label(composite, SWT.LEFT | SWT.WRAP);
        label.setText(Labels.WIZARD_PAGE_PAGE_TREE_LABEL);
        label.setLayoutData(gd);

        createRows();
        this.multiItemBase = DoltengProjectUtil.findDtoNames(htmlfile,
                wizardPage.getPackageText());

        this.viewer = new TableViewer(composite, SWT.BORDER
                | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
        final Table table = viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new TableProvider(viewer,
                createColumnDescs(table)));
//      viewer.setSorter(new ComparableViewerSorter());
        viewer.setInput(this.mappingRows);

        gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL
                | GridData.GRAB_VERTICAL);
        gd.heightHint = 60;
        gd.horizontalSpan = 2;
        table.setLayoutData(gd);

        final Label spacer = new Label(composite, SWT.NONE);
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
    private void createPartOfMappingSelector(final Composite composite) {
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        final Group group = new Group(composite, SWT.NONE);
        group.setLayout(new GridLayout(5, false));
        group.setLayoutData(gd);
        group.setText(Labels.WIZARD_PAGE_SELECT_TYPE);

        final Composite radios = new Composite(group, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 5;
        radios.setLayoutData(gd);
        radios.setLayout(new FillLayout(SWT.HORIZONTAL));

        final Button classRadio = new Button(radios, SWT.RADIO);
        classRadio.setText(Labels.WIZARD_PAGE_CLASS_MAPPING);
        classRadio.setSelection(true);
        final Button tableRadio = new Button(radios, SWT.RADIO);
        tableRadio.setText(Labels.WIZARD_PAGE_TABLE_MAPPING);

        final Composite comp = new Composite(group, SWT.NONE);
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

        final Button browse = new Button(group, SWT.PUSH);
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

        final Button refresh = new Button(group, SWT.PUSH);
        refresh.setText(Labels.REFRESH);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 1;
        refresh.setLayoutData(gd);
        refresh.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                final IRunnableWithProgress op = new IRunnableWithProgress() {
                    public void run(IProgressMonitor monitor)
                            throws InvocationTargetException,
                            InterruptedException {
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
                } catch (final Exception e) {
                    DoltengCore.log(e);
                }
            }
        });
    }

    private void createPartOfPublicField(final Composite composite) {
        final Group group = new Group(composite, SWT.NONE);
        group.setLayout(new FillLayout(SWT.HORIZONTAL));
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setText(Labels.WIZARD_PAGE_FIELD_TYPE);

        final Button privateRadio = new Button(group, SWT.RADIO);
        privateRadio.setText(Labels.WIZARD_PAGE_FIELD_PRIVATE);
        privateRadio.setSelection(! usePublicField);
        final Button publicRadio = new Button(group, SWT.RADIO);
        publicRadio.setText(Labels.WIZARD_PAGE_FIELD_PUBLIC);
        publicRadio.setSelection(usePublicField);
        
        privateRadio.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                usePublicField = false;
                
                IDialogSettings section = getDialogSettings().getSection(NAME);
                if (section == null) {
                    section = getDialogSettings().addNewSection(NAME);
                }
                section.put(CONFIG_USE_PUBLIC_FIELD, usePublicField);
                
                // TODO : Accessor Modifierの列をenable若しくはvisible
            }
        });
        publicRadio.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                usePublicField = true;
                
                IDialogSettings section = getDialogSettings().getSection(NAME);
                if (section == null) {
                    section = getDialogSettings().addNewSection(NAME);
                }
                section.put(CONFIG_USE_PUBLIC_FIELD, usePublicField);
                
                // TODO : Accessor Modifierの列をdisable若しくはinvisible
            }
        });
    }

    private final SelectionStrategy tableStrategy = new SelectionStrategy() {
        public void chooseType() {
            chooseTableTypes();
        }

        public void refresh() {
            processTableMapping();
        }
    };

    private final SelectionStrategy classStrategy = new SelectionStrategy() {
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
        final IJavaProject javap = this.wizardPage.getPackageFragment()
                .getJavaProject();
        final TableDialog dialog = new TableDialog(getShell(), javap);
        selectedTable = null;
        if (dialog.open() == Window.OK) {
            final TableNode node = dialog.getTableNode();
            if (node != null) {
                final TreeContent tc = node.getParent();
                final StringBuffer stb = new StringBuffer();
                if (tc instanceof SchemaNode) {
                    final SchemaNode sn = (SchemaNode) tc;
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
            final IJavaProject javap = this.wizardPage.getPackageFragment()
                    .getJavaProject();
            final IJavaSearchScope scope = SearchEngine.createJavaSearchScope(
                    new IJavaElement[] { javap }, true);
            final SelectionDialog dialog = JavaUI.createTypeDialog(getShell(),
                    getContainer(), scope,
                    IJavaElementSearchConstants.CONSIDER_CLASSES, false);
            if (dialog.open() == Window.OK) {
                final Object[] result = dialog.getResult();
                if (result != null && 0 < result.length) {
                    final IType type = (IType) result[0];
                    mappingTypeName.setText(type.getFullyQualifiedName());
                }
            }
        } catch (final Exception e) {
            DoltengCore.log(e);
        }
    }

    private void processTableMapping() {
        if (selectedTable == null) {
            return;
        }
        final ProjectNode pn = (ProjectNode) selectedTable.getRoot();
        final TypeMappingRegistry registry = DoltengCore.getTypeMappingRegistry(pn
                .getJavaProject());
        final TreeContent[] columns = selectedTable.getChildren();
        for (int i = 0; i < columns.length; i++) {
            final ColumnNode cn = (ColumnNode) columns[i];
            final ColumnMetaData meta = cn.getColumnMetaData();

            final String s = StringUtil.decapitalize(NameConverter.toCamelCase(meta
                    .getName()));
            final PageMappingRow row = rowFieldMapping.get(s);
            if (row != null) {
                final TypeMapping mapping = registry.toJavaClass(meta);
                row.setSrcClassName(meta.getSqlTypeName());
                row.setSrcFieldName(meta.getName());
                row.setPageClassName(mapping.getJavaClassName());
            }
        }
    }

    private void processTypeMapping() {
        try {
            final String typeName = mappingTypeName.getText();
            if (StringUtil.isEmpty(typeName)) {
                return;
            }
            final IJavaProject javap = this.wizardPage.getPackageFragment()
                    .getJavaProject();
            final IType type = javap.findType(typeName);
            final IRunnableWithProgress runnable = new TypeHierarchyFieldProcessor(
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
        } catch (final Exception e) {
            DoltengCore.log(e);
        }
    }

    protected ColumnDescriptor[] createColumnDescs(final Table table) {
        final List<ColumnDescriptor> descs = new ArrayList<ColumnDescriptor>();
        descs.add(new IsSuperGenerateColumn(table));
        descs.add(new IsThisGenerateColumn(table));
        descs.add(new PageModifierColumn(table));
        descs.add(new PageClassColumn(viewer, multiItemBase, htmlfile, this));
        descs.add(new PageFieldNameColumn(table, this));
        descs.add(new SrcClassColumn(table));
        descs.add(new SrcFieldNameColumn(table));
        return descs.toArray(new ColumnDescriptor[descs
                .size()]);
    }

    protected void createRows() {
        analyzer.analyze();
        final Map<String, FieldMetaData> pageFields = analyzer.getPageFields();
        final FuzzyXMLElement root = getHtmlRootElement();
        for (final Map.Entry<String, FieldMetaData> e : pageFields.entrySet()) {
            final FieldMetaData meta = e.getValue();
            final BasicPageMappingRow row = new BasicPageMappingRow(
                    new BasicFieldMetaData(), meta);
            row.setThisGenerate(true);
            this.mappingRows.add(row);
            this.rowFieldMapping.put(meta.getName(), row);
            if (root != null && meta.getName().endsWith("Save")) {
                final FuzzyXMLNode[] list = XPath.selectNodes(root, "//input[@id=\""
                        + meta.getName() + "\"][@type=\"hidden\"]");
                if (list != null && 0 < list.length) {
                    row.setThisGenerate(false);
                }
            }
        }
//      Collections.sort(this.mappingRows);
    }

    private FuzzyXMLElement getHtmlRootElement() {
        try {
            final FuzzyXMLDocument doc = FuzzyXMLUtil.parse(this.htmlfile);
            if (doc != null) {
                return doc.getDocumentElement();
            }
        } catch (final Exception e) {
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
    public void setVisible(final boolean visible) {
        try {
            if (visible) {
                final IJavaProject project = this.wizardPage
                        .getPackageFragment().getJavaProject();
                final String typename = this.wizardPage.getSuperClass();
                final IType type = project.findType(typename);
                if (type.getFullyQualifiedName().startsWith("java") == false) {
                    final IRunnableWithProgress runnable = new TypeHierarchyFieldProcessor(
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
        } catch (final Exception e) {
        }
        super.setVisible(visible);
    }

}