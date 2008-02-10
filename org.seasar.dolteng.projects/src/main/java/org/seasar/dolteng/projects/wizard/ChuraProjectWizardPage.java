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
package org.seasar.dolteng.projects.wizard;

import static org.seasar.dolteng.eclipse.Constants.CTX_JAVA_VERSION;
import static org.seasar.dolteng.eclipse.Constants.CTX_JRE_CONTAINER;
import static org.seasar.dolteng.eclipse.Constants.CTX_PACKAGE_NAME;
import static org.seasar.dolteng.eclipse.Constants.CTX_PACKAGE_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_PROJECT_NAME;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.internal.ui.preferences.CompliancePreferencePage;
import org.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathSupport;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Images;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.util.JREUtils;
import org.seasar.dolteng.projects.ProjectBuildConfigResolver;
import org.seasar.dolteng.projects.model.ApplicationType;
import org.seasar.dolteng.projects.model.FacetCategory;
import org.seasar.dolteng.projects.model.FacetConfig;
import org.seasar.dolteng.projects.model.FacetDisplay;
import org.seasar.framework.util.ArrayMap;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class ChuraProjectWizardPage extends WizardNewProjectCreationPage {

    private ProjectBuildConfigResolver resolver = new ProjectBuildConfigResolver();

    // UI Controls

    private Text rootPkgName;

    private Button useDefaultJre;

    private Button useProjectJre;

    private Combo jreCombo;

    private Combo applicationType;

    @SuppressWarnings("unchecked")
    private Map<String, Combo> facetCombos = new ArrayMap/* <String, Combo> */();

    @SuppressWarnings("unchecked")
    private List<Button> facetChecks = new ArrayList<Button>();

    private Label guidance;

    public ChuraProjectWizardPage() {
        super("ChuraProjectWizard");
        setTitle(Labels.WIZARD_CHURA_PROJECT_TITLE);
        setDescription(Messages.CHURA_PROJECT_DESCRIPTION);
        setImageDescriptor(Images.SEASAR);

        resolver.initialize();
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        Composite composite = (Composite) getControl();

        createJreContainerGroup(composite);
        createBasicSettingsGroup(composite);
        createFacetSettingsGroup(composite);

        refleshFacets();
    }

    private void createJreContainerGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setFont(parent.getFont());
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setLayout(new GridLayout(3, false));
        group.setText(Labels.WIZARD_PAGE_CHURA_JRE_CONTAINER);

        useDefaultJre = new Button(group, SWT.RADIO);
        useDefaultJre.setSelection(true);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        useDefaultJre.setLayoutData(gd);
        useDefaultJre.setText(Labels.bind(
                Labels.WIZARD_PAGE_CHURA_USE_DEFAULT_JRE, JREUtils
                        .getDefaultkey()));
        useDefaultJre.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                jreCombo.setEnabled(false);
                refleshFacets();
            }
        });

        Link preferenceLink = new Link(group, SWT.NONE);
        preferenceLink.setFont(group.getFont());
        preferenceLink
                .setText(NewWizardMessages.JavaProjectWizardFirstPage_JREGroup_link_description);
        preferenceLink.setLayoutData(new GridData(GridData.END,
                GridData.CENTER, false, false));
        preferenceLink.addSelectionListener(new SelectionListener() {
            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
             */
            public void widgetSelected(SelectionEvent e) {
                widgetDefaultSelected(e);
            }

            /*
             * (non-Javadoc)
             * 
             * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
             */
            public void widgetDefaultSelected(SelectionEvent e) {
                String jreID = BuildPathSupport.JRE_PREF_PAGE_ID;
                String complianceId = CompliancePreferencePage.PREF_ID;
                Map<String, Boolean> data = new HashMap<String, Boolean>();
                data.put(PropertyAndPreferencePage.DATA_NO_LINK, Boolean.TRUE);
                PreferencesUtil.createPreferenceDialogOn(getShell(), jreID,
                        new String[] { jreID, complianceId }, data).open();

                JREUtils.clear();
                jreCombo.removeAll();
                jreCombo.setItems(JREUtils.getKeyArray());
                jreCombo.select(0);
            }

        });

        useProjectJre = new Button(group, SWT.RADIO);
        useProjectJre.setLayoutData(new GridData());
        useProjectJre
                .setText(NewWizardMessages.JavaProjectWizardFirstPage_JREGroup_specific_compliance);
        useProjectJre.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                jreCombo.setEnabled(true);
                jreCombo.select(0);
                refleshFacets();
            }
        });

        jreCombo = new Combo(group, SWT.BORDER | SWT.READ_ONLY);
        jreCombo.setLayoutData(new GridData());
        jreCombo.setItems(JREUtils.getKeyArray());
        jreCombo.select(0);
        jreCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                refleshFacets();
            }
        });
        jreCombo.setEnabled(false);
    }

    private void createBasicSettingsGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setText(Labels.WIZARD_PAGE_CHURA_BASIC_SETTINGS);

        Label label = new Label(group, SWT.NONE);
        label.setText(Labels.WIZARD_PAGE_CHURA_TYPE_SELECTION);
        label.setFont(parent.getFont());
        applicationType = new Combo(group, SWT.BORDER | SWT.READ_ONLY);
        applicationType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        setApplicationTypeItems(applicationType);
        // applicationTypeCombo.setToolTipText(...);
        applicationType.select(0);
        applicationType.pack();
        applicationType.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                refleshFacets();
            }
        });

        label = new Label(group, SWT.NONE);
        label.setText(Labels.WIZARD_PAGE_CHURA_ROOT_PACKAGE);
        label.setFont(parent.getFont());

        rootPkgName = new Text(group, SWT.BORDER);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = 250;
        rootPkgName.setLayoutData(gd);
        rootPkgName.setFont(parent.getFont());
        rootPkgName.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                setPageComplete(validatePage());
                if (!isPageComplete()) {
                    setErrorMessage(validateRootPackageName());
                }
            }
        });
    }

    private void createFacetSettingsGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setText("Project Facet Settings");

        for (FacetCategory category : resolver.getCategoryList()) {
            Label label = new Label(group, SWT.NONE);
            label.setText(category.getName());
            label.setFont(parent.getFont());

            final Combo facetCombo = new Combo(group, SWT.BORDER
                    | SWT.READ_ONLY);
            facetCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            facetCombo.addListener(SWT.Modify, new Listener() {
                public void handleEvent(Event event) {
                    updateDirectories();
                    facetCombo.setToolTipText(getFacetDesc(facetCombo));
                    setPageComplete(validatePage());
                    // if (! isPageComplete()) {
                    // setErrorMessage(validateRootPackageName());
                    // }
                    displayLegacyTypeGuidance();
                }
            });
            facetCombos.put(category.getKey(), facetCombo);
        }

        List<FacetConfig> nonCategorizedFacets = getAvailableFacets();
        if (nonCategorizedFacets.size() != 0) {
            Group otherGroup = new Group(group, SWT.NONE);
            otherGroup.setLayout(new RowLayout(SWT.HORIZONTAL));
            GridData gd = new GridData(GridData.FILL_BOTH);
            gd.horizontalSpan = 2;
            otherGroup.setLayoutData(gd);
            otherGroup.setText("Other Category Facets");
            for (FacetConfig fc : nonCategorizedFacets) {
                Button facetCheck = new Button(otherGroup, SWT.CHECK);
                facetCheck.setText(fc.getName());
                // facetCheck.setToolTipText(fc.getDescription());
                facetCheck.setData(fc.getName(), fc);
                // Button#addListener(SWT.Modify, new Listener...); だと動かない。。
                facetCheck.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        updateDirectories();
                        // facetChecks.setToolTipText(getFacetDesc(facetChecks));
                        setPageComplete(validatePage());
                        // if (! isPageComplete()) {
                        // setErrorMessage(validateRootPackageName());
                        // }
                        displayLegacyTypeGuidance();
                    }
                });
                facetChecks.add(facetCheck);
            }
        }

        guidance = new Label(group, SWT.BORDER);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        guidance.setLayoutData(gd);
    }

    private void refleshFacets() {
        refleshFacetComboItems();
        refleshFacetChecks();
        setSelectedFacetIds(getApplicationType().getDefaultFacets());
    }

    private void refleshFacetComboItems() {
        for (FacetCategory category : resolver.getCategoryList()) {
            Combo facetCombo = facetCombos.get(category.getKey());
            List<FacetConfig> facets = getAvailableFacets(category);
            facetCombo.removeAll();
            facetCombo.add("None"); // TODO String外部化
            for (FacetConfig fc : facets) {
                facetCombo.add(fc.getName());
                facetCombo.setData(fc.getName(), fc);
            }
            facetCombo.setToolTipText(getFacetDesc(facetCombo));
            facetCombo.select(0);

            facetCombo.setEnabled(!getApplicationType().isDisabled(category));
            if (facetCombo.getEnabled() == false) {
                facetCombo.setText("None");
            }
        }
    }

    private void refleshFacetChecks() {
        for (Button facetCheck : facetChecks) {
            FacetConfig fc = getFacetConfig(facetCheck);
            if (getApplicationType().isDisabled(fc)) {
                facetCheck.setSelection(false);
                facetCheck.setEnabled(false);
            } else {
                facetCheck.setEnabled(true);
            }
        }
    }

    private List<FacetConfig> getAvailableFacets(FacetCategory category) {
        List<FacetConfig> result = new ArrayList<FacetConfig>();
        for (FacetConfig fc : resolver.getSelectableFacets()) {
            if (getApplicationType().isDisabled(fc)) {
                continue;
            }
            String categoryKey = fc.getCategory();
            if (category == null) {
                if (categoryKey == null
                        || resolver.getCategoryByKey(categoryKey) == null) {
                    result.add(fc);
                }
            } else {
                if (category.getKey().equals(categoryKey)) {
                    result.add(fc);
                }
            }
        }
        return result;
    }

    private List<FacetConfig> getAvailableFacets() {
        return getAvailableFacets(null);
    }

    private void setApplicationTypeItems(Combo applicationTypeCombo) {
        applicationTypeCombo.removeAll();
        for (ApplicationType type : resolver.getApplicationTypeList()) {
            applicationTypeCombo.add(type.getName());
            applicationTypeCombo.setData(type.getName(), type);
        }
    }

    protected void updateDirectories() {
        ChuraProjectWizardDirectoryPage dirPage = (ChuraProjectWizardDirectoryPage) getNextPage();
        try {
            Map<String, String> ctx = resolver.resolveProperty(
                    getSelectedFacetIds(), getJavaVersion());
            dirPage.setConfigureContext(ctx);
        } catch (CoreException e) {
            DoltengCore.log(e);
        }
    }

    private String getFacetDesc(Combo facetCombo) {
        if (facetCombo.getSelectionIndex() <= 0) {
            return "";
        }
        FacetDisplay fd = getFacetConfig(facetCombo);
        if (fd == null) {
            return "";
        }
        String desc = fd.getDescription();
        return desc == null ? "" : desc;
    }

    @Override
    protected boolean validatePage() {
        return super.validatePage()
                && StringUtil.isEmpty(validateRootPackageName());
    }

    /**
     * 入力されたパッケージ名のバリデーション。
     * 
     * @return 正当な場合<code>null</code>、エラーの場合はエラーメッセージを返す。
     */
    protected String validateRootPackageName() {
        String name = getRootPackageName();
        if (StringUtil.isEmpty(name)) {
            return Messages.PACKAGE_NAME_IS_EMPTY;
        }
        IStatus pkgNameStatus = JavaConventions.validatePackageName(name);
        if (pkgNameStatus.getSeverity() == IStatus.ERROR
                || pkgNameStatus.getSeverity() == IStatus.WARNING) {
            return NLS.bind(Messages.INVALID_PACKAGE_NAME, pkgNameStatus
                    .getMessage());
        }
        return null;
    }

    private FacetConfig getFacetConfig(Control facetControl) {
        String text;
        if (facetControl instanceof Button) {
            text = ((Button) facetControl).getText();
        } else if (facetControl instanceof Combo) {
            text = ((Combo) facetControl).getText();
        } else {
            throw new IllegalArgumentException();
        }
        return (FacetConfig) facetControl.getData(text);
    }

    private ApplicationType getApplicationType() {
        return resolver.getApplicationTypeList().get(
                applicationType.getSelectionIndex());
    }

    private String getJREContainer() {
        String key = null;
        if (useProjectJre.getSelection()) {
            key = jreCombo.getText();
        }
        return JREUtils.getJREContainer(key);
    }

    private String getJavaVersion() {
        String key = null;
        if (useProjectJre.getSelection()) {
            key = jreCombo.getText();
        }
        return JREUtils.getJavaVersion(key, JREUtils.VersionLength.SHORT);
    }

    private String getRootPackageName() {
        if (rootPkgName == null) {
            return "";
        }
        return rootPkgName.getText();
    }

    private String getRootPackagePath() {
        return getRootPackageName().replace('.', '/');
    }

    private void deselectAll() {
        for (Combo facetCombo : facetCombos.values()) {
            facetCombo.select(0);
        }
        for (Button facetCheck : facetChecks) {
            facetCheck.setSelection(false);
        }
    }

    private void displayLegacyTypeGuidance() {
        if (guidance == null || guidance.isDisposed()) {
            return;
        }
        String legacyProject = null;
        if (checkProject("Web Application", "web", "teedaPage", "s2dao",
                "sysdeo")) {
            legacyProject = "Super Agile (Teeda + S2Dao)";
        } else if (checkProject("Web Application", "web", "teeda",
                "kuinaHibernate", "sysdeo")) {
            legacyProject = "Easy Enterprise (Teeda + Kuina-Dao)";
        } else if (checkProject("Web Application", "web", "teeda", "s2jmsOut",
                "sysdeo")) {
            legacyProject = "Easy Enterprise (Teeda + S2JMS)";
        } else if (checkProject("Web Application", "web", "teeda",
                "kuinaHibernate", "s2jmsOut", "sysdeo")) {
            legacyProject = "Easy Enterprise (Teeda + Kuina-Dao + S2JMS)";
        } else if (checkProject("Web Application", "web", "teedaAction",
                "sysdeo")) {
            legacyProject = "Teeda Only";
        } else if (checkProject("Web Application", "web", "s2dao")) {
            legacyProject = "S2Dao Only";
        } else if (checkProject("Web Application", "web", "kuinaHibernate")) {
            legacyProject = "Kuina-Dao Only";
        } else if (checkProject("S2JMS-Inbound Application", "s2jmsInFirst",
                "s2jmsOut", "s2jmsInLast")) {
            legacyProject = "S2JMS Only";
        } else if (checkProject("S2JMS-Inbound Application", "s2jmsInFirst",
                "s2jmsOut", "kuinaHibernate", "s2jmsInLast")) {
            legacyProject = "S2JMS + Kuina-Dao";
        } else if (checkProject("Web Application", "web", "s2flex2", "s2dao",
                "sysdeo")) {
            legacyProject = "S2Flex2 + S2Dao";
        }

        if (legacyProject == null) {
            guidance.setText("");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("Legacy \"").append(legacyProject).append(
                    "\" Project Compatible.");
            guidance.setText(sb.toString());
        }
    }

    private boolean checkProject(String appType, String... elements) {
        if (!getApplicationType().getName().equals(appType)) {
            return false;
        }
        List<String> selected = Arrays.asList(getSelectedFacetIds());
        if (selected.size() != elements.length) {
            return false;
        }
        for (String e : elements) {
            if ("teeda".equals(e)) {
                if (!(selected.contains("teedaPage") || selected
                        .contains("teedaAction"))) {
                    return false;
                }
            } else {
                if (!selected.contains(e)) {
                    return false;
                }
            }
        }

        return true;
    }

    private void setSelectedFacetIds(String[] facetIds) {
        deselectAll();
        outer: for (String facetId : facetIds) {
            if (getApplicationType().getFirstFacets().contains(facetId)
                    || getApplicationType().getLastFacets().contains(facetId)) {
                continue;
            }
            for (Combo facetCombo : facetCombos.values()) {
                for (int i = 0; i < facetCombo.getItems().length; i++) {
                    String name = facetCombo.getItems()[i];
                    FacetConfig fc = (FacetConfig) facetCombo.getData(name);
                    if (fc != null && facetId.equals(fc.getId())) {
                        facetCombo.select(i);
                        continue outer;
                    }
                }
            }
            for (Button facetCheck : facetChecks) {
                FacetConfig fc = getFacetConfig(facetCheck);
                if (fc != null && facetId.equals(fc.getId())) {
                    facetCheck.setSelection(true);
                    continue outer;
                }
            }
        }
        displayLegacyTypeGuidance();
    }

    String[] getSelectedFacetIds() {
        List<String> keys = new ArrayList<String>(getApplicationType()
                .getFirstFacets());
        for (Combo facetCombo : facetCombos.values()) {
            // TODO: disableなコンボは無視したい。facetCombo.isEnabled()では判断できない。
            if (facetCombo.getSelectionIndex() < 0) {
                continue;
            }
            FacetDisplay fd = getFacetConfig(facetCombo);
            if (fd != null) {
                keys.add(fd.getId());
            }
        }
        for (Button facetCheck : facetChecks) {
            if (facetCheck.getSelection()) {
                FacetDisplay fd = getFacetConfig(facetCheck);
                if (fd != null) {
                    keys.add(fd.getId());
                }
            }
        }
        keys.addAll(getApplicationType().getLastFacets());
        return keys.toArray(new String[keys.size()]);
    }

    ProjectBuildConfigResolver getResolver() {
        return resolver;
    }

    Map<String, String> getConfigureContext() {
        Map<String, String> ctx = new HashMap<String, String>();

        ctx.put(CTX_PROJECT_NAME, getProjectName());
        ctx.put(CTX_PACKAGE_NAME, getRootPackageName());
        ctx.put(CTX_PACKAGE_PATH, getRootPackagePath());
        ctx.put(CTX_JRE_CONTAINER, getJREContainer());
        ctx.put(CTX_JAVA_VERSION, getJavaVersion());

        return ctx;
    }
}