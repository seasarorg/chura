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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.part.DatabaseView;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.ResourcesUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.framework.exception.IORuntimeException;
import org.seasar.framework.util.ArrayMap;
import org.seasar.framework.util.CaseInsensitiveMap;
import org.seasar.framework.util.InputStreamReaderUtil;
import org.seasar.framework.util.InputStreamUtil;
import org.seasar.framework.util.ReaderUtil;
import org.seasar.framework.util.StringUtil;
import org.seasar.framework.util.URLUtil;

/**
 * @author taichi
 * 
 */
public class ChuraProjectWizardPage extends WizardNewProjectCreationPage {

    private static final String REPL_PROJECT_NAME = "__project_name__";

    private static final String REPL_PACKAGE_PATH = "__package_path__";

    private static final String REPL_PACKAGE_NAME = "__package_name__";

    private static final String REPL_JRE_LIB = "__jre_container__";

    private Text rootPkgName;

    private Combo projectType;

    private ArrayMap selectedProjectTypes = null;

    private ArrayMap projectMap = new ArrayMap();

    private ArrayMap tigerProjects = new ArrayMap();

    private Button useDefaultJre;

    private Button selectJre;

    private Combo enableJres;

    private ArrayMap jres = new ArrayMap();

    /**
     * @param pageName
     */
    public ChuraProjectWizardPage() {
        super("ChuraProjectWizard");
        setTitle(Labels.WIZARD_CHURA_PROJECT_TITLE);
        setDescription(Messages.CHURA_PROJECT_DESCRIPTION);

        setUpProjects(projectMap, "types.txt");
        setUpProjects(tigerProjects, "tigerTypes.txt");

        String version = getDefaultJavaVersion();
        if (version.startsWith(JavaCore.VERSION_1_5)) {
            selectedProjectTypes = tigerProjects;
        } else {
            selectedProjectTypes = projectMap;
        }
    }

    /**
     * @return
     */
    private String getDefaultJavaVersion() {
        String version = JavaCore.getOption(JavaCore.COMPILER_COMPLIANCE);
        IVMInstall vm = JavaRuntime.getDefaultVMInstall();
        if (vm instanceof IVMInstall2) {
            IVMInstall2 vm2 = (IVMInstall2) vm;
            version = vm2.getJavaVersion();
        }
        return version;
    }

    private void setUpProjects(Map projects, String txt) {
        String s = getTemplateResourceTxt(txt);
        String[] ary = s.split("\r\n");
        for (int i = 0; i < ary.length; i++) {
            String key = ary[i].substring(ary[i].indexOf(',') + 1);
            String value = ary[i].substring(0, ary[i].indexOf(','));
            projects.put(key, value);
        }
    }

    public void createControl(Composite parent) {
        super.createControl(parent);
        Composite composite = (Composite) getControl();
        createRootPackage(composite);
        createJreContainer(composite);
        createProjectType(composite);
    }

