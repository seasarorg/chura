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
import static org.seasar.dolteng.eclipse.Constants.CTX_LIB_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_LIB_SRC_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_MAIN_JAVA_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_MAIN_OUT_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_MAIN_RESOURCE_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_PACKAGE_NAME;
import static org.seasar.dolteng.eclipse.Constants.CTX_PACKAGE_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_PROJECT_NAME;
import static org.seasar.dolteng.eclipse.Constants.CTX_TEST_JAVA_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_TEST_LIB_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_TEST_LIB_SRC_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_TEST_OUT_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_TEST_RESOURCE_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_WEBAPP_ROOT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
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
	
	private List<FacetCategory> categoryList;

	private List<ApplicationType> applicationTypeList;

	private ProjectBuildConfigResolver resolver = new ProjectBuildConfigResolver();

	// Basic Settings
	
	private Text rootPkgName;

	private Button useDefaultJre;

	private Button selectJre;

	private Combo availableJres;
	
	private Combo applicationType;

	@SuppressWarnings("unchecked")
	private Map<String, Combo> facetCombos = new ArrayMap/*<String, Combo>*/();

	@SuppressWarnings("unchecked")
	private List<Button> facetChecks = new ArrayList<Button>();
	
	private Label guidance;

	// Detail Settings
	
	private Text libPath;

	private Text libSrcPath;

	private Text testLibPath;

	private Text testLibSrcPath;

	private Text mainJavaPath;
	
	private Text mainResourcePath;
	
	private Text mainOutputPath;

	private Text webappRootPath;
	
	private Text testJavaPath;
	
	private Text testResourcePath;
	
	private Text testOutputPath;

	@SuppressWarnings("unchecked")
	public ChuraProjectWizardPage() {
		super("ChuraProjectWizard");
		setTitle(Labels.WIZARD_CHURA_PROJECT_TITLE);
		setDescription(Messages.CHURA_PROJECT_DESCRIPTION);

		resolver.initialize();
		categoryList = resolver.getCategoryList();
		applicationTypeList = resolver.getApplicationTypeList();
	}
	
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		Composite composite = (Composite) getControl();
		
		TabFolder tabFolder = new TabFolder(composite, SWT.NONE);
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite basic = new Composite(tabFolder, SWT.NULL);
		basic.setLayout(new GridLayout(1, false));
		basic.setLayoutData(new GridData(GridData.FILL_BOTH));

		TabItem basicItem = new TabItem(tabFolder, SWT.NULL);
		basicItem.setText(Labels.WIZARD_PAGE_CHURA_BASIC_TAB);
		basicItem.setControl(basic);
		
		Composite detail = new Composite(tabFolder, SWT.NULL);
		detail.setLayout(new GridLayout(1, false));
		detail.setLayoutData(new GridData(GridData.FILL_BOTH));

		TabItem detailItem = new TabItem(tabFolder, SWT.NULL);
		detailItem.setText(Labels.WIZARD_PAGE_CHURA_DETAIL_TAB);
		detailItem.setControl(detail);
		
		createApplicationTypeUISection(basic);
		createRootPackageUISection(basic);
		createDirectoryUISection(detail);
		createJreContainerUISection(basic);
		createFacetUISection(basic);
		
		refleshFacets();
	}

	private void refleshFacets() {
		for(FacetCategory category : categoryList) {
			Combo facetCombo = facetCombos.get(category.getKey());
			setFacetComboItems(category, facetCombo);
			facetCombo.setToolTipText(getFacetDesc(facetCombo));
			facetCombo.select(0);
			
			facetCombo.setEnabled(! getApplicationType().isDisabled(category));
			if(facetCombo.getEnabled() == false) {
				facetCombo.setText("None");
			} else if("SM".equals(category.getKey())) {
				// ServerManagementのみ、デフォルトはSysdeo
				// TODO しかし…ここで対応するのも如何なものかー。
				facetCombo.setText("Sysdeo Tomcat Plugin");
			}
		}
		
		for(Button facetCheck : facetChecks) {
			setFacetCheck(facetCheck);
		}
	
	}

	private void setFacetComboItems(FacetCategory category, Combo facetCombo) {
		List<FacetConfig> facets = getAvailableCategorizedFacets(category.getKey());
		facetCombo.removeAll();
		facetCombo.add("None");	// TODO String外部化
		for(FacetConfig fc : facets) {
			facetCombo.add(fc.getName());
			facetCombo.setData(fc.getName(), fc);
		}
	}

	private void setFacetCheck(Button facetCheck) {
		FacetConfig fc = (FacetConfig) facetCheck.getData(facetCheck.getText());
		if(getApplicationType().isDisabled(fc)) {
			facetCheck.setSelection(false);
			facetCheck.setEnabled(false);
		} else {
			facetCheck.setEnabled(true);
		}
	}

	private List<FacetConfig> getAvailableCategorizedFacets(String categoryKey) {
		List<FacetConfig> result = new ArrayList<FacetConfig>();
		for(FacetConfig fc : resolver.getSelectableFacets()) {
			if(categoryKey.equals(fc.getCategory()) && ! getApplicationType().isDisabled(fc)) {
				result.add(fc);
			}
		}
		return result;
	}

	private List<FacetConfig> getNonCategorizedFacets() {
		List<FacetConfig> result = new ArrayList<FacetConfig>();
		for(FacetConfig fc : resolver.getSelectableFacets()) {
			String categoryKey = fc.getCategory();
			if(categoryKey == null || resolver.getCategoryByKey(categoryKey) == null) {
				result.add(fc);
			}
		}
		return result;
	}

	private void createApplicationTypeUISection(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label label = new Label(composite, SWT.NONE);
		label.setText(Labels.WIZARD_PAGE_CHURA_TYPE_SELECTION);
		label.setFont(parent.getFont());
		applicationType = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
		applicationType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		setApplicationTypeItems(applicationType);
//		applicationTypeCombo.setToolTipText(...);
		applicationType.select(0);
		applicationType.pack();
		applicationType.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				refleshFacets();
			}
		});
	}

	private void setApplicationTypeItems(Combo applicationTypeCombo) {
		applicationTypeCombo.removeAll();
		for(ApplicationType type : applicationTypeList) {
			applicationTypeCombo.add(type.getName());
			applicationTypeCombo.setData(type.getName(), type);
		}
	}

	private void createRootPackageUISection(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label label = new Label(composite, SWT.NONE);
		label.setText(Labels.WIZARD_PAGE_CHURA_ROOT_PACKAGE);
		label.setFont(parent.getFont());

		rootPkgName = new Text(composite, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 250;
		rootPkgName.setLayoutData(gd);
		rootPkgName.setFont(parent.getFont());
		rootPkgName.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				setPageComplete(validatePage());
				if (! isPageComplete()) {
					setErrorMessage(validateRootPackageName());
				}
			}
		});
	}
	
	private void createDirectoryUISection(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		libPath = createField(composite, Labels.WIZARD_PAGE_CHURA_LIB_PATH, "src/main/webapp/WEB-INF/lib");
		libSrcPath = createField(composite, Labels.WIZARD_PAGE_CHURA_LIB_SRC_PATH, "src/main/webapp/WEB-INF/lib/sources");
		testLibPath = createField(composite, Labels.WIZARD_PAGE_CHURA_TEST_LIB_PATH, "lib");
		testLibSrcPath = createField(composite, Labels.WIZARD_PAGE_CHURA_TEST_LIB_SRC_PATH, "lib/sources");
		mainJavaPath = createField(composite, Labels.WIZARD_PAGE_CHURA_MAIN_JAVA_PATH, "src/main/java");
		mainResourcePath = createField(composite, Labels.WIZARD_PAGE_CHURA_MAIN_RESOURCE_PATH, "src/main/resources");
		mainOutputPath = createField(composite, Labels.WIZARD_PAGE_CHURA_MAIN_OUT_PATH, "src/main/webapp/WEB-INF/classes");
		webappRootPath = createField(composite, Labels.WIZARD_PAGE_CHURA_WEBAPP_ROOT, "src/main/webapp");
		testJavaPath = createField(composite, Labels.WIZARD_PAGE_CHURA_TEST_JAVA_PATH, "src/test/java");
		testResourcePath = createField(composite, Labels.WIZARD_PAGE_CHURA_TEST_RESOURCE_PATH, "src/test/resources");
		testOutputPath = createField(composite, Labels.WIZARD_PAGE_CHURA_TEST_OUT_PATH, "target/test-classes");
		
		libPath.addListener(SWT.Modify, new ModifyListener());
		libSrcPath.addListener(SWT.Modify, new ModifyListener());
		testLibPath.addListener(SWT.Modify, new ModifyListener());
		testLibSrcPath.addListener(SWT.Modify, new ModifyListener());
		mainJavaPath.addListener(SWT.Modify, new ModifyListener());
		mainResourcePath.addListener(SWT.Modify, new ModifyListener());
		mainOutputPath.addListener(SWT.Modify, new ModifyListener());
		webappRootPath.addListener(SWT.Modify, new ModifyListener());
		testJavaPath.addListener(SWT.Modify, new ModifyListener());
		testResourcePath.addListener(SWT.Modify, new ModifyListener());
		testOutputPath.addListener(SWT.Modify, new ModifyListener());
		
		// FIXME 変更しても、現状の仕組みでは無効な為、編集不可とする。
		// 有効になり次第、このコメント以下、終了コメントまでを削除。
		libSrcPath.setEditable(false);
		testLibSrcPath.setEditable(false);
		libPath.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				libSrcPath.setText(libPath.getText() + "/sources");
			}
		});
		testLibPath.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				testLibSrcPath.setText(testLibPath.getText() + "/sources");
			}
		});
		// 削除終了
	}
	
	private class ModifyListener implements Listener {
		public void handleEvent(Event event) {
			event.widget.setData(true);
		}
	}

	private Text createField(Composite parent, String labelStr, String defaultValue) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(labelStr);
		label.setFont(parent.getFont());
		
		Text field = new Text(parent, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 250;
		field.setLayoutData(gd);
		field.setFont(parent.getFont());
		field.setText(defaultValue);
		field.setData(false);
		
		return field;
	}

	private void createJreContainerUISection(Composite parent) {
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		Group group = new Group(parent, SWT.NONE);
		group.setLayout(new GridLayout(2, false));
		group.setText(Labels.WIZARD_PAGE_CHURA_JRE_CONTAINER);
		group.setLayoutData(gd);

		gd = new GridData(GridData.FILL_BOTH);
		useDefaultJre = new Button(group, SWT.RADIO);
		useDefaultJre.setSelection(true);
		gd.horizontalSpan = 2;
		useDefaultJre.setLayoutData(gd);
		useDefaultJre.setText(Labels.bind(
				Labels.WIZARD_PAGE_CHURA_USE_DEFAULT_JRE,
				JREUtils.getDefaultJavaVersion(JREUtils.VersionLength.FULL)));
		useDefaultJre.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				availableJres.setEnabled(false);
				refleshFacets();
			}
		});

		gd = new GridData();
		selectJre = new Button(group, SWT.RADIO);
		selectJre.setLayoutData(gd);
		selectJre.setText("");
		selectJre.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				availableJres.setEnabled(true);
				availableJres.select(0);
				refleshFacets();
			}
		});

		gd = new GridData();
		availableJres = new Combo(group, SWT.BORDER | SWT.READ_ONLY);
		availableJres.setLayoutData(gd);
		availableJres.setItems(JREUtils.getKeyArray());
		availableJres.select(0);
		availableJres.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refleshFacets();
			}
		});
		availableJres.setEnabled(false);
	}

	private void createFacetUISection(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		for(FacetCategory category : categoryList) {
			Label label = new Label(composite, SWT.NONE);
			label.setText(category.getName());
			label.setFont(parent.getFont());
			
			final Combo facetCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
			facetCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			facetCombo.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event event) {
					facetCombo.setToolTipText(getFacetDesc(facetCombo));
					setPageComplete(validatePage());
//					if (! isPageComplete()) {
//						setErrorMessage(validateRootPackageName());
//					}
					displayLegacyTypeGuidance();
				}
			});
			facetCombos.put(category.getKey(), facetCombo);
		}
		
		List<FacetConfig> nonCategorizedFacets = getNonCategorizedFacets();
		if(nonCategorizedFacets.size() != 0) {
			Group group = new Group(composite, SWT.NONE);
			group.setText("Other Facet");
			group.setLayout(new RowLayout(SWT.HORIZONTAL));
			GridData gd = new GridData(GridData.FILL_BOTH);
			gd.horizontalSpan = 2;
			group.setLayoutData(gd);
			for(FacetConfig fc : nonCategorizedFacets) {
				Button facetCheck = new Button(group, SWT.CHECK);
				facetCheck.setText(fc.getName());
//				facetCheck.setToolTipText(fc.getDescription());
				facetCheck.setData(fc.getName(), fc);
//				facetCheck.addListener(SWT.Modify, new Listener() { ... }); だと動かない。。
				facetCheck.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
//						facetChecks.setToolTipText(getFacetDesc(facetChecks));
						setPageComplete(validatePage());
//						if (! isPageComplete()) {
//							setErrorMessage(validateRootPackageName());
//						}
						displayLegacyTypeGuidance();
					}
				});
				facetChecks.add(facetCheck);
			}
		}
		
		guidance = new Label(composite, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		guidance.setLayoutData(gd);
	}
	
	private String getFacetDesc(Combo facetCombo) {
		if(facetCombo.getSelectionIndex() <= 0) {
			return "";
		}
		String value = facetCombo.getText();
		FacetDisplay fd = (FacetDisplay) facetCombo.getData(value);
		if(fd == null) {
			return "";
		}
		String desc = fd.getDescription();
		return desc == null ? "" : desc;
	}

	private void displayLegacyTypeGuidance() {
		if(guidance == null || guidance.isDisposed()) {
			return;
		}
		String legacyProject = null;
		if (checkProject("teedaPage", "s2dao", "sysdeo")) {
			legacyProject = "Super Agile (Teeda + S2Dao)";
		} else if (checkProject("teeda", "kuinaHibernate", "sysdeo")) {
			legacyProject = "Easy Enterprise (Teeda + Kuina-Dao)";
		} else if (checkProject("teeda", "s2jmsOut", "sysdeo")) {
			legacyProject = "Easy Enterprise (Teeda + S2JMS)";
		} else if (checkProject("teeda", "kuinaHibernate", "s2jmsOut", "sysdeo")) {
			legacyProject = "Easy Enterprise (Teeda + Kuina-Dao + S2JMS)";
		} else if(checkProject("teedaAction", "sysdeo")) {
			legacyProject = "Teeda Only";
		} else if (checkProject("s2dao")) {
			legacyProject = "S2Dao Only";
		} else if (checkProject("kuinaHibernate")) {
			legacyProject = "Kuina-Dao Only";
		} else if (checkProject("s2jmsIn", "s2jmsOut")) {
			legacyProject = "S2JMS Only";
		} else if (checkProject("s2jmsIn", "s2jmsOut", "kuinaHibernate")) {
			legacyProject = "S2JMS + Kuina-Dao";
		} else if (checkProject("s2flex2", "s2dao", "sysdeo")) {
			legacyProject = "S2Flex2 + S2Dao";
		}
		
		if(legacyProject == null) {
			guidance.setText("");
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("Legacy \"").append(legacyProject).append("\" Project Compatible.");
			guidance.setText(sb.toString());
		}
	}
	
	private boolean checkProject(String... elements) {
		List<String> selected = Arrays.asList(getSelectedFacetIds());
		if(selected.size() != elements.length) {
			return false;
		}
		for(String e : elements) {
			if("teeda".equals(e)) {
				if(! (selected.contains("teedaPage") || selected.contains("teedaAction"))) {
					return false;
				}
			} else {
				if(! selected.contains(e)) {
					return false;
				}
			}
		}
		
		return true;
	}

	@Override
	protected boolean validatePage() {
		return super.validatePage() && StringUtil.isEmpty(validateRootPackageName());
	}

	/**
	 * 入力されたパッケージ名のバリデーション。
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
			return NLS.bind(Messages.INVALID_PACKAGE_NAME, pkgNameStatus.getMessage());
		}
		return null;
	}

	private ApplicationType getApplicationType() {
		return applicationTypeList.get(applicationType.getSelectionIndex());
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

	private String getJREContainer() {
		String key = null;
		if (selectJre.getSelection()) {
			key = availableJres.getText();
		}
		return JREUtils.getJREContainer(key);
	}
	
	private String getJavaVersion() {
		String key = null;
		if (selectJre.getSelection()) {
			key = availableJres.getText();
		}
		return JREUtils.getJavaVersion(key, JREUtils.VersionLength.SHORT);
	}

	String[] getSelectedFacetIds() {
		List<String> keys = new ArrayList<String>(getApplicationType().getBaseFacets());
		for(Combo facetCombo : facetCombos.values()) {
			// TODO: disableなコンボは無視したい。facetCombo.isEnabled()では判断できない。
			if(facetCombo.getSelectionIndex() < 0) {
				continue;
			}
			String value = facetCombo.getText();
			FacetDisplay fd = (FacetDisplay) facetCombo.getData(value);
			if(fd != null) {
				keys.add(fd.getId());
			}
		}
		for(Button facetCheck : facetChecks) {
			if(facetCheck.getSelection()) {
				String value = facetCheck.getText();
				FacetDisplay fd = (FacetDisplay) facetCheck.getData(value);
				if(fd != null) {
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
		ctx.put(CTX_LIB_PATH, libPath.getText());
		ctx.put(CTX_LIB_SRC_PATH, libSrcPath.getText());
		ctx.put(CTX_TEST_LIB_PATH, testLibPath.getText());
		ctx.put(CTX_TEST_LIB_SRC_PATH, testLibSrcPath.getText());
		ctx.put(CTX_MAIN_JAVA_PATH, mainJavaPath.getText());
		ctx.put(CTX_MAIN_RESOURCE_PATH, mainResourcePath.getText());
		ctx.put(CTX_MAIN_OUT_PATH, mainOutputPath.getText());
		ctx.put(CTX_WEBAPP_ROOT, webappRootPath.getText());
		ctx.put(CTX_TEST_JAVA_PATH, testJavaPath.getText());
		ctx.put(CTX_TEST_RESOURCE_PATH, testResourcePath.getText());
		ctx.put(CTX_TEST_OUT_PATH, testOutputPath.getText());
		ctx.put(CTX_JAVA_VERSION, getJavaVersion());
		
		return ctx;
	}

	Set<String> getEditContext() {
		Set<String> propertyNames = new HashSet<String>();
		
		// 編集済みマークをつけ、plugin.xmlでの上書き更新を禁止とする。
		propertyNames.add(CTX_PROJECT_NAME);
		propertyNames.add(CTX_PACKAGE_NAME);
		propertyNames.add(CTX_PACKAGE_PATH);
		propertyNames.add(CTX_JRE_CONTAINER);
		propertyNames.add(CTX_JAVA_VERSION);
		
		if((Boolean) libPath.getData()) {
			propertyNames.add(CTX_LIB_PATH);
		}
		if((Boolean) libSrcPath.getData()) {
			propertyNames.add(CTX_LIB_SRC_PATH);
		}
		if((Boolean) testLibPath.getData()) {
			propertyNames.add(CTX_TEST_LIB_PATH);
		}
		if((Boolean) testLibSrcPath.getData()) {
			propertyNames.add(CTX_TEST_LIB_SRC_PATH);
		}
		if((Boolean) mainJavaPath.getData()) {
			propertyNames.add(CTX_MAIN_JAVA_PATH);
		}
		if((Boolean) mainResourcePath.getData()) {
			propertyNames.add(CTX_MAIN_RESOURCE_PATH);
		}
		if((Boolean) mainOutputPath.getData()) {
			propertyNames.add(CTX_MAIN_OUT_PATH);
		}
		if((Boolean) webappRootPath.getData()) {
			propertyNames.add(CTX_WEBAPP_ROOT);
		}
		if((Boolean) testJavaPath.getData()) {
			propertyNames.add(CTX_TEST_JAVA_PATH);
		}
		if((Boolean) testResourcePath.getData()) {
			propertyNames.add(CTX_TEST_RESOURCE_PATH);
		}
		if((Boolean) testOutputPath.getData()) {
			propertyNames.add(CTX_TEST_OUT_PATH);
		}
		
		return propertyNames;
	}
}
