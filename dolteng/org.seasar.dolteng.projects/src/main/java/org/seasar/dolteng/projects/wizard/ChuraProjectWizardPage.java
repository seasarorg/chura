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

import java.util.Arrays;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
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
import org.seasar.dolteng.projects.ProjectBuildConfigResolver;
import org.seasar.dolteng.projects.model.ProjectDisplay;
import org.seasar.framework.util.ArrayMap;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class ChuraProjectWizardPage extends WizardNewProjectCreationPage {

    private Text rootPkgName;

    private Combo projectType;

    private Label projectDesc;

    private ArrayMap selectedProjectTypes = null;

    private ArrayMap mantisMap = new ArrayMap();

    private ArrayMap tigerMap = new ArrayMap();

    private Button useDefaultJre;

    private Button selectJre;

    private Combo availableJres;

    private ArrayMap jres = new ArrayMap();

    private ProjectBuildConfigResolver resolver = new ProjectBuildConfigResolver();

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

        setUpProjects(mantisMap, "1.4");
        setUpProjects(tigerMap, "1.5");

        String version = getDefaultJavaVersion();
        if (version != null && version.startsWith(JavaCore.VERSION_1_4)) {
            selectedProjectTypes = mantisMap;
        } else {
            selectedProjectTypes = tigerMap;
        }
    }

    private void setUpProjects(Map<String, ProjectDisplay> map, String jre) {
        ProjectDisplay[] projects = resolver.getProjects(jre);
        Arrays.sort(projects);
        for (ProjectDisplay project : projects) {
            map.put(project.getName(), project);
        }
    }

    private String getDefaultJavaVersion() {
        String version = JavaCore.getOption(JavaCore.COMPILER_COMPLIANCE);
        IVMInstall vm = JavaRuntime.getDefaultVMInstall();
        if (vm instanceof IVMInstall2) {
            IVMInstall2 vm2 = (IVMInstall2) vm;
            version = vm2.getJavaVersion();
        }
        return version;
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
                if (!isPageComplete()) {
                    setErrorMessage(validateRootPackageName());
                }
            }
        });
    }

    private void createDirectoryUISection(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        libPath = createField(composite, Labels.WIZARD_PAGE_CHURA_LIB_PATH,
                "src/main/webapp/WEB-INF/lib");
        libSrcPath = createField(composite,
                Labels.WIZARD_PAGE_CHURA_LIB_SRC_PATH,
                "src/main/webapp/WEB-INF/lib/sources");
        testLibPath = createField(composite,
                Labels.WIZARD_PAGE_CHURA_TEST_LIB_PATH, "lib");
        testLibSrcPath = createField(composite,
                Labels.WIZARD_PAGE_CHURA_TEST_LIB_SRC_PATH, "lib/sources");
        mainJavaPath = createField(composite,
                Labels.WIZARD_PAGE_CHURA_MAIN_JAVA_PATH, "src/main/java");
        mainResourcePath = createField(composite,
                Labels.WIZARD_PAGE_CHURA_MAIN_RESOURCE_PATH,
                "src/main/resources");
        mainOutputPath = createField(composite,
                Labels.WIZARD_PAGE_CHURA_MAIN_OUT_PATH,
                "src/main/webapp/WEB-INF/classes");
        webappRootPath = createField(composite,
                Labels.WIZARD_PAGE_CHURA_WEBAPP_ROOT, "src/main/webapp");
        testJavaPath = createField(composite,
                Labels.WIZARD_PAGE_CHURA_TEST_JAVA_PATH, "src/test/java");
        testResourcePath = createField(composite,
                Labels.WIZARD_PAGE_CHURA_TEST_RESOURCE_PATH,
                "src/test/resources");
        testOutputPath = createField(composite,
                Labels.WIZARD_PAGE_CHURA_TEST_OUT_PATH, "target/test-classes");

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

    private Text createField(Composite parent, String labelStr,
            String defaultValue) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(labelStr);
        label.setFont(parent.getFont());

        Text field = new Text(parent, SWT.BORDER);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = 250;
        field.setLayoutData(gd);
        field.setFont(parent.getFont());
        field.setText(defaultValue);

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
                getDefaultJavaVersion()));
        useDefaultJre.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                availableJres.setEnabled(false);
                selectJre(ChuraProjectWizardPage.this, getDefaultJavaVersion());
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

        for (IVMInstallType type : JavaRuntime.getVMInstallTypes()) {
            for (IVMInstall install : type.getVMInstalls()) {
                if (install instanceof IVMInstall2) {
                    IVMInstall2 vm2 = (IVMInstall2) install;
                    StringBuffer stb = new StringBuffer();
                    stb.append(install.getName());
                    stb.append(" (");
                    stb.append(vm2.getJavaVersion());
                    stb.append(")");
                    jres.put(stb.toString(), vm2);
                }
            }
        }
        String[] ary = new String[jres.size()];
        for (int i = 0; i < jres.size(); i++) {
            ary[i] = jres.getKey(i).toString();
        }
        availableJres.setItems(ary);
        availableJres.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                selectJre(ChuraProjectWizardPage.this);
            }
        });
        availableJres.setEnabled(false);
    }

    private static void selectJre(ChuraProjectWizardPage page) {
        IVMInstall2 vm = (IVMInstall2) page.jres.get(page.availableJres
                .getText());
        selectJre(page, vm.getJavaVersion());
    }

    private static void selectJre(ChuraProjectWizardPage page, String version) {
        if (version != null && version.startsWith(JavaCore.VERSION_1_4)) {
            page.selectedProjectTypes = page.mantisMap;
        } else {
            page.selectedProjectTypes = page.tigerMap;
        }
        page.projectType.setItems(page.getProjectTypes());
        page.projectType.select(0);
    }

    private void createProjectTypeUISection(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label label = new Label(composite, SWT.NONE);
        label.setText(Labels.WIZARD_PAGE_CHURA_TYPE_SELECTION);
        label.setFont(parent.getFont());

        projectType = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        projectType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        projectType.setItems(getProjectTypes());
        projectType.select(0);
        projectType.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                projectDesc.setText(getProjectTypeDesc());
            }
        });
        projectType.pack();

        label = new Label(composite, SWT.NONE);
        label.setText(Labels.WIZARD_PAGE_CHURA_TYPE_DESCRIPTION);
        label.setFont(parent.getFont());

        projectDesc = new Label(composite, SWT.BORDER);
        projectDesc.setLayoutData(new GridData(GridData.FILL_BOTH));
        projectDesc.setText(getProjectTypeDesc());
    }

    private String[] getProjectTypes() {
        String[] ary = new String[selectedProjectTypes.size()];
        for (int i = 0; i < ary.length; i++) {
            ary[i] = selectedProjectTypes.getKey(i).toString();
        }
        return ary;
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

    public String getRootPackageName() {
        if (rootPkgName == null) {
            return "";
        }
        return rootPkgName.getText();
    }

    public String getRootPackagePath() {
        return getRootPackageName().replace('.', '/');
    }

    public String[] getProjectTypeKeys() {
        // FIXME 複合プロジェクトメカニズムの布石…
        return new String[] { getProjectTypeKey() };
    }

    private String getProjectTypeKey() {
        return ((ProjectDisplay) selectedProjectTypes
                .get(projectType.getText())).getId();
    }

    private String getProjectTypeDesc() {
        ProjectDisplay pd = ((ProjectDisplay) selectedProjectTypes
                .get(projectType.getText()));
        if (pd == null) {
            return "";
        }
        String desc = pd.getDescription();
        return desc == null ? "" : desc;
    }

    public String getJREContainer() {
        IPath path = new Path(JavaRuntime.JRE_CONTAINER);
        if (selectJre.getSelection()) {
            IVMInstall vm = (IVMInstall) jres.get(availableJres.getText());
            path = path.append(vm.getVMInstallType().getId());
            path = path.append(vm.getName());
        }
        return path.toString();
    }

    public ProjectBuildConfigResolver getResolver() {
        return resolver;
    }

    String getLibraryPath() {
        if (libPath == null) {
            return "";
        }
        return libPath.getText();
    }

    String getLibrarySourcePath() {
        if (libSrcPath == null) {
            return "";
        }
        return libSrcPath.getText();
    }

    String getTestLibraryPath() {
        if (testLibPath == null) {
            return "";
        }
        return testLibPath.getText();
    }

    String getTestLibrarySourcePath() {
        if (testLibSrcPath == null) {
            return "";
        }
        return testLibSrcPath.getText();
    }

    String getMainJavaPath() {
        if (mainJavaPath == null) {
            return "";
        }
        return mainJavaPath.getText();
    }

    String getMainResourcePath() {
        if (mainResourcePath == null) {
            return "";
        }
        return mainResourcePath.getText();
    }

    String getMainOutputPath() {
        if (mainOutputPath == null) {
            return "";
        }
        return mainOutputPath.getText();
    }

    String getWebappRootPath() {
        if (webappRootPath == null) {
            return "";
        }
        return webappRootPath.getText();
    }

    String getTestJavaPath() {
        if (testJavaPath == null) {
            return "";
        }
        return testJavaPath.getText();
    }

    String getTestResourcePath() {
        if (testResourcePath == null) {
            return "";
        }
        return testResourcePath.getText();
    }

    String getTestOutputPath() {
        if (testOutputPath == null) {
            return "";
        }
        return testOutputPath.getText();
    }
}