    private void createRootPackage(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label label = new Label(composite, SWT.NONE);
        label.setText(Labels.WIZARD_PAGE_CHURA_ROOT_PACKAGE);
        label.setFont(parent.getFont());

        this.rootPkgName = new Text(composite, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        this.rootPkgName.setLayoutData(data);
        this.rootPkgName.setFont(parent.getFont());
        this.rootPkgName.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                boolean is = validatePage();
                if (is == false) {
                    setErrorMessage(validateRootPackageName());
                }
                setPageComplete(is);
            }
        });
    }

    private void createJreContainer(Composite parent) {
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        Group group = new Group(parent, SWT.NONE);
        group.setLayout(new GridLayout(2, false));
        group.setText(Labels.WIZARD_PAGE_CHURA_JRE_CONTAINER);
        group.setLayoutData(data);

        data = new GridData(GridData.FILL_BOTH);
        useDefaultJre = new Button(group, SWT.RADIO);
        useDefaultJre.setSelection(true);
        data.horizontalSpan = 2;
        useDefaultJre.setLayoutData(data);
        useDefaultJre.setText(Labels.bind(
                Labels.WIZARD_PAGE_CHURA_USE_DEFAULT_JRE,
                getDefaultJavaVersion()));
        useDefaultJre.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                enableJres.setEnabled(false);
                selectJre(ChuraProjectWizardPage.this, JavaCore
                        .getOption(JavaCore.COMPILER_COMPLIANCE));
            }
        });

        data = new GridData();
        selectJre = new Button(group, SWT.RADIO);
        selectJre.setLayoutData(data);
        selectJre.setText("");
        selectJre.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                enableJres.setEnabled(true);
                enableJres.select(0);
                selectJre(ChuraProjectWizardPage.this);
            }
        });

        data = new GridData();
        enableJres = new Combo(group, SWT.BORDER | SWT.READ_ONLY);
        enableJres.setLayoutData(data);

        IVMInstallType[] types = JavaRuntime.getVMInstallTypes();
        for (int i = 0; i < types.length; i++) {
            IVMInstall[] installs = types[i].getVMInstalls();
            for (int j = 0; j < installs.length; j++) {
                if (installs[j] instanceof IVMInstall2) {
                    IVMInstall2 vm2 = (IVMInstall2) installs[j];
                    StringBuffer stb = new StringBuffer();
                    stb.append(installs[j].getName());
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
        enableJres.setItems(ary);
        enableJres.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                selectJre(ChuraProjectWizardPage.this);
            }
        });
        enableJres.setEnabled(false);
    }

    private static void selectJre(ChuraProjectWizardPage page) {
        IVMInstall2 vm = (IVMInstall2) page.jres.get(page.enableJres.getText());
        selectJre(page, vm.getJavaVersion());
    }

    private static void selectJre(ChuraProjectWizardPage page, String version) {
        if (version.startsWith(JavaCore.VERSION_1_5)) {
            page.selectedProjectTypes = page.tigerProjects;
        } else {
            page.selectedProjectTypes = page.projectMap;
        }
        page.projectType.setItems(page.getProjectTypes());
        page.projectType.select(0);
    }

    private void createProjectType(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label label = new Label(composite, SWT.NONE);
        label.setText(Labels.WIZARD_PAGE_CHURA_TYPE_SELECTION);
        label.setFont(parent.getFont());

        this.projectType = new Combo(composite, SWT.BORDER | SWT.READ_ONLY);
        this.projectType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        this.projectType.setItems(getProjectTypes());
        this.projectType.select(0);
        this.projectType.pack();
    }

    private String[] getProjectTypes() {
        String[] ary = new String[selectedProjectTypes.size()];
        for (int i = 0; i < ary.length; i++) {
            ary[i] = selectedProjectTypes.getKey(i).toString();
        }
        return ary;
    }

    protected boolean validatePage() {
        return super.validatePage() ? StringUtil
                .isEmpty(validateRootPackageName()) : false;
    }

    protected String validateRootPackageName() {
        String name = getRootPackageName();
        if (StringUtil.isEmpty(name)) {
            return Messages.PACKAGE_NAME_IS_EMPTY;
        }
        IStatus val = JavaConventions.validatePackageName(name);
        if (val.getSeverity() == IStatus.ERROR
                || val.getSeverity() == IStatus.WARNING) {
            return NLS.bind(Messages.INVALID_PACKAGE_NAME, val.getMessage());
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

    protected String getProjectTypeKey() {
        return (String) selectedProjectTypes.get(this.projectType.getText());
    }

    public String getJREContainer() {
        IPath path = new Path(JavaRuntime.JRE_CONTAINER);
        if (selectJre.getSelection()) {
            IVMInstall vm = (IVMInstall) jres.get(enableJres.getText());
            path = path.append(vm.getVMInstallType().getId());
            path = path.append(vm.getName());
        }
        return path.toString();
    }

    public IRunnableWithProgress getOperation() {
        return new NewChuraProjectCreation();
    }

    private class NewChuraProjectCreation implements IRunnableWithProgress {
        private Map pathHandlers = new CaseInsensitiveMap();

        private final PathHandler DEFALUT_PATH_HANDLER = new DefaultPathHandler();

        public NewChuraProjectCreation() {
            setUpPathHandlers();
        }

        private void setUpPathHandlers() {
            pathHandlers.put(".jar", new BinaryHandler());
            PathHandler handler = new TxtHandler();
            pathHandlers.put(".txt", handler);
            pathHandlers.put(".dicon", handler);
            pathHandlers.put(".lck", handler);
            pathHandlers.put(".log", handler);
            pathHandlers.put(".properties", handler);
            pathHandlers.put(".script", handler);
            pathHandlers.put(".classpath", handler);
            pathHandlers.put(".tomcatplugin", handler);
            pathHandlers.put(".xml", handler);
            pathHandlers.put(".mf", handler);
            pathHandlers.put(".html", handler);
            pathHandlers.put(".htm", handler);
            pathHandlers.put(".xhtml", handler);
        }

        private void process(String path) {
            PathHandler handler = null;
            int index = path.lastIndexOf('.');
            if (-1 < index) {
                handler = (PathHandler) pathHandlers.get(path.substring(index));
            }
            if (handler == null) {
                handler = DEFALUT_PATH_HANDLER;
            }
            handler.process(path);
        }

        public void run(IProgressMonitor monitor)
                throws InvocationTargetException, InterruptedException {
            if (monitor == null) {
                monitor = new NullProgressMonitor();
            }
            try {
                String struct = getTemplateResourceTxt(getProjectTypeKey()
                        + "/struct.txt");
                String[] ary = struct.split("\r\n");

                monitor.beginTask(Messages.BEGINING_OF_CREATE, ary.length + 13);

                monitor.setTaskName(Messages.CREATE_BASE_PROJECT);
                ProjectUtil.createProject(getProjectHandle(),
                        getLocationPath(), null);
                monitor.worked(1);

                for (int i = 0; i < ary.length; i++) {
                    String path = ary[i].replaceAll(REPL_PACKAGE_PATH,
                            getRootPackagePath());
                    monitor.setTaskName(Messages.bind(Messages.PROCESS, path));
                    process(path);
                    monitor.worked(1);
                }

                // リソースの再読込み
                monitor.setTaskName(Messages.RELOAD_RESOURCES);
                getProjectHandle().refreshLocal(IResource.DEPTH_INFINITE, null);

                // ネイチャーの追加
                monitor.setTaskName(Messages
                        .bind(Messages.ADD_NATURE_OF, "JDT"));
                final IProject project = getProjectHandle();
                project.setDefaultCharset("UTF-8", null);
                ProjectUtil.addNature(project, JavaCore.NATURE_ID);
                monitor.worked(2);

                setUpJDTPreferences();

                monitor.setTaskName(Messages.BUILD_PROJECT);
                project.build(IncrementalProjectBuilder.FULL_BUILD, null);
                monitor.worked(4);

                monitor.setTaskName(Messages.bind(Messages.ADD_NATURE_OF,
                        "Dolteng"));
                ProjectUtil.addNature(project, Constants.ID_NATURE);
                monitor.worked(4);

                monitor.setTaskName(Messages.bind(Messages.ADD_NATURE_OF,
                        "Tomcat"));
                if (Platform.getBundle(Constants.ID_TOMCAT_PLUGIN) != null) {
                    ProjectUtil.addNature(project, Constants.ID_TOMCAT_NATURE);
                }

                if (Platform.getBundle(Constants.ID_DIIGU_PLUGIN) != null) {
                    ProjectUtil.addNature(project, Constants.ID_DIIGU_NATURE);
                }

                setUpDoltengPreferences();

                monitor.setTaskName(Messages.RELOAD_DATABASE_VIEW);
                IRunnableWithProgress op = new IRunnableWithProgress() {
                    public void run(IProgressMonitor monitor)
                            throws InvocationTargetException,
                            InterruptedException {
                        DatabaseView.reloadView();
                    }
                };
                PlatformUI.getWorkbench().getProgressService().runInUI(
                        WorkbenchUtil.getWorkbenchWindow(), op,
                        ResourcesPlugin.getWorkspace().getRoot());
                monitor.worked(2);
            } catch (Exception e) {
                DoltengCore.log(e);
                throw new InterruptedException();
            } finally {
                monitor.done();
            }
        }

        private void setUpJDTPreferences() {
            IJavaProject project = JavaCore.create(getProjectHandle());
            Map options = project.getOptions(false);
            Properties props = getTemplateProperties("jdt.pref");
            for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
                String key = e.nextElement().toString();
                options.put(key, props.getProperty(key));
            }
            project.setOptions(options);
        }

        /**
         * @throws IOException
         */
        private void setUpDoltengPreferences() throws IOException {
            DoltengProjectPreferences pref = DoltengCore
                    .getPreferences(getProjectHandle());
            IPersistentPreferenceStore store = pref.getRawPreferences();
            Properties props = getTemplateProperties("dolteng.pref");
            for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
                String key = e.nextElement().toString();
                store.setValue(key, props.getProperty(key));
            }
            store.save();
        }
    }

    private Properties getTemplateProperties(String name) {
        Properties props = new Properties();
        URL url = getTemplateResourceURL(getProjectTypeKey() + "/" + name);
        InputStream in = null;
        if (url != null) {
            try {
                in = URLUtil.openStream(url);
                props.load(in);
            } catch (IOException e) {
                throw new IORuntimeException(e);
            } finally {
                InputStreamUtil.close(in);
            }
        }

        return props;
    }

    private interface PathHandler {
        void process(String path);
    }

    private class BinaryHandler implements PathHandler {
        public void process(String path) {
            String jar = path.substring(path.lastIndexOf('/') + 1);
            String dir = "jars/";
            if (jar.endsWith("sources.jar")) {
                dir = dir + "sources/";
            }
            URL url = getTemplateResourceURL(dir + jar);
            if (url == null) {
                DoltengCore.log("missing .." + path);
                return;
            }
            InputStream src = null;
            try {
                src = URLUtil.openStream(url);
                IFile f = getProjectHandle().getFile(path);
                f.create(src, true, null);
            } catch (Exception e) {
                DoltengCore.log(e);
            } finally {
                InputStreamUtil.close(src);
            }
        }
    }

    private class DefaultPathHandler implements PathHandler {
        public void process(String path) {
            ResourcesUtil.createDir(getProjectHandle(), path);
        }
    }

    private static final String[] DIRS = { "licenses/", "resources/",
            "WEB-INF/", "META-INF/", "view" };

    private class TxtHandler implements PathHandler {
        public void process(String path) {
            int index = -1;
            String srcPath = path;
            for (int i = 0; i < DIRS.length; i++) {
                index = path.indexOf(DIRS[i]);
                if (-1 < index) {
                    srcPath = path.substring(index);
                    break;
                }
            }

            URL url = getTemplateResourceURL(getProjectTypeKey() + "/"
                    + srcPath);
            if (url == null) {
                url = getTemplateResourceURL(srcPath);
            }
            if (url == null) {
                DoltengCore.log("missing .." + path);
                return;
            }
            String txt = getTemplateResourceTxt(url);
            txt = txt.replaceAll(REPL_JRE_LIB, getJREContainer());
            txt = txt.replaceAll(REPL_PROJECT_NAME, getProjectName());
            txt = txt.replaceAll(REPL_PACKAGE_NAME, getRootPackageName());
            txt = txt.replaceAll(REPL_PACKAGE_PATH, getRootPackagePath());

            String dest = path.replaceAll(REPL_PROJECT_NAME, getProjectName());
            IFile f = getProjectHandle().getFile(dest);
            try {
                createNewFile(f, txt, null);
            } catch (Exception e) {
                DoltengCore.log(e);
            }
        }
    }

    private void createNewFile(IFile handle, String txt,
            IProgressMonitor monitor) throws Exception {
        byte[] bytes = txt.getBytes("UTF-8");
        InputStream src = null;
        try {
            if (handle.exists()) {
                handle.delete(true, monitor);
            }
            src = new ByteArrayInputStream(bytes);
            handle.create(src, IResource.FORCE, monitor);
        } finally {
            InputStreamUtil.close(src);
        }
    }

    public static String getTemplateResourceTxt(String path) {
        URL url = getTemplateResourceURL(path);
        return getTemplateResourceTxt(url);
    }

    private static String getTemplateResourceTxt(URL url) {
        Reader reader = InputStreamReaderUtil.create(URLUtil.openStream(url),
                "UTF-8");
        return ReaderUtil.readText(reader);
    }

    private static URL getTemplateResourceURL(String path) {
        Plugin plugin = DoltengCore.getDefault();
        return plugin.getBundle().getEntry("template/" + path);
    }

}
