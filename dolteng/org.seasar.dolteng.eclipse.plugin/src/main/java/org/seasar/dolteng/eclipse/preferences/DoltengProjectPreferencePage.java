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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.PropertyPage;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.util.ProjectUtil;

/**
 * @author taichi
 * 
 */
public class DoltengProjectPreferencePage extends PropertyPage {

    private Button useDolteng;

    private Button useS2Dao;

    private Text defaultEntityPkg;

    private Text defaultDaoPkg;

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

        this.useS2Dao = new Button(createDefaultComposite(composite), SWT.CHECK);
        this.useS2Dao.setText(Labels.PREFERENCE_USE_S2DAO);

        Label label = new Label(composite, SWT.NONE);
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

        setUpStoredValue();

        return composite;
    }

    private void choosePkg(Text txt) {
        IJavaElement[] packages = null;
        try {
            Set pkgs = new HashSet();
            IPackageFragmentRoot[] froots = getPackageFragmentRoot();
            if (froots != null) {
                for (int i = 0; i < froots.length; i++) {
                    IPackageFragmentRoot froot = froots[i];
                    if (froot.exists()
                            && IPackageFragmentRoot.K_SOURCE == froot.getKind()) {
                        pkgs.addAll(Arrays.asList(froot.getChildren()));
                    }
                }
            }
            packages = (IJavaElement[]) pkgs.toArray(new IJavaElement[pkgs
                    .size()]);
        } catch (JavaModelException e) {
            DoltengCore.log(e);
        }
        if (packages == null) {
            packages = new IJavaElement[0];
        }

        ElementListSelectionDialog dialog = new ElementListSelectionDialog(
                getShell(), new JavaElementLabelProvider(
                        JavaElementLabelProvider.SHOW_DEFAULT));
        dialog.setIgnoreCase(false);
        dialog.setTitle(Labels.PACKAGE_SELECTION);
        dialog.setMessage(Labels.PACKAGE_SELECTION_DESC);
        dialog.setEmptyListMessage(Labels.PACKAGE_SELECTION_EMPTY);
        dialog.setElements(packages);

        if (dialog.open() == Window.OK) {
            IPackageFragment pkg = (IPackageFragment) dialog.getFirstResult();
            txt.setText(pkg.getElementName());
        }

    }

    private IPackageFragmentRoot[] getPackageFragmentRoot()
            throws JavaModelException {
        IProject proj = getSelectedProject();
        if (proj != null) {
            IJavaProject javap = JavaCore.create(proj);
            return javap.getPackageFragmentRoots();
        }
        return null;
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
            this.useS2Dao.setSelection(pref.isUseS2Dao());
            this.defaultDaoPkg.setText(pref.getRawPreferences().getString(
                    Constants.PREF_DEFAULT_DAO_PACKAGE));
            this.defaultEntityPkg.setText(pref.getRawPreferences().getString(
                    Constants.PREF_DEFAULT_ENTITY_PACKAGE));
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
                        pref.setUseS2Dao(this.useS2Dao.getSelection());
                        pref.getRawPreferences().setValue(
                                Constants.PREF_DEFAULT_DAO_PACKAGE,
                                this.defaultDaoPkg.getText());
                        pref.getRawPreferences().setValue(
                                Constants.PREF_DEFAULT_ENTITY_PACKAGE,
                                this.defaultEntityPkg.getText());
                    }
                } else {
                    ProjectUtil.removeNature(project, Constants.ID_NATURE);
                }
            }

            return true;
        } catch (CoreException e) {
            DoltengCore.log(e);
            return false;
        }
    }

}
