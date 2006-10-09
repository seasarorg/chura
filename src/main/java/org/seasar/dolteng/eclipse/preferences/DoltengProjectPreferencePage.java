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

package org.seasar.dolteng.eclipse.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.part.DatabaseView;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.wigets.ResourceTreeSelectionDialog;

/**
 * @author taichi
 * 
 */
public class DoltengProjectPreferencePage extends PropertyPage {

    private Button useDolteng;

    private Combo daoType;

    private Button usePageMarker;

    private Text defaultEntityPkg;

    private Text defaultDaoPkg;

    private Text defaultDtoPkg;

    private Text defaultWebPkg;

    private Text ormXmlOutputPath;

    public DoltengProjectPreferencePage() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        composite.setLayout(layout);
        GridData data = new GridData(GridData.FILL);
        data.grabExcessHorizontalSpace = true;
        composite.setLayoutData(data);

        this.useDolteng = new Button(createDefaultComposite(composite),
                SWT.CHECK);
        this.useDolteng.setText(Labels.PREFERENCE_USE_DOLTENG);

        Label label = new Label(composite, SWT.NONE);
        label.setText(Labels.PREFERENCE_DAO_TYPE);
        this.daoType = new Combo(composite, SWT.READ_ONLY);
        this.daoType.setItems(Constants.DAO_TYPES);
        this.daoType.select(0);

        this.usePageMarker = new Button(createDefaultComposite(composite),
                SWT.CHECK);
        this.usePageMarker.setText(Labels.PREFERENCE_USE_PAGE_MARKER);

