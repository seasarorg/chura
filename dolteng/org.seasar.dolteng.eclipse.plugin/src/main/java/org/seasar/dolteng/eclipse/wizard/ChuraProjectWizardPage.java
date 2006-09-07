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
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
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
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.framework.util.ArrayMap;
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

    private Text rootPkgName;

    private Combo projectType;

    private ArrayMap projectMap = new ArrayMap();

    /**
     * @param pageName
     */
    public ChuraProjectWizardPage() {
        super("ChuraProjectWizard");
        setTitle(Labels.WIZARD_CHURA_PROJECT_TITLE);
        setDescription(Messages.CHURA_PROJECT_DESCRIPTION);

        String s = getTemplateResourceTxt("types.txt");
        String[] ary = s.split("\r\n");
        for (int i = 0; i < ary.length; i++) {
            String key = ary[i].substring(ary[i].indexOf(',') + 1);
            String value = ary[i].substring(0, ary[i].indexOf(','));
            projectMap.put(key, value);
        }

    }

    public void createControl(Composite parent) {
        super.createControl(parent);
        Composite composite = (Composite) getControl();
        createRootPackage(composite);
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
                setPageComplete(validatePage());
            }
        });
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
        this.projectType.select(1);
        this.projectType.pack();
    }

    private String[] getProjectTypes() {
        String[] ary = new String[projectMap.size()];
        for (int i = 0; i < ary.length; i++) {
            ary[i] = projectMap.getKey(i).toString();
        }
        return ary;
    }

    protected boolean validatePage() {
        String name = getRootPackageName();
        if (StringUtil.isEmpty(name)) {
            setErrorMessage(Messages.PACKAGE_NAME_IS_EMPTY);
            return false;
        }
        IStatus val = JavaConventions.validatePackageName(name);
        if (val.getSeverity() == IStatus.ERROR
                || val.getSeverity() == IStatus.WARNING) {
            String msg = NLS.bind(Messages.INVALID_PACKAGE_NAME, val
                    .getMessage());
            setErrorMessage(msg);
            return false;
        }

        return super.validatePage();
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
        return (String) projectMap.get(this.projectType.getText());
    }

    public IRunnableWithProgress getOperation() {
        return new NewChuraProjectCreation();
    }

    private class NewChuraProjectCreation implements IRunnableWithProgress {
        private Map pathHandlers = new HashMap();

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
        }

        private void process(String path, IProgressMonitor monitor) {
            monitor.beginTask(path, 3);
            try {
                PathHandler handler = null;
                int index = path.lastIndexOf('.');
                if (-1 < index) {
                    handler = (PathHandler) pathHandlers.get(path
                            .substring(index));
                }
                if (handler == null) {
                    handler = DEFALUT_PATH_HANDLER;
                }
                monitor.worked(1);
                handler.process(path, monitor);
            } finally {
                monitor.done();
            }
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

                monitor.beginTask("Create Chura Project ....", ary.length + 9);
                monitor.setTaskName("Process .....");

                ProjectUtil.createProject(getProjectHandle(),
                        getLocationPath(), monitor);
                monitor.worked(1);

                for (int i = 0; i < ary.length; i++) {
                    process(ary[i], new SubProgressMonitor(monitor, 1));
                }

                // リソースの再読込み
                getProjectHandle().refreshLocal(IResource.DEPTH_INFINITE,
                        new SubProgressMonitor(monitor, 1));

                // ネイチャーの追加
                final IProject project = getProjectHandle();
                project.setDefaultCharset("UTF-8", new SubProgressMonitor(
                        monitor, 1));
                ProjectUtil.addNature(project, JavaCore.NATURE_ID);
                monitor.worked(1);

                project.build(IncrementalProjectBuilder.FULL_BUILD,
                        new SubProgressMonitor(monitor, 3));

                ProjectUtil.addNature(project, Constants.ID_NATURE);
                monitor.worked(1);
                if (Platform.getBundle(Constants.ID_TOMCAT_PLUGIN) != null) {
                    ProjectUtil.addNature(project, Constants.ID_TOMCAT_NATURE);
                }

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
                monitor.worked(1);
            } catch (Exception e) {
                DoltengCore.log(e);
                throw new InterruptedException();
            } finally {
                monitor.done();
            }
        }

    }

    private interface PathHandler {
        void process(String path, IProgressMonitor monitor);
    }

    private class BinaryHandler implements PathHandler {
        public void process(String path, IProgressMonitor monitor) {
            String jar = path.substring(path.lastIndexOf('/') + 1);
            String dir = "jars/";
            if (jar.endsWith("sources.jar")) {
                dir = dir + "sources/";
            }
            URL url = getTemplateResourceURL(dir + jar);
            if (url == null) {
                DoltengCore.log("missing .." + url);
                return;
            }
            monitor.worked(1);
            InputStream src = null;
            try {
                src = URLUtil.openStream(url);
                IFile f = getProjectHandle().getFile(path);
                f.create(src, true, monitor);
            } catch (Exception e) {
                DoltengCore.log(e);
            } finally {
                InputStreamUtil.close(src);
            }
        }
    }

    private class DefaultPathHandler implements PathHandler {
        public void process(String path, IProgressMonitor monitor) {
            try {
                path = path.replaceAll(REPL_PACKAGE_PATH, getRootPackagePath());
                IPath p = new Path(path);
                if (getProjectHandle().exists(p) == false) {
                    String[] ary = p.segments();
                    StringBuffer stb = new StringBuffer();
                    for (int i = 0; i < ary.length; i++) {
                        String s = stb.append(ary[i]).toString();
                        if (getProjectHandle().exists(new Path(s)) == false) {
                            IFolder f = getProjectHandle().getFolder(s);
                            f.create(true, true, monitor);
                        }
                        stb.append('/');
                    }
                }
            } catch (CoreException e) {
                DoltengCore.log(e);
            }
        }
    }

    private class TxtHandler implements PathHandler {
        public void process(String path, IProgressMonitor monitor) {
            int index = path.indexOf("resources/");
            String srcPath = path;
            if (index < 0) {
                index = path.indexOf("WEB-INF/");
            }
            if (-1 < index) {
                srcPath = path.substring(index);
            }

            URL url = getTemplateResourceURL(getProjectTypeKey() + "/"
                    + srcPath);
            if (url == null) {
                url = getTemplateResourceURL(srcPath);
            }
            if (url == null) {
                DoltengCore.log("missing .." + url);
                return;
            }
            String txt = getTemplateResourceTxt(url);
            txt = txt.replaceAll(REPL_PROJECT_NAME, getProjectName());
            txt = txt.replaceAll(REPL_PACKAGE_NAME, getRootPackageName());
            txt = txt.replaceAll(REPL_PACKAGE_PATH, getRootPackagePath());

            String dest = path.replaceAll(REPL_PROJECT_NAME, getProjectName());
            IFile f = getProjectHandle().getFile(dest);
            try {
                createNewFile(f, txt, monitor);
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
