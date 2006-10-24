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
package org.seasar.dolteng.eclipse.wigets;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sql.XAConnection;

import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaElementSorter;
import org.eclipse.jdt.ui.StandardJavaElementContentProvider;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Images;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.operation.JdbcDriverFinder;
import org.seasar.dolteng.eclipse.preferences.ConnectionConfig;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.dolteng.eclipse.preferences.impl.ConnectionConfigImpl;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.extension.jdbc.util.ConnectionUtil;
import org.seasar.framework.util.StringUtil;
import org.seasar.framework.util.URLUtil;

/**
 * @author taichi
 * 
 */
public class ConnectionDialog extends TitleAreaDialog {
    // TODO Wizardにする。
    private static final String[] EXTENSIONS = new String[] { "*.jar", "*.zip" };

    private static final String[] CHARSETS = new String[] { "Shift_JIS",
            "EUC-JP", "MS932", "UTF-8" };

    private static final Class[] JAVA_PROJECTS = new Class[] {
            IJavaModel.class, IJavaProject.class };

    private FocusListener validationListener;

    private ConnectionConfig oldConfig;

    private IJavaProject dependentProject;

    private Text projectName;

    private Combo name;

    private Text driverPath;

    private Combo driverClass;

    private Button driverFinder;

    private Text connectionUrl;

    private Text user;

    private Text pass;

    private Combo charset;

