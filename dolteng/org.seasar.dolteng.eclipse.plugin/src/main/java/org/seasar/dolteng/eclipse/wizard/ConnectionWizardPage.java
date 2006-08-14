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

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.wizard.WizardPage;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Images;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.operation.JdbcDriverFinder;
import org.seasar.dolteng.eclipse.preferences.ConnectionConfig;
import org.seasar.dolteng.eclipse.preferences.impl.ConnectionConfigImpl;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class ConnectionWizardPage extends WizardPage {

    private static final String[] EXTENSIONS = new String[] { "*.jar", "*.zip" };

    private static final String[] CHARSETS = new String[] { "Shift_JIS",
            "EUC-JP", "MS932", "UTF-8" };

    private Listener validationListener;

    private Combo name;

    private Text driverPath;

    private Combo driverClass;

    private Button driverFinder;

    private Text connectionUrl;

    private Text user;

    private Text pass;

    private Combo charset;

    /**
     * @param pageName
     */
    public ConnectionWizardPage(ChuraProjectWizardPage page) {
        super("ConnectionWizardPage");
        validationListener = new Listener() {
            public void handleEvent(Event event) {
                cleanErrorMessage();
            }
        };
    }

    protected ConnectionConfigImpl toConnectionConfig(
            IPersistentPreferenceStore store) {
        ConnectionConfigImpl cc = new ConnectionConfigImpl(store);
        cc.setName(this.name.getText());
        cc.setDriverPath(this.driverPath.getText());
        cc.setDriverClass(this.driverClass.getText());
        cc.setConnectionUrl(this.connectionUrl.getText());
        cc.setUser(this.user.getText());
        cc.setPass(this.pass.getText());
        cc.setCharset(this.charset.getText());
        return cc;
    }

    public void loadConfig(ConnectionConfig config) {
        if (config != null) {
            this.name.setText(config.getName());
            this.driverPath.setText(config.getDriverPath());
            this.driverClass.setEnabled(true);
            this.driverClass.add(config.getDriverClass());
            this.driverClass.select(0);
            this.connectionUrl.setText(config.getConnectionUrl());
            this.user.setText(config.getUser());
            this.pass.setText(config.getPass());
            this.charset.setText(config.getCharset());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        setImageDescriptor(Images.CONNECTION_WIZARD);

        Composite rootComposite = parent;

        setTitle(Labels.CONNECTION_DIALOG_TITLE);

        Composite composite = createMainLayout(rootComposite);

        createPartOfName(composite);

        createPartOfDriverPath(composite);

        createPartOfDriverClass(composite);

        createLabel(composite, Labels.CONNECTION_DIALOG_CONNECTION_URL);
        this.connectionUrl = new Text(composite, SWT.BORDER);
        this.connectionUrl.setLayoutData(createGridData());
        this.validators.add(new Validator() {
            public boolean validate() {
                Text t = ConnectionWizardPage.this.connectionUrl;
                return StringUtil.isEmpty(t.getText());
            }

            public String getMessage() {
                return Messages.CONNECTION_URL_EMPTY;
            }
        });

        this.connectionUrl.addListener(SWT.Modify, this.validationListener);

        createLabel(composite, Labels.CONNECTION_DIALOG_USER);
        this.user = new Text(composite, SWT.BORDER);
        this.user.setLayoutData(createGridData());

        createLabel(composite, Labels.CONNECTION_DIALOG_PASS);
        this.pass = new Text(composite, SWT.BORDER | SWT.PASSWORD);
        this.pass.setLayoutData(createGridData());

        createPartOfCharset(composite);

        Button test = new Button(composite, SWT.PUSH);
        // TODO 接続テストボタン。

        Label separator = new Label(rootComposite, SWT.HORIZONTAL
                | SWT.SEPARATOR);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        setControl(rootComposite);
    }

    private Composite createMainLayout(Composite rootComposite) {
        Composite composite = new Composite(rootComposite, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        composite.setLayout(layout);

        GridData data = new GridData();
        data.verticalAlignment = GridData.FILL;
        data.horizontalAlignment = GridData.FILL;
        composite.setLayoutData(data);
        return composite;
    }

    /**
     * @param composite
     */
    protected void createPartOfName(Composite composite) {
        createLabel(composite, Labels.CONNECTION_DIALOG_NAME);
        this.name = new Combo(composite, SWT.BORDER);
        this.name.setLayoutData(createGridData());

        this.validators.add(new Validator() {
            public boolean validate() {
                Combo t = ConnectionWizardPage.this.name;
                return StringUtil.isEmpty(t.getText());
            }

            public String getMessage() {
                return Messages.NAME_IS_EMPTY;
            }
        });

        this.name.addListener(SWT.Modify, this.validationListener);
    }

    /**
     * @param composite
     */
    protected void createPartOfDriverPath(Composite composite) {
        GridData data;
        createLabel(composite, Labels.CONNECTION_DIALOG_DRIVER_PATH);
        this.driverPath = new Text(composite, SWT.BORDER);
        data = createGridData();
        data.horizontalSpan = 1;
        data.widthHint = 250;
        this.driverPath.setLayoutData(data);

        this.validators.add(new Validator() {
            public boolean validate() {
                Text t = ConnectionWizardPage.this.driverPath;
                File f = new File(t.getText());
                boolean exists = f.exists();
                ConnectionWizardPage.this.driverFinder.setEnabled(exists);
                return exists == false;
            }

            public String getMessage() {
                return Messages.FILE_NOT_FOUND;
            }
        });

        this.driverPath.addListener(SWT.Modify, this.validationListener);

        this.driverPath.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                Text t = (Text) e.widget;
                File f = new File(t.getText());
                boolean is = false;
                if (is = f.exists()) {
                    cleanErrorMessage();
                } else {
                    setErrorMessage(Messages.FILE_NOT_FOUND);
                }
                ConnectionWizardPage.this.driverFinder.setEnabled(is);
            }
        });

        Button browse = new Button(composite, SWT.PUSH);
        browse.setText(Labels.CONNECTION_DIALOG_DRIVER_PATH_BROWSE);
        setButtonLayoutData(browse);
        browse.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(ConnectionWizardPage.this
                        .getShell());
                dialog.setFilterExtensions(EXTENSIONS);
                String path = dialog.open();
                if (StringUtil.isEmpty(path) == false) {
                    ConnectionWizardPage.this.driverPath.setText(path);
                }
            }
        });
    }

    /**
     * @param composite
     */
    protected void createPartOfDriverClass(Composite composite) {
        GridData data;
        createLabel(composite, Labels.CONNECTION_DIALOG_DRIVER_CLASS);
        this.driverClass = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        data = createGridData();
        data.horizontalSpan = 1;
        this.driverClass.setLayoutData(data);
        this.driverClass.setEnabled(false);

        this.validators.add(new Validator() {
            public boolean validate() {
                Combo t = ConnectionWizardPage.this.driverClass;
                return t.getEnabled() == false
                        || StringUtil.isEmpty(t.getText());
            }

            public String getMessage() {
                return Messages.DRIVER_CLASS_NOT_FOUND;
            }
        });

        this.driverClass.addListener(SWT.Modify, this.validationListener);

        this.driverFinder = new Button(composite, SWT.PUSH);
        this.driverFinder.setText(Labels.CONNECTION_DIALOG_DRIVER_CLASS_FIND);
        setButtonLayoutData(this.driverFinder);
        this.driverFinder.setEnabled(false);
        this.driverFinder.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                ProgressMonitorDialog dialog = new ProgressMonitorDialog(
                        ConnectionWizardPage.this.getShell());
                JdbcDriverFinder finder = new JdbcDriverFinder(
                        ConnectionWizardPage.this.driverPath.getText());
                try {
                    dialog.run(true, true, finder);
                    String[] ary = finder.getDriverClasses();
                    ConnectionWizardPage.this.driverClass.setItems(ary);
                    boolean is = false;
                    if (is = 0 < ary.length) {
                        ConnectionWizardPage.this.driverClass.select(0);
                        cleanErrorMessage();
                    } else {
                        setErrorMessage(Messages.DRIVER_CLASS_NOT_FOUND);
                    }
                    ConnectionWizardPage.this.driverClass.setEnabled(is);
                } catch (InterruptedException ex) {
                    setErrorMessage(ex.getMessage());
                } catch (Exception ex) {
                    DoltengCore.log(ex);
                }
            }
        });
    }

    /**
     * @param composite
     */
    protected void createPartOfCharset(Composite composite) {
        createLabel(composite, Labels.CONNECTION_DIALOG_CHARSET);
        this.charset = new Combo(composite, SWT.BORDER);
        this.charset.setLayoutData(createGridData());
        this.charset.setItems(CHARSETS);
        this.charset.setText(System.getProperty("file.encoding"));
        this.charset.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                Combo c = (Combo) e.widget;
                String s = c.getText();
                if (Charset.isSupported(s) == false) {
                    setErrorMessage(Messages.UNSUPPORTED_ENCODING);
                } else {
                    cleanErrorMessage();
                }
            }
        });
    }

    protected Label createLabel(Composite parent, String s) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(s);
        return label;
    }

    protected GridData createGridData() {
        GridData gd = new GridData();
        gd.verticalAlignment = GridData.FILL;
        gd.horizontalAlignment = GridData.FILL;
        gd.horizontalSpan = 2;
        return gd;
    }

    public void cleanErrorMessage() {
        setMessage("");
        for (final Iterator i = this.validators.iterator(); i.hasNext();) {
            Validator v = (Validator) i.next();
            if (v.validate()) {
                setErrorMessage(v.getMessage());
                return;
            }
        }
        setPageComplete(true);
    }

    private List validators = new ArrayList();

    private interface Validator {
        public boolean validate();

        public String getMessage();
    }
}
