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
package org.seasar.dolteng.eclipse.preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.part.DatabaseView;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.wigets.ResourceTreeSelectionDialog;
import org.seasar.eclipse.common.util.ExtensionAcceptor;

/**
 * @author taichi
 * 
 */
public class DoltengProjectPreferencePage extends PropertyPage {

    private Pattern httpUrl = Pattern
            .compile("https?://[-_.!~*'()a-zA-Z0-9;/?:\\@&=+\\$,%#]+");

    private Button useDolteng;

    private Combo viewType;

    private Combo daoType;

    private Button usePageMarker;

    private Button useDIMarker;

    private Button useSqlMarker;

    private Combo rootPackage;

    private Text ormXmlOutputPath;

    private Text defaultSrcPath;

    private Text defaultRscPath;

    private Text webServer;

    private Text flexSourceFolderPath;

    private Button isHelpRemote;

    public DoltengProjectPreferencePage() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    @SuppressWarnings("unchecked")
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

        List[] types = loadViewAndDaoTypes();

        Label label = new Label(composite, SWT.NONE);
        label.setText(Labels.PREFERENCE_VIEW_TYPE);
        this.viewType = new Combo(composite, SWT.READ_ONLY);
        this.viewType.setItems((String[]) types[0].toArray(new String[types[0]
                .size()]));
        this.viewType.select(0);
        label = new Label(composite, SWT.NONE);// empty space.

        label = new Label(composite, SWT.NONE);
        label.setText(Labels.PREFERENCE_DAO_TYPE);
        this.daoType = new Combo(composite, SWT.READ_ONLY);
        this.daoType.setItems((String[]) types[1].toArray(new String[types[1]
                .size()]));
        this.daoType.select(0);
        label = new Label(composite, SWT.NONE);// empty space.

        this.usePageMarker = new Button(createDefaultComposite(composite),
                SWT.CHECK);
        this.usePageMarker.setText(Labels.PREFERENCE_USE_PAGE_MARKER);

        this.useDIMarker = new Button(createDefaultComposite(composite),
                SWT.CHECK);
        this.useDIMarker.setText(Labels.PREFERENCE_USE_DI_MARKER);

        this.useSqlMarker = new Button(createDefaultComposite(composite),
                SWT.CHECK);
        this.useSqlMarker.setText(Labels.PREFERENCE_USE_SQL_MARKER);

        label = new Label(composite, SWT.NONE);
        label.setText(Labels.PREFERENCE_DEFAULT_ROOT_PKG);
        this.rootPackage = new Combo(composite, SWT.READ_ONLY);
        label = new Label(composite, SWT.NONE);// empty space.