        label = new Label(composite, SWT.NONE);
        label.setText(Labels.PREFERENCE_DEFAULT_ENTITY_PKG);
        this.defaultEntityPkg = new Text(composite, SWT.SINGLE | SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        this.defaultEntityPkg.setLayoutData(data);
        Button entBtn = new Button(composite, SWT.PUSH);
        entBtn.setText(Labels.BROWSE);
        entBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                choosePkg(DoltengProjectPreferencePage.this.defaultEntityPkg);
            }
        });

        label = new Label(composite, SWT.NONE);
        label.setText(Labels.PREFERENCE_DEFAULT_DAO_PKG);
        this.defaultDaoPkg = new Text(composite, SWT.SINGLE | SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        this.defaultDaoPkg.setLayoutData(data);
        Button daoBtn = new Button(composite, SWT.PUSH);
        daoBtn.setText(Labels.BROWSE);
        daoBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                choosePkg(DoltengProjectPreferencePage.this.defaultDaoPkg);
            }
        });

        label = new Label(composite, SWT.NONE);
        label.setText(Labels.PREFERENCE_DEFAULT_DTO_PKG);
        this.defaultDtoPkg = new Text(composite, SWT.SINGLE | SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        this.defaultDtoPkg.setLayoutData(data);
        Button dtoBtn = new Button(composite, SWT.PUSH);
        dtoBtn.setText(Labels.BROWSE);
        dtoBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                choosePkg(DoltengProjectPreferencePage.this.defaultDtoPkg);
            }
        });

        label = new Label(composite, SWT.NONE);
        label.setText(Labels.PREFERENCE_DEFAULT_WEB_PKG);
        this.defaultWebPkg = new Text(composite, SWT.SINGLE | SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        this.defaultWebPkg.setLayoutData(data);
        Button webBtn = new Button(composite, SWT.PUSH);
        webBtn.setText(Labels.BROWSE);
        webBtn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                choosePkg(DoltengProjectPreferencePage.this.defaultWebPkg);
            }
        });

        label = new Label(composite, SWT.NONE);
        label.setText("OrmXmlPath");
        this.ormXmlOutputPath = new Text(composite, SWT.SINGLE | SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        this.ormXmlOutputPath.setLayoutData(data);
        Button outpath = new Button(composite, SWT.PUSH);
        outpath.setText(Labels.BROWSE);
        outpath.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                ResourceTreeSelectionDialog dialog = new ResourceTreeSelectionDialog(
                        getShell(), getSelectedProject().getParent(),
                        IResource.FOLDER | IResource.PROJECT);
                dialog.setInitialSelection(getSelectedProject());
                dialog.setAllowMultiple(false);
                if (dialog.open() == Dialog.OK) {
                    Object[] results = dialog.getResult();
                    if (results != null && 0 < results.length) {
                        IResource r = (IResource) results[0];
                        ormXmlOutputPath.setText(r.getFullPath().toString());
                    }
                }
            }
        });

        setUpStoredValue();

        return composite;
    }

    private void choosePkg(Text txt) {
        try {
            SelectionDialog dialog = JavaUI.createPackageDialog(getShell(),
                    JavaCore.create(getSelectedProject()), 0);
            if (dialog.open() == Window.OK) {
                Object[] result = dialog.getResult();
                if (result != null && 0 < result.length) {
                    IPackageFragment pkg = (IPackageFragment) result[0];
                    txt.setText(pkg.getElementName());
                }
            }
        } catch (CoreException e) {
            DoltengCore.log(e);
        }
    }

    private Composite createDefaultComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        composite.setLayout(layout);

        GridData data = new GridData();
        data.verticalAlignment = GridData.FILL;
        data.horizontalAlignment = GridData.FILL;
        data.horizontalSpan = 3;
        composite.setLayoutData(data);

        return composite;
    }

    private void setUpStoredValue() {
        IProject project = getSelectedProject();
        if (project != null) {
            this.useDolteng.setSelection(ProjectUtil.hasNature(project,
                    Constants.ID_NATURE));
        }
        DoltengProjectPreferences pref = DoltengCore.getPreferences(project);
        if (pref != null) {
            this.daoType.setText(pref.getDaoType());
            this.usePageMarker.setSelection(pref.isUsePageMarker());
            this.defaultDtoPkg.setText(pref.getRawPreferences().getString(
                    Constants.PREF_DEFAULT_DTO_PACKAGE));
            this.defaultDaoPkg.setText(pref.getRawPreferences().getString(
                    Constants.PREF_DEFAULT_DAO_PACKAGE));
            this.defaultEntityPkg.setText(pref.getRawPreferences().getString(
                    Constants.PREF_DEFAULT_ENTITY_PACKAGE));
            this.defaultWebPkg.setText(pref.getRawPreferences().getString(
                    Constants.PREF_DEFAULT_WEB_PACKAGE));
            this.ormXmlOutputPath
                    .setText(pref.getOrmXmlOutputPath().toString());
        }
    }

    private IProject getSelectedProject() {
        IAdaptable adaptor = getElement();
        IProject project = null;
        if (adaptor instanceof IJavaProject) {
            IJavaProject javap = (IJavaProject) adaptor;
            project = javap.getProject();
        } else if (adaptor instanceof IProject) {
            IProject p = (IProject) adaptor;
            IJavaProject javap = JavaCore.create(p);
            if (javap.exists()) {
                project = p;
            }
        }
        return project;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    protected void performDefaults() {
        IProject project = getSelectedProject();
        DoltengProjectPreferences pref = DoltengCore.getPreferences(project);
        if (pref != null) {
            this.defaultDaoPkg.setText(pref.getRawPreferences()
                    .getDefaultString(Constants.PREF_DEFAULT_DAO_PACKAGE));
            this.defaultEntityPkg.setText(pref.getRawPreferences()
                    .getDefaultString(Constants.PREF_DEFAULT_ENTITY_PACKAGE));
            this.usePageMarker.setSelection(true);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    public boolean performOk() {
        try {
            IProject project = getSelectedProject();
            if (project != null) {
                if (this.useDolteng.getSelection()) {
                    ProjectUtil.addNature(project, Constants.ID_NATURE);
                    DoltengProjectPreferences pref = DoltengCore
                            .getPreferences(project);
                    if (pref != null) {
                        pref.setDaoType(this.daoType.getText());
                        pref
                                .setUsePageMarker(this.usePageMarker
                                        .getSelection());
                        pref.getRawPreferences().setValue(
                                Constants.PREF_DEFAULT_DAO_PACKAGE,
                                this.defaultDaoPkg.getText());
                        pref.getRawPreferences().setValue(
                                Constants.PREF_DEFAULT_ENTITY_PACKAGE,
                                this.defaultEntityPkg.getText());
                        pref.setOrmXmlOutputPath(this.ormXmlOutputPath
                                .getText());
                        pref.getRawPreferences().save();
                    }
                } else {
                    ProjectUtil.removeNature(project, Constants.ID_NATURE);
                }
                if (this.usePageMarker.getSelection() == false) {
                    project.deleteMarkers(Constants.ID_HTML_MAPPER, true,
                            IResource.DEPTH_INFINITE);
                    project.deleteMarkers(Constants.ID_PAGE_MAPPER, true,
                            IResource.DEPTH_INFINITE);
                }
                DatabaseView.reloadView();
            }
            return true;
        } catch (Exception e) {
            DoltengCore.log(e);
            return false;
        }
    }

}
