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
import org.seasar.dolteng.projects.model.ProjectConfig;
import org.seasar.dolteng.projects.model.ProjectDisplay;
import org.seasar.framework.util.ArrayMap;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class ChuraProjectWizardPage extends WizardNewProjectCreationPage {
	
	private ArrayMap/*<String, ProjectConfig>*/ availableProjectTypes = new ArrayMap/*<String, ProjectConfig>*/();

	private Map<String, ArrayMap/*<String, ProjectConfig>*/> projectMap;
	
	@SuppressWarnings("unchecked")
	private Map<String, String> categoryMap = new ArrayMap/*<String, String>*/();

	private ProjectBuildConfigResolver resolver = new ProjectBuildConfigResolver();

	private Text rootPkgName;

	private Button useDefaultJre;

	private Button selectJre;

	private Combo availableJres;

	@SuppressWarnings("unchecked")
	private Map<String, Combo> projectTypeCombos = new ArrayMap/*<String, Combo>*/();

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
		projectMap = resolver.getProjectMap();
		setJre(JREUtils.getDefaultJavaVersion(JREUtils.SHORT));
		
		categoryMap.put("PR", "Presentation");	// TODO String外部化
		categoryMap.put("PE", "Persistance");
		categoryMap.put("CO", "Communication");
		categoryMap.put("IM", "Server Management");
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
		
		createRootPackageUISection(basic);
		createDirectoryUISection(detail);
		createJreContainerUISection(basic);
		createProjectTypeUISection(basic);
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
				JREUtils.getDefaultJavaVersion(JREUtils.FULL)));
		useDefaultJre.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				availableJres.setEnabled(false);
				selectJre(ChuraProjectWizardPage.this, JREUtils.getDefaultJavaVersion(JREUtils.SHORT));
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
				selectJre(ChuraProjectWizardPage.this);
			}
		});

		gd = new GridData();
		availableJres = new Combo(group, SWT.BORDER | SWT.READ_ONLY);
		availableJres.setLayoutData(gd);
		availableJres.setItems(JREUtils.getKeyArray());
		availableJres.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectJre(ChuraProjectWizardPage.this);
			}
		});
		availableJres.setEnabled(false);
	}

	private void setJre(String shortVersion) {
		if(shortVersion == null) {
			shortVersion = JREUtils.getDefaultJavaVersion(JREUtils.SHORT);
		}
		ArrayMap/*<String, ProjectConfig>*/ map = projectMap.get(shortVersion);
		if(map != null) {
			availableProjectTypes = map;
		}
	}

	private static void selectJre(ChuraProjectWizardPage page) {
		selectJre(page, JREUtils.getJavaVersion(page.availableJres.getText(), JREUtils.SHORT));
	}

	private static void selectJre(ChuraProjectWizardPage page, String version) {
		page.setJre(version);
		for(Map.Entry<String, Combo> e : page.projectTypeCombos.entrySet()) {
			Combo projectTypeCombo = e.getValue();
			page.setProjectItems(e.getKey(), projectTypeCombo);
			projectTypeCombo.select(0);
		}
	}

	private void createProjectTypeUISection(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		for(Map.Entry<String, String> e : categoryMap.entrySet()) {
			Label label = new Label(composite, SWT.NONE);
			label.setText(e.getValue());	// Labels.WIZARD_PAGE_CHURA_TYPE_SELECTION);
			label.setFont(parent.getFont());
	
			final Combo projectTypeCombo = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
			projectTypeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			setProjectItems(e.getKey(), projectTypeCombo);
			projectTypeCombo.setToolTipText(getProjectTypeDesc(projectTypeCombo));
			projectTypeCombo.select(0);
			projectTypeCombo.pack();
			projectTypeCombos.put(e.getKey(), projectTypeCombo);
			
			projectTypeCombo.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event event) {
					projectTypeCombo.setToolTipText(getProjectTypeDesc(projectTypeCombo));
					setPageComplete(validatePage());
	//				if (! isPageComplete()) {
	//					setErrorMessage(validateRootPackageName());
	//				}
				}
			});
		}
	}

	/**
	 * @param projectTypeCombo
	 */
	private void setProjectItems(String categoryKey, Combo projectTypeCombo) {
		Map<String, String> projectTypes = getProjectTypes(categoryKey);
		for(Map.Entry<String, String> e : projectTypes.entrySet()) {
			projectTypeCombo.add(e.getValue());
			projectTypeCombo.setData(e.getValue(), availableProjectTypes.get(e.getKey()));
		}
	}

	private Map<String, String> getProjectTypes(String categoryId) {
		@SuppressWarnings("unchecked")
		Map<String, String> result = new ArrayMap/*<String, String>*/();
		result.put("", "None");	// TODO 外部化
		for(Object/*Map.Entry<String, ProjectConfig>*/ e : availableProjectTypes.entrySet()) {
			ProjectConfig pc = (ProjectConfig) ((Map.Entry) e).getValue();
			if(pc.getCategory().equals(categoryId)) {
				result.put(pc.getId(), pc.getName());
			}
		}
		return result;
	}

	@Override
	protected boolean validatePage() {
		if(super.validatePage() && StringUtil.isEmpty(validateRootPackageName())) {
			for(Map.Entry<String, Combo> e : projectTypeCombos.entrySet()) {
				if(e.getValue().getSelectionIndex() > 0) {
					return true;
				}
			}
		}
		return false;
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

	private String getRootPackageName() {
		if (rootPkgName == null) {
			return "";
		}
		return rootPkgName.getText();
	}

	private String getRootPackagePath() {
		return getRootPackageName().replace('.', '/');
	}

	String[] getProjectTypeKeys() {
		List<String> keys = new ArrayList<String>();
		for(Map.Entry<String, Combo> e : projectTypeCombos.entrySet()) {
			if(e.getValue().getSelectionIndex() < 0) {
				continue;
			}
			String value = e.getValue().getText();
			ProjectDisplay pd = (ProjectDisplay) e.getValue().getData(value);
			if(pd != null) {
				keys.add(pd.getId());
			}
		}
		return keys.toArray(new String[keys.size()]);
	}

	private String getProjectTypeDesc(Combo projectTypeCombo) {
		if(projectTypeCombo.getSelectionIndex() <= 0) {
			return "";
		}
		String value = projectTypeCombo.getText();
		ProjectDisplay pd = (ProjectDisplay) projectTypeCombo.getData(value);
		if(pd == null) {
			return "";
		}
		String desc = pd.getDescription();
		return desc == null ? "" : desc;
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
		return JREUtils.getJavaVersion(key, JREUtils.SHORT);
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
