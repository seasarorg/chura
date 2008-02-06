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
import static org.seasar.dolteng.projects.Constants.DEFAULT_LIB_PATH;
import static org.seasar.dolteng.projects.Constants.DEFAULT_LIB_SRC_PATH;
import static org.seasar.dolteng.projects.Constants.DEFAULT_MAIN_JAVA_PATH;
import static org.seasar.dolteng.projects.Constants.DEFAULT_MAIN_OUT_PATH;
import static org.seasar.dolteng.projects.Constants.DEFAULT_MAIN_RESOURCE_PATH;
import static org.seasar.dolteng.projects.Constants.DEFAULT_TEST_JAVA_PATH;
import static org.seasar.dolteng.projects.Constants.DEFAULT_TEST_LIB_PATH;
import static org.seasar.dolteng.projects.Constants.DEFAULT_TEST_LIB_SRC_PATH;
import static org.seasar.dolteng.projects.Constants.DEFAULT_TEST_OUT_PATH;
import static org.seasar.dolteng.projects.Constants.DEFAULT_TEST_RESOURCE_PATH;
import static org.seasar.dolteng.projects.Constants.DEFAULT_WEBAPP_ROOT;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.nls.Messages;

/**
 * 
 * @author daisuke
 */
public class ChuraProjectWizardDirectoryPage extends WizardPage {

    private ChuraProjectWizardPage page;

    // UI Controls

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

    public ChuraProjectWizardDirectoryPage() {
        super("ChuraProjectWizard - Directories");

        setTitle(Labels.WIZARD_CHURA_PROJECT_TITLE);
        setDescription(Messages.CHURA_PROJECT_DESCRIPTION);

        this.page = (ChuraProjectWizardPage) getPreviousPage();
    }

    /**
     * Override method.
     * 
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        libPath = createField(composite, Labels.WIZARD_PAGE_CHURA_LIB_PATH,
                DEFAULT_LIB_PATH);
        libSrcPath = createField(composite,
                Labels.WIZARD_PAGE_CHURA_LIB_SRC_PATH, DEFAULT_LIB_SRC_PATH);
        testLibPath = createField(composite,
                Labels.WIZARD_PAGE_CHURA_TEST_LIB_PATH, DEFAULT_TEST_LIB_PATH);
        testLibSrcPath = createField(composite,
                Labels.WIZARD_PAGE_CHURA_TEST_LIB_SRC_PATH,
                DEFAULT_TEST_LIB_SRC_PATH);
        mainJavaPath = createField(composite,
                Labels.WIZARD_PAGE_CHURA_MAIN_JAVA_PATH, DEFAULT_MAIN_JAVA_PATH);
        mainResourcePath = createField(composite,
                Labels.WIZARD_PAGE_CHURA_MAIN_RESOURCE_PATH,
                DEFAULT_MAIN_RESOURCE_PATH);
        mainOutputPath = createField(composite,
                Labels.WIZARD_PAGE_CHURA_MAIN_OUT_PATH, DEFAULT_MAIN_OUT_PATH);
        webappRootPath = createField(composite,
                Labels.WIZARD_PAGE_CHURA_WEBAPP_ROOT, DEFAULT_WEBAPP_ROOT);
        testJavaPath = createField(composite,
                Labels.WIZARD_PAGE_CHURA_TEST_JAVA_PATH, DEFAULT_TEST_JAVA_PATH);
        testResourcePath = createField(composite,
                Labels.WIZARD_PAGE_CHURA_TEST_RESOURCE_PATH,
                DEFAULT_TEST_RESOURCE_PATH);
        testOutputPath = createField(composite,
                Labels.WIZARD_PAGE_CHURA_TEST_OUT_PATH, DEFAULT_TEST_OUT_PATH);

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
        setControl(composite);
    }

    private class ModifyListener implements Listener {
        public void handleEvent(Event event) {
            event.widget.setData(true);
            // ↑デフォルト設定ではない（編集された）事を意味するマーク。
            // 後に getEditContext() 内で Widget#getData() を用いる。
            // 編集された事を示す為に true （編集された）を設定。
        }
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
        field.setData(false);
        // ↑デフォルト設定ではない（編集された）事を意味するマーク。
        // 後に getEditContext() 内で Widget#getData() を用いる。
        // 初期値は false （編集されていない）を設定。

        return field;
    }

    Map<String, String> getConfigureContext() {
        Map<String, String> ctx = new HashMap<String, String>();

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

        if ((Boolean) libPath.getData()) {
            propertyNames.add(CTX_LIB_PATH);
        }
        if ((Boolean) libSrcPath.getData()) {
            propertyNames.add(CTX_LIB_SRC_PATH);
        }
        if ((Boolean) testLibPath.getData()) {
            propertyNames.add(CTX_TEST_LIB_PATH);
        }
        if ((Boolean) testLibSrcPath.getData()) {
            propertyNames.add(CTX_TEST_LIB_SRC_PATH);
        }
        if ((Boolean) mainJavaPath.getData()) {
            propertyNames.add(CTX_MAIN_JAVA_PATH);
        }
        if ((Boolean) mainResourcePath.getData()) {
            propertyNames.add(CTX_MAIN_RESOURCE_PATH);
        }
        if ((Boolean) mainOutputPath.getData()) {
            propertyNames.add(CTX_MAIN_OUT_PATH);
        }
        if ((Boolean) webappRootPath.getData()) {
            propertyNames.add(CTX_WEBAPP_ROOT);
        }
        if ((Boolean) testJavaPath.getData()) {
            propertyNames.add(CTX_TEST_JAVA_PATH);
        }
        if ((Boolean) testResourcePath.getData()) {
            propertyNames.add(CTX_TEST_RESOURCE_PATH);
        }
        if ((Boolean) testOutputPath.getData()) {
            propertyNames.add(CTX_TEST_OUT_PATH);
        }

        return propertyNames;
    }
}
