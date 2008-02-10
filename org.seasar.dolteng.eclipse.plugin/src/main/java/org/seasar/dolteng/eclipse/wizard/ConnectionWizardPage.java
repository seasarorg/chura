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
package org.seasar.dolteng.eclipse.wizard;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.sql.XAConnection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaElementSorter;
import org.eclipse.jdt.ui.StandardJavaElementContentProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Images;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.operation.JdbcDriverFinder;
import org.seasar.dolteng.eclipse.preferences.ConnectionConfig;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.preferences.impl.ConnectionConfigImpl;
import org.seasar.dolteng.eclipse.util.JdbcDiconResourceVisitor;
import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.dolteng.eclipse.util.JdbcDiconResourceVisitor.ConnectionConfigHandler;
import org.seasar.extension.jdbc.util.ConnectionUtil;
import org.seasar.framework.util.StringUtil;
import org.seasar.framework.util.URLUtil;

/**
 * 
 * @author taichi
 * 
 */
public class ConnectionWizardPage extends WizardPage implements
        ConnectionConfigHandler {

    private static final String[] EXTENSIONS = new String[] { "*.jar", "*.zip" };

    private static final String[] CHARSETS = new String[] { "Shift_JIS",
            "EUC-JP", "MS932", "UTF-8" };

    private static final Class[] JAVA_PROJECTS = new Class[] {
            IJavaModel.class, IJavaProject.class };

    private ModifyListener validationListener;

    private IJavaProject dependentProject;

    private Text projectName;

    private Combo name;

    private TableViewer driverPath;

    private Set<String> driverPathList = new HashSet<String>();

    private Combo driverClass;

    private Button driverFinder;

    private Text connectionUrl;

    private Text user;

    private Text pass;

    private Combo charset;

    public ConnectionWizardPage() {
        super("ConnectionWizardPage");
    }

    /**
     * @param pageName
     */
    public ConnectionWizardPage(String pageName) {
        super(pageName);
    }

    /**
     * @param pageName
     * @param title
     * @param titleImage
     */
    public ConnectionWizardPage(String pageName, String title,
            ImageDescriptor titleImage) {
        super(pageName, title, titleImage);
    }

    public void init(IStructuredSelection selection) {
        Object o = selection.getFirstElement();
        IProject p = ProjectUtil.getProject(o);
        dependentProject = JavaCore.create(p);

        validationListener = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                cleanErrorMessage();
            }
        };
        driverPathList = new HashSet<String>();
        setPageComplete(false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        composite.setLayout(layout);

        createPartOfDependentProject(composite);

        createPartOfName(composite);

        createPartOfDriverPath(composite);

        createPartOfDriverClass(composite);

        createPartOfConnectionUrl(composite);

        createPartOfCharset(composite);

        createPartOfAuthentication(composite);

        setControl(composite);
    }

    protected void createPartOfDependentProject(Composite composite) {
        createLabel(composite, Labels.CONNECTION_DIALOG_DEPENDENT_PROJECT);
        this.projectName = new Text(composite, SWT.BORDER);
        this.projectName.setText(dependentProject.getElementName());
        this.projectName.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        this.projectName.setEnabled(false);

        this.validators.add(new Validator() {
            public boolean validate() {
                Text t = projectName;
                IJavaProject proj = ProjectUtil.getJavaProject(t.getText());
                return proj == null || proj.exists() == false;
            }

            public String getMessage() {
                return Messages.PROJECT_NOT_FOUND;
            }
        });

        this.projectName.addModifyListener(this.validationListener);

        Button browse = new Button(composite, SWT.PUSH);
        browse.setText(Labels.CONNECTION_DIALOG_DEPENDENT_PROJECT_BROWSE);
        setButtonLayoutData(browse);
        browse.addSelectionListener(new SelectionAdapter() {
            @Override
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
                    @Override
                    public boolean select(Viewer viewer, Object parentElement,
                            Object element) {
                        for (Class javap : JAVA_PROJECTS) {
                            if (javap.isInstance(element)) {
                                return true;
                            }
                        }
                        return false;
                    }
                });
                dialog.setInput(JavaCore.create(ResourcesPlugin.getWorkspace()
                        .getRoot()));
                dialog.setInitialSelection(dependentProject);
                if (Window.OK == dialog.open()) {
                    Object elem = dialog.getFirstResult();
                    if (elem instanceof IJavaProject) {
                        IJavaProject proj = (IJavaProject) elem;
                        dependentProject = proj;
                        projectName.setText(proj.getElementName());
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
        this.name.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        this.name.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = name.getSelectionIndex();
                DoltengPreferences pref = DoltengCore
                        .getPreferences(dependentProject);
                if (pref != null && -1 < index) {
                    ConnectionConfig cc = pref.getConnectionConfig(name
                            .getItem(index));
                    loadConfig(cc);
                }
            }
        });

        this.name.addModifyListener(this.validationListener);

        DoltengPreferences pref = DoltengCore.getPreferences(dependentProject);
        if (pref != null) {
            ConnectionConfig[] configs = pref.getAllOfConnectionConfig();
            List<String> names = new ArrayList<String>();
            for (ConnectionConfig config : configs) {
                names.add(config.getName());
            }
            if (0 < names.size()) {
                this.name.setItems(names.toArray(new String[names.size()]));
            }
        }

        Button load = new Button(composite, SWT.NONE);
        load.setText(Labels.LOAD_FROM_PROJECT);
        load.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                loadFromProject();
                cleanErrorMessage();
            }
        });
    }

    protected void loadFromProject() {
        try {
            getWizard().getContainer().run(false, false,
                    new IRunnableWithProgress() {
                        public void run(IProgressMonitor monitor) {
                            monitor = ProgressMonitorUtil.care(monitor);
                            try {
                                IPackageFragmentRoot[] roots = ProjectUtil
                                        .findSrcFragmentRoots(dependentProject);
                                monitor.beginTask(Messages.JDBC_DICON_LOADING,
                                        roots.length);
                                IResourceVisitor visitor = new JdbcDiconResourceVisitor(
                                        dependentProject,
                                        ConnectionWizardPage.this);
                                for (IPackageFragmentRoot root : roots) {
                                    root.getResource().accept(visitor,
                                            IResource.DEPTH_ONE, false);
                                }
                            } catch (Exception e) {
                                DoltengCore.log(e);
                            } finally {
                                monitor.done();
                            }
                        }
                    });
        } catch (Exception e) {
            DoltengCore.log(e);
        }
    }

    public void handle(ConnectionConfig config) {
        loadConfig(config); // TODO 複数の接続設定がロード出来た時の処理が、考慮されていない。
    }

    /**
     * @param composite
     */
    protected void createPartOfDriverPath(Composite composite) {
        createLabel(composite, Labels.CONNECTION_DIALOG_DRIVER_PATH);

        Composite c = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        c.setLayout(layout);

        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        c.setLayoutData(data);

        this.driverPath = new TableViewer(c, SWT.BORDER | SWT.FULL_SELECTION
                | SWT.H_SCROLL | SWT.V_SCROLL);
        this.driverPath.setContentProvider(new ArrayContentProvider());
        this.driverPath.setInput(driverPathList);

        Table table = driverPath.getTable();
        table.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL
                | GridData.FILL_HORIZONTAL));

        this.validators.add(new Validator() {
            public boolean validate() {
                for (Iterator i = driverPathList.iterator(); i.hasNext();) {
                    File f = new File((String) i.next());
                    if (f.exists() == false) {
                        return true;
                    }
                }
                return false;
            }

            public String getMessage() {
                return Messages.FILE_NOT_FOUND;
            }
        });

        c = new Composite(c, SWT.NONE);
        GridLayout gl = new GridLayout();
        gl.numColumns = 1;
        gl.marginTop = 0;
        gl.marginWidth = 0;
        gl.marginHeight = 0;
        c.setLayout(gl);
        Button browse = new Button(c, SWT.PUSH);
        browse.setText(Labels.CONNECTION_DIALOG_DRIVER_PATH_BROWSE);
        setButtonLayoutData(browse);
        browse.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(getShell());
                dialog.setFilterExtensions(EXTENSIONS);
                String path = dialog.open();
                if (StringUtil.isEmpty(path) == false) {
                    driverPathList.add(path);
                    driverPath.refresh();
                    driverFinder.setEnabled(true);
                }
            }
        });

        Button modify = new Button(c, SWT.PUSH);
        modify.setText(Labels.MODIFY);
        setButtonLayoutData(modify);
        modify.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) driverPath
                        .getSelection();
                String current = (String) selection.getFirstElement();
                if (current != null) {
                    FileDialog dialog = new FileDialog(getShell());
                    dialog.setFilterExtensions(EXTENSIONS);
                    dialog.setFileName(current);
                    String selected = dialog.open();
                    if (StringUtil.isEmpty(selected) == false) {
                        File f = new File(selected);
                        if (f.exists() && f.canRead()) {
                            driverPathList.remove(current);
                            driverPathList.add(selected);
                            driverPath.refresh();
                        }
                    }
                }
            }
        });

        Button delete = new Button(c, SWT.PUSH);
        delete.setText(Labels.DELETE);
        setButtonLayoutData(delete);
        delete.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) driverPath
                        .getSelection();
                String current = (String) selection.getFirstElement();
                if (current != null) {
                    driverPathList.remove(current);
                    driverPath.refresh();
                }
            }
        });
    }

    /**
     * @param composite
     */
    protected void createPartOfDriverClass(Composite composite) {
        createLabel(composite, Labels.CONNECTION_DIALOG_DRIVER_CLASS);

        this.driverClass = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        this.driverClass.setLayoutData(data);
        this.driverClass.setEnabled(false);

        this.validators.add(new Validator() {
            public boolean validate() {
                Combo t = driverClass;
                int index = t.getSelectionIndex();
                return index < 0 || StringUtil.isEmpty(t.getItem(index));
            }

            public String getMessage() {
                return Messages.DRIVER_CLASS_NOT_FOUND;
            }
        });

        this.driverClass.addModifyListener(this.validationListener);

        this.driverFinder = new Button(composite, SWT.PUSH);
        this.driverFinder.setText(Labels.CONNECTION_DIALOG_DRIVER_CLASS_FIND);
        setButtonLayoutData(this.driverFinder);
        this.driverFinder.setEnabled(false);
        this.driverFinder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ProgressMonitorDialog dialog = new ProgressMonitorDialog(
                        getShell());
                JdbcDriverFinder finder = new JdbcDriverFinder(driverPathList
                        .toArray(new String[driverPathList.size()]));
                try {
                    dialog.run(true, true, finder);
                    String[] ary = finder.getDriverClasses();
                    driverClass.setItems(ary);
                    boolean is = 0 < ary.length;
                    if (is) {
                        driverClass.select(0);
                        cleanErrorMessage();
                    } else {
                        setErrorMessage(Messages.DRIVER_CLASS_NOT_FOUND);
                    }
                    driverClass.setEnabled(is);
                } catch (InterruptedException ex) {
                    setErrorMessage(ex.getMessage());
                } catch (Exception ex) {
                    DoltengCore.log(ex);
                }
            }
        });
    }

    protected void createPartOfConnectionUrl(Composite composite) {
        createLabel(composite, Labels.CONNECTION_DIALOG_CONNECTION_URL);
        this.connectionUrl = new Text(composite, SWT.BORDER);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        this.connectionUrl.setLayoutData(gd);
        this.validators.add(new Validator() {
            public boolean validate() {
                return StringUtil.isEmpty(connectionUrl.getText());
            }

            public String getMessage() {
                return Messages.CONNECTION_URL_EMPTY;
            }
        });

        this.connectionUrl.addModifyListener(this.validationListener);
    }

    protected void createPartOfAuthentication(Composite composite) {
        createLabel(composite, Labels.CONNECTION_DIALOG_USER);
        this.user = new Text(composite, SWT.BORDER);
        this.user.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Button test = new Button(composite, SWT.PUSH);
        test.setText(Labels.CONNECTION_DIALOG_TEST);
        setButtonLayoutData(test);
        GridData gd = (GridData) test.getLayoutData();
        gd.verticalSpan = 2;
        test.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                testConnection();
            }
        });

        createLabel(composite, Labels.CONNECTION_DIALOG_PASS);
        this.pass = new Text(composite, SWT.BORDER | SWT.PASSWORD);
        this.pass.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

    }

    public void loadConfig(ConnectionConfig config) {
        if (config != null) {
            this.name.setText(config.getName());
            String[] ary = config.getDriverPaths();
            for (String element : ary) {
                String path = URLUtil.decode(element, "UTF-8");
                this.driverPathList.add(path);
            }
            this.driverPath.refresh();
            this.driverClass.setEnabled(true);
            this.driverClass.add(config.getDriverClass());
            this.driverClass.select(0);
            this.connectionUrl.setText(config.getConnectionUrl());
            this.user.setText(config.getUser());
            this.pass.setText(config.getPass());
            this.charset.setText(config.getCharset());
        }
    }

    public ConnectionConfig getConfig() {
        ConnectionConfigImpl cc = new ConnectionConfigImpl(
                new PreferenceStore());
        cc.setName(ConnectionConfig.class + "@"
                + String.valueOf(System.identityHashCode(cc)));
        cc.setDriverPaths(toDriverPathArray());
        cc.setDriverClass(this.driverClass.getText());
        cc.setConnectionUrl(this.connectionUrl.getText());
        cc.setUser(this.user.getText());
        cc.setPass(this.pass.getText());
        cc.setCharset(this.charset.getText());
        return cc;
    }

    private String[] toDriverPathArray() {
        String[] ary = driverPathList
                .toArray(new String[driverPathList.size()]);
        for (int i = 0; i < ary.length; i++) {
            ary[i] = toEncodedPath(ary[i]);
        }
        return ary;
    }

    private String toEncodedPath(String path) {
        try {
            File f = new File(path);
            return f.toURI().toURL().getPath();
        } catch (MalformedURLException e) {
            return "";
        }
    }

    /**
     * @param composite
     */
    protected void createPartOfCharset(Composite composite) {
        createLabel(composite, Labels.CONNECTION_DIALOG_CHARSET);
        this.charset = new Combo(composite, SWT.BORDER);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        this.charset.setLayoutData(gd);
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

    protected void testConnection() {
        ConnectionConfig cc = getConfig();
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

    public void cleanErrorMessage() {
        setErrorMessage(null);
        for (Validator v : this.validators) {
            if (v.validate()) {
                setErrorMessage(v.getMessage());
                setPageComplete(false);
                return;
            }
        }
        setPageComplete(true);
    }

    private List<Validator> validators = new ArrayList<Validator>();

    private interface Validator {
        public boolean validate();

        public String getMessage();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            loadFromProject();
            cleanErrorMessage();
            setTitle(Labels.CONNECTION_DIALOG_TITLE);
            setImageDescriptor(Images.CONNECTION_WIZARD);
        } else {
            setImageDescriptor(null);
        }
        super.setVisible(visible);
    }

}