        label = new Label(composite, SWT.NONE);
        label.setText(Labels.PREFERENCE_ORM_XML_PATH);
        this.ormXmlOutputPath = new Text(composite, SWT.SINGLE | SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        this.ormXmlOutputPath.setLayoutData(data);
        Button outpath = new Button(composite, SWT.PUSH);
        outpath.setText(Labels.BROWSE);
        outpath.addSelectionListener(new SelectionAdapter() {
            @Override
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

        label = new Label(composite, SWT.NONE);
        label.setText(Labels.PREFERENCE_SOURCE_PATH);
        this.defaultSrcPath = new Text(composite, SWT.SINGLE | SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        this.defaultSrcPath.setLayoutData(data);
        Button srcpath = new Button(composite, SWT.PUSH);
        srcpath.setText(Labels.BROWSE);
        srcpath.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ResourceTreeSelectionDialog dialog = new ResourceTreeSelectionDialog(
                        getShell(), getSelectedProject(), IResource.FOLDER);
                dialog.setInitialSelection(getSelectedProject());
                dialog.setAllowMultiple(false);
                if (dialog.open() == Dialog.OK) {
                    Object[] results = dialog.getResult();
                    if (results != null && 0 < results.length) {
                        IResource r = (IResource) results[0];
                        defaultSrcPath.setText(r.getFullPath().toString());
                    }
                }
            }
        });

        label = new Label(composite, SWT.NONE);
        label.setText(Labels.PREFERENCE_RESOURCE_PATH);
        this.defaultRscPath = new Text(composite, SWT.SINGLE | SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        this.defaultRscPath.setLayoutData(data);
        Button rscpath = new Button(composite, SWT.PUSH);
        rscpath.setText(Labels.BROWSE);
        rscpath.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                chooseFolder(defaultRscPath);
            }
        });

        label = new Label(composite, SWT.NONE);
        label.setText(Labels.PREFERENCE_WEB_SERVER);
        this.webServer = new Text(composite, SWT.SINGLE | SWT.BORDER);
        this.webServer.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                String port = webServer.getText();
                boolean is = httpUrl.matcher(port).matches();
                if (is) {
                    setErrorMessage(null);
                } else {
                    setErrorMessage(NLS.bind(Messages.ONLY_USE_VALID_URL,
                            "Web Server"));
                }
                setValid(is);
            }
        });
        data = new GridData(GridData.FILL_HORIZONTAL);
        this.webServer.setLayoutData(data);
        label = new Label(composite, SWT.NONE); // Spacer

        label = new Label(composite, SWT.NONE);
        label.setText(Labels.PREFERENCE_FLEX_SRC_FOLDER);
        this.flexSourceFolderPath = new Text(composite, SWT.SINGLE | SWT.BORDER);
        this.flexSourceFolderPath.setLayoutData(new GridData(
                GridData.FILL_HORIZONTAL));
        Button flexpath = new Button(composite, SWT.PUSH);
        flexpath.setText(Labels.BROWSE);
        flexpath.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                chooseFolder(flexSourceFolderPath);
            }
        });

        this.isHelpRemote = new Button(createDefaultComposite(composite),
                SWT.CHECK);
        this.isHelpRemote.setText(Labels.PREFERENCE_IS_HELP_REMOTE);

        setUpStoredValue();

        return composite;
    }

    @SuppressWarnings("unchecked")
    private List[] loadViewAndDaoTypes() {
        final List<IConfigurationElement> views = new ArrayList<IConfigurationElement>();
        final List<IConfigurationElement> daos = new ArrayList<IConfigurationElement>();

        ExtensionAcceptor.accept(Constants.ID_PLUGIN, "projectType",
                new ExtensionAcceptor.ExtensionVisitor() {
                    public void visit(IConfigurationElement e) {
                        if ("viewtype".equals(e.getName())) {
                            views.add(e);
                        } else if ("daotype".equals(e.getName())) {
                            daos.add(e);
                        }
                    }
                });

        Comparator<IConfigurationElement> c = new Comparator<IConfigurationElement>() {
            public int compare(IConfigurationElement l, IConfigurationElement r) {
                String ls = l.getAttribute("id");
                String rs = r.getAttribute("id");
                return ls.compareTo(rs);
            }
        };

        Collections.sort(views, c);
        Collections.sort(daos, c);

        List[] types = new List[2];
        types[0] = new ArrayList(views.size());
        types[1] = new ArrayList(daos.size());

        for (IConfigurationElement e : views) {
            types[0].add(e.getAttribute("name"));
        }
        for (IConfigurationElement e : daos) {
            types[1].add(e.getAttribute("name"));
        }
        return types;
    }

    private void chooseFolder(Text txt) {
        ResourceTreeSelectionDialog dialog = new ResourceTreeSelectionDialog(
                getShell(), getSelectedProject(), IResource.FOLDER);
        dialog.setInitialSelection(getSelectedProject());
        dialog.setAllowMultiple(false);
        if (dialog.open() == Dialog.OK) {
            Object[] results = dialog.getResult();
            if (results != null && 0 < results.length) {
                IResource r = (IResource) results[0];
                txt.setText(r.getFullPath().toString());
            }
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
        DoltengPreferences pref = DoltengCore.getPreferences(project);
        if (pref != null) {
            this.viewType.setText(pref.getViewType());
            this.daoType.setText(pref.getDaoType());
            this.usePageMarker.setSelection(pref.isUsePageMarker());
            this.useDIMarker.setSelection(pref.isUseDIMarker());
            this.useSqlMarker.setSelection(pref.isUseSqlMarker());
            String[] names = pref.getNamingConvention().getRootPackageNames();
            if (names != null && 0 < names.length) {
                this.rootPackage.setItems(names);
                this.rootPackage.select(0);
            }
            this.rootPackage.setText(pref.getDefaultRootPackageName());
            this.ormXmlOutputPath
                    .setText(pref.getOrmXmlOutputPath().toString());
            this.defaultSrcPath.setText(pref.getDefaultSrcPath().toString());
            this.defaultRscPath.setText(pref.getDefaultResourcePath()
                    .toString());
            this.webServer.setText(pref.getWebServer());
            this.flexSourceFolderPath.setText(pref.getFlexSourceFolderPath()
                    .toString());
            this.isHelpRemote.setSelection(pref.isHelpRemote());
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
    @Override
    protected void performDefaults() {
        IProject project = getSelectedProject();
        DoltengPreferences pref = DoltengCore.getPreferences(project);
        if (pref != null) {
            this.usePageMarker.setSelection(true);
            this.useDIMarker.setSelection(true);
            this.useSqlMarker.setSelection(true);
            this.webServer.setText("http://localhost:8080");
            this.flexSourceFolderPath.setText("");
            this.isHelpRemote.setSelection(false);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        try {
            IProject project = getSelectedProject();
            if (project != null) {
                if (this.useDolteng.getSelection()) {
                    ProjectUtil.addNature(project, Constants.ID_NATURE);
                    if (Constants.VIEW_TYPE_FLEX2.equals(this.viewType
                            .getText())) {
                        ProjectUtil
                                .addNature(project, Constants.ID_NATURE_FLEX);
                    } else {
                        ProjectUtil.removeNature(project,
                                Constants.ID_NATURE_FLEX);
                    }

                    DoltengPreferences pref = DoltengCore
                            .getPreferences(project);
                    if (pref != null) {
                        pref.setViewType(this.viewType.getText());
                        pref.setDaoType(this.daoType.getText());
                        pref
                                .setUsePageMarker(this.usePageMarker
                                        .getSelection());
                        pref.setUseDIMarker(this.useDIMarker.getSelection());
                        pref.setUseSqlMarker(this.useSqlMarker.getSelection());
                        pref.setDefaultRootPackageName(this.rootPackage
                                .getText());
                        pref.setOrmXmlOutputPath(this.ormXmlOutputPath
                                .getText());
                        pref.setDefaultSrcPath(this.defaultSrcPath.getText());
                        pref.setDefaultResourcePath(this.defaultRscPath
                                .getText());
                        String port = this.webServer.getText();
                        if (httpUrl.matcher(port).matches()) {
                            pref.setWebServerPort(port);
                        }
                        pref.setFlexSourceFolderPath(this.flexSourceFolderPath
                                .getText());
                        pref.setHelpRemote(this.isHelpRemote.getSelection());
                        pref.getRawPreferences().save();
                    }
                } else {
                    ProjectUtil.removeNature(project, Constants.ID_NATURE);
                    ProjectUtil.removeNature(project, Constants.ID_NATURE_FLEX);
                }
                if (this.usePageMarker.getSelection() == false) {
                    project.deleteMarkers(Constants.ID_HTML_MAPPER, true,
                            IResource.DEPTH_INFINITE);
                    project.deleteMarkers(Constants.ID_PAGE_MAPPER, true,
                            IResource.DEPTH_INFINITE);
                }
                if (this.useDIMarker.getSelection() == false) {
                    project.deleteMarkers(Constants.ID_DI_MAPPER, true,
                            IResource.DEPTH_INFINITE);
                }
                if (this.useSqlMarker.getSelection() == false) {
                    project.deleteMarkers(Constants.ID_SQL_MAPPER, true,
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
