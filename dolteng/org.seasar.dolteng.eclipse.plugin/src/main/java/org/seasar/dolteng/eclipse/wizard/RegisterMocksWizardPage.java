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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.ColumnDescriptor;
import org.seasar.dolteng.eclipse.model.impl.BasicRegisterMocksRow;
import org.seasar.dolteng.eclipse.model.impl.MockImplementationName;
import org.seasar.dolteng.eclipse.model.impl.MockInterfaceNameColumn;
import org.seasar.dolteng.eclipse.model.impl.MockPackageNameColumn;
import org.seasar.dolteng.eclipse.model.impl.MockRegisterColumn;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.viewer.ComparableViewerSorter;
import org.seasar.dolteng.eclipse.viewer.TableProvider;
import org.seasar.dolteng.eclipse.wigets.ResourceTreeSelectionDialog;

/**
 * @author taichi
 * 
 */
public class RegisterMocksWizardPage extends WizardPage {

    private IPackageFragmentRoot root;

    private Text convention;

    private Label copyFromLabel;

    private Text copyFrom;

    private Button copyFromButton;

    private TableViewer viewer;

    private List registerMockRows = new ArrayList();

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

        final Button newone = new Button(c, SWT.CHECK);
        newone
                .setText("Specify New convention.dicon for Mock From production config.");
        newone.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                copyFromLabel.setEnabled(newone.getSelection());
                copyFrom.setEnabled(newone.getSelection());
                copyFromButton.setEnabled(newone.getSelection());
            }
        });
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 3;
        newone.setLayoutData(data);

        Label l = new Label(c, SWT.NONE);
        l.setText("Output File");
        convention = new Text(c, SWT.BORDER | SWT.SINGLE);
        data = new GridData(GridData.FILL_HORIZONTAL);
        convention.setLayoutData(data);
        Button conventionButton = new Button(c, SWT.PUSH);
        conventionButton.setText(Labels.BROWSE);
        conventionButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                chooseDicon(convention);
            }
        });

        copyFromLabel = new Label(c, SWT.NONE);
        copyFromLabel.setText("Copy From");
        copyFromLabel.setEnabled(false);
        copyFrom = new Text(c, SWT.BORDER | SWT.SINGLE);
        copyFrom.setEnabled(false);
        data = new GridData(GridData.FILL_HORIZONTAL);
        copyFrom.setLayoutData(data);
        copyFromButton = new Button(c, SWT.PUSH);
        copyFromButton.setText(Labels.BROWSE);
        copyFromButton.setEnabled(false);
        copyFromButton.addSelectionListener(new SelectionAdapter() {
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

        setControl(c);
    }

    private void chooseDicon(Text txt) {
        ResourceTreeSelectionDialog dialog = new ResourceTreeSelectionDialog(
                getShell(), ProjectUtil.getWorkspaceRoot(), IResource.FOLDER
                        | IResource.PROJECT | IResource.FILE);
        dialog.addFilter(new ViewerFilter() {
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
        List result = new ArrayList();
        result.add(new MockRegisterColumn(table));
        result.add(new MockPackageNameColumn(table));
        result.add(new MockInterfaceNameColumn(table));
        result.add(new MockImplementationName(table));
        return (ColumnDescriptor[]) result.toArray(new ColumnDescriptor[result
                .size()]);
    }

    private List setUpRows() {
        // FIXME : イマイチ。。。
        try {
            IJavaElement[] elements = root.getChildren();
            for (int i = 0; i < elements.length; i++) {
                IPackageFragment f = (IPackageFragment) elements[i];
                // FIXME : NamingConvention からとれる？
                if (f.getElementName().endsWith("mock")) {
                    ICompilationUnit[] units = f.getCompilationUnits();
                    for (int j = 0; units != null && j < units.length; j++) {
                        IType type = units[j].findPrimaryType();
                        addRegisterMockRow(f, type);
                    }
                } else {
                    ICompilationUnit[] units = f.getCompilationUnits();
                    for (int j = 0; units != null && j < units.length; j++) {
                        IType type = units[j].findPrimaryType();
                        if (type.getElementName().endsWith("Mock")) {
                            addRegisterMockRow(f, type);
                        }
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
        IType[] supers = hierarchy.getAllInterfaces();
        for (int k = 0; supers != null && k < supers.length; k++) {
            BasicRegisterMocksRow row = new BasicRegisterMocksRow(f
                    .getElementName(), supers[k].getFullyQualifiedName(), type
                    .getFullyQualifiedName());
            registerMockRows.add(row);
        }
    }

    public boolean registerMocks() {
        return false;
    }
}
