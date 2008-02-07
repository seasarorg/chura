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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.aonir.fuzzyxml.FuzzyXMLDocument;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLNode;
import jp.aonir.fuzzyxml.XPath;

import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.ColumnDescriptor;
import org.seasar.dolteng.eclipse.model.RegisterMocksRow;
import org.seasar.dolteng.eclipse.model.impl.BasicRegisterMocksRow;
import org.seasar.dolteng.eclipse.model.impl.MockImplementationName;
import org.seasar.dolteng.eclipse.model.impl.MockInterfaceNameColumn;
import org.seasar.dolteng.eclipse.model.impl.MockPackageNameColumn;
import org.seasar.dolteng.eclipse.model.impl.MockRegisterColumn;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.util.FuzzyXMLUtil;
import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.TextFileBufferUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.dolteng.eclipse.viewer.ComparableViewerSorter;
import org.seasar.dolteng.eclipse.viewer.TableProvider;
import org.seasar.dolteng.eclipse.wigets.ResourceTreeSelectionDialog;
import org.seasar.framework.convention.impl.NamingConventionImpl;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class RegisterMocksWizardPage extends WizardPage {

    private IPackageFragmentRoot root;

    private Button newoneCreate;

    private Text convention;

    private Label copyFromLabel;

    private Text copyFrom;

    private Button copyFromButton;

    private TableViewer viewer;

    private List<BasicRegisterMocksRow> registerMockRows = new ArrayList<BasicRegisterMocksRow>();

    private Map<String, BasicRegisterMocksRow> registerMockMap = new HashMap<String, BasicRegisterMocksRow>();

    /**
     * @param pageName
     */
    public RegisterMocksWizardPage(String pageName) {
        super(pageName);
    }

    public RegisterMocksWizardPage() {
        super("RegisterMocksWizardPage");
        setTitle(Labels.WIZARD_REGISTER_MOCKS_TITLE);
        setDescription(Labels.WIZARD_SELECT_MOCKS_TO_REGISTER);
    }

    public void setPackageFragmentRoot(IPackageFragmentRoot root) {
        this.root = root;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        initializeDialogUnits(parent);

        Composite c = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        c.setLayout(layout);

        newoneCreate = new Button(c, SWT.CHECK);
        newoneCreate.setText(Labels.WIZARD_CREATE_NEWONE);
        newoneCreate.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                copyFromLabel.setEnabled(newoneCreate.getSelection());
                copyFrom.setEnabled(newoneCreate.getSelection());
                copyFromButton.setEnabled(newoneCreate.getSelection());
                if (newoneCreate.getSelection() == false) {
                    copyFrom.setText("");
                }
                verifyInput();
            }
        });
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 3;
        newoneCreate.setLayoutData(data);

        Label l = new Label(c, SWT.NONE);
        l.setText(Labels.WIZARD_OUTPUT_FILE);
        convention = new Text(c, SWT.BORDER | SWT.SINGLE);
        data = new GridData(GridData.FILL_HORIZONTAL);
        convention.setLayoutData(data);
        Button conventionButton = new Button(c, SWT.PUSH);
        conventionButton.setText(Labels.BROWSE);
        conventionButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                chooseDicon(convention);
            }
        });

        copyFromLabel = new Label(c, SWT.NONE);
        copyFromLabel.setText(Labels.WIZARD_COPYFROM_FILE);
        copyFromLabel.setEnabled(false);
        copyFrom = new Text(c, SWT.BORDER | SWT.SINGLE);
        copyFrom.setEnabled(false);
        data = new GridData(GridData.FILL_HORIZONTAL);
        copyFrom.setLayoutData(data);
        copyFromButton = new Button(c, SWT.PUSH);
        copyFromButton.setText(Labels.BROWSE);
        copyFromButton.setEnabled(false);
        copyFromButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                chooseDicon(copyFrom);
            }
        });

        this.viewer = new TableViewer(c, SWT.BORDER | SWT.FULL_SELECTION
                | SWT.H_SCROLL | SWT.V_SCROLL);
        Table table = viewer.getTable();
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 3;
        data.verticalSpan = 3;
        table.setLayoutData(data);
        table.setSize(400, 350);

        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new TableProvider(viewer,
                createColumnDescs(table)));
        viewer.setSorter(new ComparableViewerSorter());
        viewer.setInput(setUpRows());

        ModifyListener checker = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                verifyInput();
            }
        };
        convention.addModifyListener(checker);
        copyFrom.addModifyListener(checker);
        setPageComplete(false);
        setControl(c);
    }

    private void chooseDicon(Text txt) {
        ResourceTreeSelectionDialog dialog = new ResourceTreeSelectionDialog(
                getShell(), ProjectUtil.getWorkspaceRoot(), IResource.FOLDER
                        | IResource.PROJECT | IResource.FILE);
        dialog.addFilter(new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement,
                    Object element) {
                if (element instanceof IFile) {
                    IFile f = (IFile) element;
                    return f.getFileExtension().endsWith("dicon");
                }
                return true;
            }
        });
        dialog.setInitialSelection(root.getResource().getProject());
        dialog.setAllowMultiple(false);
        if (dialog.open() == Dialog.OK) {
            Object[] results = dialog.getResult();
            if (results != null && 0 < results.length
                    && results[0] instanceof IFile) {
                IFile f = (IFile) results[0];
                txt.setText(f.getFullPath().toString());
            }
        }
    }

    private ColumnDescriptor[] createColumnDescs(Table table) {
        List<ColumnDescriptor> result = new ArrayList<ColumnDescriptor>();
        result.add(new MockRegisterColumn(table));
        result.add(new MockPackageNameColumn(table));
        result.add(new MockInterfaceNameColumn(table));
        result.add(new MockImplementationName(table));
        return result.toArray(new ColumnDescriptor[result.size()]);
    }

    private List setUpRows() {
        try {
            IJavaElement[] elements = root.getChildren();
            for (IJavaElement element : elements) {
                IPackageFragment f = (IPackageFragment) element;
                ICompilationUnit[] units = f.getCompilationUnits();
                for (int j = 0; units != null && j < units.length; j++) {
                    IType type = units[j].findPrimaryType();
                    if (f.getElementName().endsWith("mock")
                            || (type.isClass() && type.getElementName()
                                    .endsWith("Mock"))) {
                        addRegisterMockRow(f, type);
                    }
                }
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        }

        return registerMockRows;
    }

    private void addRegisterMockRow(IPackageFragment f, IType type)
            throws JavaModelException {
        ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
        if (hierarchy != null) {
            IType[] supers = hierarchy.getAllInterfaces();
            for (int k = 0; supers != null && k < supers.length; k++) {
                BasicRegisterMocksRow row = new BasicRegisterMocksRow(f
                        .getElementName(), supers[k].getFullyQualifiedName(),
                        type.getFullyQualifiedName());
                registerMockRows.add(row);
                registerMockMap.put(type.getFullyQualifiedName(), row);
            }
        }
    }

    private void verifyInput() {
        setErrorMessage(null);
        setPageComplete(false);
        IWorkspaceRoot root = ProjectUtil.getWorkspaceRoot();
        IPath path = null;
        if (newoneCreate.getSelection()
                && StringUtil.isEmpty(copyFrom.getText().trim()) == false) {
            if (copyFrom.getText().equalsIgnoreCase(convention.getText()) == false) {
                path = new Path(copyFrom.getText());
            } else {
                setErrorMessage(Messages.COPY_FROM_AND_COPY_TO_ARE_SAME);
            }
        } else if (newoneCreate.getSelection() == false
                && StringUtil.isEmpty(convention.getText().trim()) == false) {
            path = new Path(convention.getText());
        } else {
            setErrorMessage(Messages.INVALID_OUTPUT_FILE);
        }
        if (path != null) {
            IFile f = root.getFile(path);
            if (f != null && f.exists()) {
                setPageComplete(true);
            } else {
                setErrorMessage(Messages.INVALID_OUTPUT_FILE);
            }
        }
    }

    public boolean registerMocks() {
        IRunnableWithProgress op = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor)
                    throws InvocationTargetException {
                monitor = ProgressMonitorUtil.care(monitor);
                try {
                    monitor.beginTask(Messages.REGISTER_MOCKS, 10);
                    IWorkspaceRoot w = ProjectUtil.getWorkspaceRoot();
                    IPath newPath = new Path(convention.getText());
                    IFile content = w.getFile(newPath);
                    if (newoneCreate.getSelection()) {
                        if (content != null && content.exists()) {
                            content.delete(true, true, null);
                        }
                        IFile old = w.getFile(new Path(copyFrom.getText()));
                        ProgressMonitorUtil.isCanceled(monitor, 1);
                        old.copy(newPath, true, null);
                        ProgressMonitorUtil.isCanceled(monitor, 1);
                    }
                    FuzzyXMLDocument doc = FuzzyXMLUtil.parse(content);
                    removeExists(doc, content);
                    ProgressMonitorUtil.isCanceled(monitor, 3);

                    appendElements(doc, content);
                    ProgressMonitorUtil.isCanceled(monitor, 3);

                    root.getResource().getProject().refreshLocal(
                            IResource.DEPTH_INFINITE, null);
                    ProgressMonitorUtil.isCanceled(monitor, 1);

                    IFile f = w.getFile(new Path(convention.getText()));
                    WorkbenchUtil.openResource(f);
                    ProgressMonitorUtil.isCanceled(monitor, 1);
                } catch (Exception e) {
                    DoltengCore.log(e);
                    throw new InvocationTargetException(e);
                } finally {
                    monitor.done();
                }
            }

        };
        try {
            getWizard().getContainer().run(false, true, op);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void removeExists(FuzzyXMLDocument doc, IFile content) {
        FuzzyXMLNode[] nodes = XPath
                .selectNodes(doc.getDocumentElement(),
                        "//initMethod[@name=\"addInterfaceToImplementationClassName\"]");
        for (FuzzyXMLNode node : nodes) {
            FuzzyXMLElement e = (FuzzyXMLElement) node;
            FuzzyXMLNode[] kids = e.getChildren();
            int arg = 0;
            for (FuzzyXMLNode kid : kids) {
                if (kid instanceof FuzzyXMLElement) {
                    arg++;
                    if (1 < arg) {
                        FuzzyXMLElement argTag = (FuzzyXMLElement) kid;
                        String s = argTag.getValue().replaceAll("[\r\n\"]", "");
                        RegisterMocksRow row = registerMockMap
                                .remove(s);
                        registerMockRows.remove(row);
                        break;
                    }
                }
            }
        }
    }

    private void appendElements(FuzzyXMLDocument doc, IFile content)
            throws Exception {
        FuzzyXMLNode[] nodes = XPath.selectNodes(doc.getDocumentElement(),
                "//component[@class=\"" + NamingConventionImpl.class.getName()
                        + "\"]");
        String sep = ProjectUtil.getLineDelimiterPreference(content
                .getProject());
        if (0 < nodes.length) {
            FuzzyXMLElement e = (FuzzyXMLElement) nodes[0];
            FuzzyXMLNode[] kids = e.getChildren();
            if (0 < kids.length) {
                FuzzyXMLNode lastkid = kids[kids.length - 1];
                int offset = lastkid.getOffset() + lastkid.getLength();
                ITextFileBuffer buffer = null;
                IDocument document = null;
                try {
                    buffer = TextFileBufferUtil.acquire(content);
                    document = buffer.getDocument();

                    offset = document.getLineOffset(document
                            .getLineOfOffset(offset)) - 1;
                    MultiTextEdit editor = new MultiTextEdit();

                    for (RegisterMocksRow row : registerMockRows) {
                        StringBuffer stb = new StringBuffer();
                        stb.append(sep);
                        stb
                                .append("        <initMethod name=\"addInterfaceToImplementationClassName\">");
                        stb.append(sep);
                        stb.append("            <arg>\"");
                        stb.append(row.getInterfaceName());
                        stb.append("\"</arg>");
                        stb.append(sep);
                        stb.append("            <arg>\"");
                        stb.append(row.getImplementationName());
                        stb.append("\"</arg>");
                        stb.append(sep);
                        stb.append("        </initMethod>");
                        editor.addChild(new InsertEdit(offset, stb.toString()));
                    }
                    editor.apply(document);
                    buffer.commit(null, true);
                } finally {
                    if (buffer != null) {
                        TextFileBufferUtil.release(content);
                    }
                }
            }
        }
    }

}