    /**
     * @param parentShell
     */
    public ConnectionDialog(Shell parentShell) {
        super(parentShell);
        validationListener = new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                cleanErrorMessage();
            }
        };
    }

    protected ConnectionConfigImpl toConnectionConfig(
            IPersistentPreferenceStore store) {
        ConnectionConfigImpl cc = new ConnectionConfigImpl(store);
        cc.setName(this.name.getText());
        cc.setDriverPath(toEncodedPath(this.driverPath.getText()));
        cc.setDriverClass(this.driverClass.getText());
        cc.setConnectionUrl(this.connectionUrl.getText());
        cc.setUser(this.user.getText());
        cc.setPass(this.pass.getText());
        cc.setCharset(this.charset.getText());
        return cc;
    }

    private String toEncodedPath(String path) {
        try {
            File f = new File(path);
            return f.toURI().toURL().getPath();
        } catch (MalformedURLException e) {
            return "";
        }
    }

    public void setDependentProject(IJavaProject project) {
        this.dependentProject = project;
    }

    public IJavaProject getDependentProject() {
        return this.dependentProject;
    }

    public void setOldConfig(ConnectionConfig oldOne) {
        this.oldConfig = oldOne;
    }

    /**
     * @return Returns the oldConfig.
     */
    public ConnectionConfig getOldConfig() {
        return this.oldConfig;
    }

    public void loadConfig(ConnectionConfig config) {
        if (config != null) {
            this.name.setText(config.getName());
            this.driverPath.setText(URLUtil.decode(config.getDriverPath(),
                    "UTF-8"));
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
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed() {
        try {
            ScopedPreferenceStore store = new ScopedPreferenceStore(
                    new ProjectScope(getDependentProject().getProject()),
                    Constants.ID_PLUGIN + "." + this.name.getText());
            DoltengProjectPreferences pref = DoltengCore
                    .getPreferences(getDependentProject());
            if (pref != null) {
                pref.addConnectionConfig(toConnectionConfig(store));
                pref.getRawPreferences().save();
            }
            super.okPressed();
        } catch (Exception e) {
            DoltengCore.log(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {
        setTitleImage(Images.CONNECTION_WIZARD.createImage());

        Composite rootComposite = (Composite) super.createDialogArea(parent);

        setTitle(Labels.CONNECTION_DIALOG_TITLE);

        Composite composite = createMainLayout(rootComposite);

        createPartOfDependentProject(composite);

        createPartOfName(composite);

        createPartOfDriverPath(composite);

        createPartOfDriverClass(composite);

        createLabel(composite, Labels.CONNECTION_DIALOG_CONNECTION_URL);
        this.connectionUrl = new Text(composite, SWT.BORDER);
        this.connectionUrl.setLayoutData(createGridData());
        this.validators.add(new Validator() {
            public boolean validate() {
                Text t = ConnectionDialog.this.connectionUrl;
                return StringUtil.isEmpty(t.getText());
            }

            public String getMessage() {
                return Messages.CONNECTION_URL_EMPTY;
            }
        });

        this.connectionUrl.addFocusListener(this.validationListener);

        createLabel(composite, Labels.CONNECTION_DIALOG_USER);
        this.user = new Text(composite, SWT.BORDER);
        this.user.setLayoutData(createGridData());

        createLabel(composite, Labels.CONNECTION_DIALOG_PASS);
        this.pass = new Text(composite, SWT.BORDER | SWT.PASSWORD);
        this.pass.setLayoutData(createGridData());

        createPartOfCharset(composite);

        Label separator = new Label(rootComposite, SWT.HORIZONTAL
                | SWT.SEPARATOR);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        loadConfig(this.oldConfig);

        return rootComposite;
    }

    /**
     * @param rootComposite
     * @return
     */
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

    protected void createPartOfDependentProject(Composite composite) {
        GridData data;
        createLabel(composite, Labels.CONNECTION_DIALOG_DEPENDENT_PROJECT);
        this.projectName = new Text(composite, SWT.BORDER);
        this.projectName.setText(getDependentProject().getElementName());
        data = createGridData();
        data.horizontalSpan = 1;
        data.widthHint = 250;
        this.projectName.setLayoutData(data);

        this.validators.add(new Validator() {
            public boolean validate() {
                Text t = ConnectionDialog.this.projectName;
                IJavaProject proj = ProjectUtil.getJavaProject(t.getText());
                return proj == null || proj.exists() == false;
            }

            public String getMessage() {
                return Messages.PROJECT_NOT_FOUND;
            }
        });

        this.projectName.addFocusListener(this.validationListener);

        this.projectName.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                Text t = (Text) e.widget;
                IJavaProject proj = ProjectUtil.getJavaProject(t.getText());
                if (proj != null && proj.exists()) {
                    cleanErrorMessage();
                    setDependentProject(proj);
                } else {
                    setErrorMessage(Messages.PROJECT_NOT_FOUND);
                }
            }
        });
        Button browse = new Button(composite, SWT.PUSH);
        browse.setText(Labels.CONNECTION_DIALOG_DEPENDENT_PROJECT_BROWSE);
        setButtonLayoutData(browse);
        browse.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                StandardJavaElementContentProvider provider = new StandardJavaElementContentProvider();
                ILabelProvider labelProvider = new JavaElementLabelProvider(
                        JavaElementLabelProvider.SHOW_DEFAULT);
                ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(
                        getShell(), labelProvider, provider);
                dialog.setSize(40, 18);
                dialog.setTitle(Labels.CONNECTION_DIALOG_SELECT_PROJECT);
                dialog.setMessage(Messages.SELECT_PROJECT);
                dialog.setSorter(new JavaElementSorter());
                dialog.addFilter(new ViewerFilter() {
                    public boolean select(Viewer viewer, Object parentElement,
                            Object element) {
                        for (int i = 0; i < JAVA_PROJECTS.length; i++) {
                            if (JAVA_PROJECTS[i].isInstance(element)) {
                                return true;
                            }
                        }
                        return false;
                    }
                });
                dialog.setInput(JavaCore.create(ResourcesPlugin.getWorkspace()
                        .getRoot()));
                dialog.setInitialSelection(ConnectionDialog.this
                        .getDependentProject());
                if (Window.OK == dialog.open()) {
                    Object elem = dialog.getFirstResult();
                    if (elem instanceof IJavaProject) {
                        IJavaProject proj = (IJavaProject) elem;
                        ConnectionDialog.this.dependentProject = proj;
                        ConnectionDialog.this.projectName.setText(proj
                                .getElementName());
                    }
                }

            }
        });
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
                Combo t = ConnectionDialog.this.name;
                return StringUtil.isEmpty(t.getText());
            }

            public String getMessage() {
                return Messages.NAME_IS_EMPTY;
            }
        });

        this.name.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Combo c = (Combo) e.widget;
                DoltengProjectPreferences pref = DoltengCore
                        .getPreferences(getDependentProject());
                if (pref != null) {
                    ConnectionConfig cc = pref.getConnectionConfig(c.getText());
                    loadConfig(cc);
                }
            }
        });

        this.name.addFocusListener(this.validationListener);

        DoltengProjectPreferences pref = DoltengCore
                .getPreferences(getDependentProject());
        if (pref != null) {
            ConnectionConfig[] configs = pref.getAllOfConnectionConfig();
            List names = new ArrayList();
            for (int i = 0; i < configs.length; i++) {
                ConnectionConfig config = configs[i];
                names.add(config.getName());
            }
            if (0 < names.size()) {
                this.name.setItems((String[]) names.toArray(new String[names
                        .size()]));
            }
        }
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
                Text t = ConnectionDialog.this.driverPath;
                File f = new File(t.getText());
                boolean exists = f.exists();
                ConnectionDialog.this.driverFinder.setEnabled(exists);
                return exists == false;
            }

            public String getMessage() {
                return Messages.FILE_NOT_FOUND;
            }
        });

        this.driverPath.addFocusListener(this.validationListener);

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
                ConnectionDialog.this.driverFinder.setEnabled(is);
            }
        });

        Button browse = new Button(composite, SWT.PUSH);
        browse.setText(Labels.CONNECTION_DIALOG_DRIVER_PATH_BROWSE);
        setButtonLayoutData(browse);
        browse.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(ConnectionDialog.this
                        .getShell());
                dialog.setFilterExtensions(EXTENSIONS);
                String path = dialog.open();
                if (StringUtil.isEmpty(path) == false) {
                    ConnectionDialog.this.driverPath.setText(path);
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
                Combo t = ConnectionDialog.this.driverClass;
                return t.getEnabled() == false
                        || StringUtil.isEmpty(t.getText());
            }

            public String getMessage() {
                return Messages.DRIVER_CLASS_NOT_FOUND;
            }
        });

        this.driverClass.addFocusListener(this.validationListener);

        this.driverFinder = new Button(composite, SWT.PUSH);
        this.driverFinder.setText(Labels.CONNECTION_DIALOG_DRIVER_CLASS_FIND);
        setButtonLayoutData(this.driverFinder);
        this.driverFinder.setEnabled(false);
        this.driverFinder.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                ProgressMonitorDialog dialog = new ProgressMonitorDialog(
                        ConnectionDialog.this.getShell());
                JdbcDriverFinder finder = new JdbcDriverFinder(
                        ConnectionDialog.this.driverPath.getText());
                try {
                    dialog.run(true, true, finder);
                    String[] ary = finder.getDriverClasses();
                    ConnectionDialog.this.driverClass.setItems(ary);
                    boolean is = false;
                    if (is = 0 < ary.length) {
                        ConnectionDialog.this.driverClass.select(0);
                        cleanErrorMessage();
                    } else {
                        setErrorMessage(Messages.DRIVER_CLASS_NOT_FOUND);
                    }
                    ConnectionDialog.this.driverClass.setEnabled(is);
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

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar(Composite parent) {
        Button btn = createButton(parent, 5000, Labels.CONNECTION_DIALOG_TEST,
                false);
        btn.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                testConnection();
            }
        });
        super.createButtonsForButtonBar(parent);
    }

    protected void testConnection() {
        ConnectionConfigImpl cc = toConnectionConfig(new PreferenceStore());
        Connection con = null;
        try {
            XAConnection xa = cc.getXAConnection();
            if (xa != null) {
                con = xa.getConnection();
                con.getMetaData();
                WorkbenchUtil.showMessage(Messages.CONNECTION_TEST_SUCCEED);
            } else {
                showFailedMsg();
            }
        } catch (Exception e) {
            DoltengCore.log(e);
            showFailedMsg();
        } finally {
            ConnectionUtil.close(con);
        }
    }

    private void showFailedMsg() {
        WorkbenchUtil.showMessage(Messages.CONNECTION_TEST_FAILED,
                MessageDialog.ERROR);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#setErrorMessage(java.lang.String)
     */
    public void setErrorMessage(String newErrorMessage) {
        setMessage(newErrorMessage, IMessageProvider.ERROR);
        Button ok = getButton(IDialogConstants.OK_ID);
        if (ok != null) {
            ok.setEnabled(false);
        }
    }

    public void cleanErrorMessage() {
        setMessage("");
        Button ok = getButton(IDialogConstants.OK_ID);
        if (ok != null) {
            for (final Iterator i = this.validators.iterator(); i.hasNext();) {
                Validator v = (Validator) i.next();
                if (v.validate()) {
                    setErrorMessage(v.getMessage());
                    return;
                }
            }
            ok.setEnabled(true);
        }
    }

    private List validators = new ArrayList();

    private interface Validator {
        public boolean validate();

        public String getMessage();
    }

}
