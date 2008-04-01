package org.seasar.dolteng.projects.wizard;

import static org.seasar.dolteng.eclipse.Constants.CTX_LIB_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_LIB_SRC_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_MAIN_JAVA_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_MAIN_OUT_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_MAIN_RESOURCE_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_TEST_JAVA_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_TEST_LIB_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_TEST_LIB_SRC_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_TEST_OUT_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_TEST_RESOURCE_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_WEBAPP_ROOT;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.projects.Constants;

/**
 * @author daisuke
 */
public class ChuraProjectWizardDirectoryPage extends WizardPage {

    private static final Pattern pathPatterh = Pattern
            .compile("^[^\\\\/:*?\"<>\\t|]+(/[^\\\\/:*?\"<>|\\t]+)*$");

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

    private Listener validateListener = new Listener() {
        public void handleEvent(Event event) {
            boolean valid = validatePage();
            setPageComplete(valid);
            System.out.println("validation: " + valid);
        }
    };

    protected boolean validatePage() {
        boolean valid = true;
        valid = valid && validatePath(libPath.getText());
        valid = valid && validatePath(libSrcPath.getText());
        valid = valid && validatePath(testLibPath.getText());
        valid = valid && validatePath(testLibSrcPath.getText());
        valid = valid && validatePath(mainJavaPath.getText());
        valid = valid && validatePath(mainResourcePath.getText());
        valid = valid && validatePath(mainOutputPath.getText());
        valid = valid && validatePath(webappRootPath.getText());
        valid = valid && validatePath(testJavaPath.getText());
        valid = valid && validatePath(testResourcePath.getText());
        valid = valid && validatePath(testOutputPath.getText());

        if (valid) {
            setErrorMessage(null);
            setMessage(null);
        }
        return valid;
    }

    private boolean validatePath(String text) {
        boolean valid = pathPatterh.matcher(text).matches();
        if (valid == false) {
            setErrorMessage("invalid path: " + text);
        }
        return valid;
    }

    public ChuraProjectWizardDirectoryPage() {
        super("ChuraProjectWizard - Directories");

        setTitle(Labels.WIZARD_CHURA_PROJECT_TITLE);
        setDescription(Messages.CHURA_PROJECT_DESCRIPTION);
    }

    /**
     * Override method.
     * 
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        Group group = new Group(composite, SWT.NONE);
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setText(Labels.WIZARD_PAGE_CHURA_PROJECT_LAYOUT);

        libPath = createField(group, Labels.WIZARD_PAGE_CHURA_LIB_PATH);
        libSrcPath = createField(group, Labels.WIZARD_PAGE_CHURA_LIB_SRC_PATH);
        testLibPath = createField(group, Labels.WIZARD_PAGE_CHURA_TEST_LIB_PATH);
        testLibSrcPath = createField(group,
                Labels.WIZARD_PAGE_CHURA_TEST_LIB_SRC_PATH);
        mainJavaPath = createField(group,
                Labels.WIZARD_PAGE_CHURA_MAIN_JAVA_PATH);
        mainResourcePath = createField(group,
                Labels.WIZARD_PAGE_CHURA_MAIN_RESOURCE_PATH);
        mainOutputPath = createField(group,
                Labels.WIZARD_PAGE_CHURA_MAIN_OUT_PATH);
        webappRootPath = createField(group,
                Labels.WIZARD_PAGE_CHURA_WEBAPP_ROOT);
        testJavaPath = createField(group,
                Labels.WIZARD_PAGE_CHURA_TEST_JAVA_PATH);
        testResourcePath = createField(group,
                Labels.WIZARD_PAGE_CHURA_TEST_RESOURCE_PATH);
        testOutputPath = createField(group,
                Labels.WIZARD_PAGE_CHURA_TEST_OUT_PATH);

        setConfigureContext(Constants.DEFAULT_CONFIGURE_CONTEXT);

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
        setControl(group);
    }

    private Text createField(Composite parent, String labelStr) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(labelStr);
        label.setFont(parent.getFont());

        Text field = new Text(parent, SWT.BORDER);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = 250;
        field.setLayoutData(gd);
        field.setFont(parent.getFont());
        field.setText("");
        field.addListener(SWT.Modify, validateListener);

        return field;
    }

    /**
     * フィールドからコンテキスト情報を取得します。
     * 
     * @return コンテキスト情報
     */
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

    /**
     * フィールドにコンテキスト情報を設定します。
     * 
     * @param ctx
     *            コンテキスト情報
     */
    void setConfigureContext(Map<String, String> ctx) {
        try {
            libPath.setText(getContextData(ctx, CTX_LIB_PATH));
            libSrcPath.setText(getContextData(ctx, CTX_LIB_SRC_PATH));
            testLibPath.setText(getContextData(ctx, CTX_TEST_LIB_PATH));
            testLibSrcPath.setText(getContextData(ctx, CTX_TEST_LIB_SRC_PATH));
            mainJavaPath.setText(getContextData(ctx, CTX_MAIN_JAVA_PATH));
            mainResourcePath
                    .setText(getContextData(ctx, CTX_MAIN_RESOURCE_PATH));
            mainOutputPath.setText(getContextData(ctx, CTX_MAIN_OUT_PATH));
            webappRootPath.setText(getContextData(ctx, CTX_WEBAPP_ROOT));
            testJavaPath.setText(getContextData(ctx, CTX_TEST_JAVA_PATH));
            testResourcePath
                    .setText(getContextData(ctx, CTX_TEST_RESOURCE_PATH));
            testOutputPath.setText(getContextData(ctx, CTX_TEST_OUT_PATH));
        } catch (NullPointerException e) {
            // TODO ダサい対策orz
            // ウィザード起動時にDirectoryPageのControlが作られる前に呼び出しが起きてしまうのでignore。
        }
    }

    private String getContextData(Map<String, String> ctx, String key) {
        String result = ctx.get(key);
        if (result == null) {
            result = Constants.DEFAULT_CONFIGURE_CONTEXT.get(key);
        }
        if (result == null) {
            result = "";
        }
        return result;
    }
}
