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
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.framework.util.InputStreamUtil;
import org.seasar.framework.util.TextUtil;

/**
 * @author taichi
 * 
 */
public class ChuraProjectWizard extends Wizard implements INewWizard {

    private WizardNewProjectCreationPage creationPage;

    public ChuraProjectWizard() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    public void addPages() {
        super.addPages();
        this.creationPage = new WizardNewProjectCreationPage(
                "ChuraProjectWizard");
        creationPage.setTitle(Labels.WIZARD_CHURA_PROJECT_TITLE);
        creationPage.setDescription(Messages.CHURA_PROJECT_DESCRIPTION);
        addPage(this.creationPage);
    }

    protected IProject getProjectHandle() {
        return this.creationPage.getProjectHandle();
    }

    protected IPath getLocationPath() {
        return this.creationPage.getLocationPath();
    }

    public IJavaProject getNewJavaProject() {
        return JavaCore.create(getProjectHandle());
    }

    public String getRootPackageName() {
        return "teeda.example.hoge"; // FIXME
    }

    private class NewChuraProjectCreation implements IRunnableWithProgress {

        public void run(IProgressMonitor monitor)
                throws InvocationTargetException, InterruptedException {
            if (monitor == null) {
                monitor = new NullProgressMonitor();
            }
            try {
                ProjectUtil.createProject(getProjectHandle(),
                        getLocationPath(), monitor);
                ProjectUtil.addNature(getProjectHandle(), JavaCore.NATURE_ID);
                ProjectUtil.addNature(getProjectHandle(), Constants.ID_NATURE);

                // ディレクトリの作成 & ファイルのコピー
                List path = processFiles("basic", monitor);
                path.addAll(processFiles("teeda", monitor));
                path.addAll(processFiles("kuina", monitor));
                // .classpathの生成
                createClasspath(path, monitor);
                // .tomcatpluginの生成
                createTomcatConfig();
                // リソースの再読込み
                getProjectHandle().refreshLocal(IResource.DEPTH_INFINITE,
                        monitor);
                getProjectHandle().build(IncrementalProjectBuilder.FULL_BUILD,
                        monitor);
            } catch (Exception e) {
                DoltengCore.log(e);
            } finally {
                monitor.done();
            }
        }
    }

    protected List processFiles(String templateName, IProgressMonitor monitor) {
        List path = new ArrayList();
        Plugin plugin = DoltengCore.getDefault();
        String replaceQuery = "template-" + templateName;
        for (Enumeration e = plugin.getBundle().findEntries(
                "template/" + templateName, null, true); e != null
                && e.hasMoreElements();) {
            URL u = (URL) e.nextElement();
            InputStream src = null;
            try {
                String name = u.getFile().replaceAll(
                        "/template/" + templateName + "/", "");
                if (0 < name.indexOf(".svn")) {
                    continue;
                }
                if (0 < name.indexOf('.')) {
                    src = u.openStream();
                    name = name.replaceAll(replaceQuery, getProjectHandle()
                            .getName());
                    IFile f = getProjectHandle().getFile(name);
                    f.create(src, IResource.FORCE, monitor);
                    if (name.endsWith(".dicon")) {
                        String txt = TextUtil.readUTF8(f.getRawLocation()
                                .toFile());
                        txt = txt.replaceAll(replaceQuery, getProjectHandle()
                                .getName());
                        txt = txt.replaceAll("root_package_name",
                                getRootPackageName());
                        createNewFile(f, txt, monitor);
                    } else if (0 < name.indexOf(".jar")
                            && 0 < name.indexOf("-sources") == false
                            && path.contains(name) == false) {
                        path.add(name);
                    }
                } else {
                    name = name.replaceAll(replaceQuery, getRootPackageName());
                    IFolder f = getProjectHandle().getFolder(name);
                    if (f.exists() == false) {
                        f.create(true, true, monitor);
                    }
                }

            } catch (Exception ex) {
                DoltengCore.log(ex);
            } finally {
                InputStreamUtil.close(src);
            }
        }
        return path;
    }

    private static final String CLASSPATH_ENTRY = "\t<classpathentry";

    // FIXME : プロジェクトのタイプを選べる時には…。
    private static final String WEB_CLASSES = " output=\"src/main/webapp/WEB-INF/classes\"";

    protected void createClasspath(List path, IProgressMonitor monitor)
            throws Exception {
        String lineDelim = ProjectUtil
                .getLineDelimiterPreference(getProjectHandle());
        StringBuffer xml = new StringBuffer();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append(lineDelim);
        xml.append("<classpath>");
        xml.append(lineDelim);
        xml.append(CLASSPATH_ENTRY).append(WEB_CLASSES).append(
                " kind=\"src\" path=\"src/main/java\"/>");
        xml.append(lineDelim);
        xml.append(CLASSPATH_ENTRY).append(WEB_CLASSES).append(
                " kind=\"src\" path=\"src/main/resources\"/>");
        xml.append(lineDelim);
        xml.append(CLASSPATH_ENTRY).append(
                " kind=\"src\" path=\"src/test/java\"/>");
        xml.append(lineDelim);
        xml.append(CLASSPATH_ENTRY).append(
                " kind=\"src\" path=\"src/test/resources\"/>");
        xml.append(lineDelim);
        for (Iterator i = path.iterator(); i.hasNext();) {
            xml.append(CLASSPATH_ENTRY).append(" kind=\"lib\" path=\"").append(
                    i.next()).append("\"/>");
            xml.append(lineDelim);
        }
        xml
                .append(CLASSPATH_ENTRY)
                .append(
                        " kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER\"/>");
        xml.append(lineDelim);
        xml.append(CLASSPATH_ENTRY).append(
                " kind=\"output\" path=\"target/test-classes\"/>");
        xml.append(lineDelim);
        xml.append("</classpath>");

        IFile f = getProjectHandle().getFile(".classpath");
        createNewFile(f, xml.toString(), monitor);

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

    protected void createTomcatConfig() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    public boolean performFinish() {
        try {
            getContainer().run(false, false, new NewChuraProjectCreation());
            return true;
        } catch (InvocationTargetException e) {
            DoltengCore.log(e.getTargetException());
        } catch (Exception e) {
            DoltengCore.log(e);
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
     *      org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {

    }

}
