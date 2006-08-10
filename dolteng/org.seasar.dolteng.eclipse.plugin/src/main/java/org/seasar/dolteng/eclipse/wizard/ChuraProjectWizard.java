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
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.part.DatabaseView;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.framework.util.InputStreamUtil;
import org.seasar.framework.util.TextUtil;

/**
 * @author taichi
 * 
 */
public class ChuraProjectWizard extends Wizard implements INewWizard {

    private ChuraProjectWizardPage creationPage;

    public ChuraProjectWizard() {
        super();
        setNeedsProgressMonitor(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    public void addPages() {
        super.addPages();
        this.creationPage = new ChuraProjectWizardPage("ChuraProjectWizard");
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
        return this.creationPage.getRootPackageName();
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
                // ディレクトリの作成 & ファイルのコピー
                Collection path = processFiles("basic", monitor);
                path.addAll(processFiles("chura", monitor));
                // .classpathの生成
                createClasspath(path, monitor);
                // .tomcatpluginの生成
                createTomcatConfig(monitor);

                // リソースの再読込み
                getProjectHandle().refreshLocal(IResource.DEPTH_INFINITE,
                        monitor);

                // ネイチャーの追加
                final IProject project = getProjectHandle();
                ProjectUtil.addNature(project, JavaCore.NATURE_ID);

                project.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
                ProjectUtil.addNature(project, Constants.ID_NATURE);
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

            } catch (Exception e) {
                DoltengCore.log(e);
                throw new InterruptedException();
            } finally {
                monitor.done();
            }
        }
    }

    protected Collection processFiles(String templateName,
            IProgressMonitor monitor) {
        Set path = new HashSet();
        Plugin plugin = DoltengCore.getDefault();
        String replaceQuery = "template-" + templateName;
        String replaceQueryPath = replaceQuery + "-path";
        String rootpkgPath = getRootPackageName().replace('.', '/');
        for (Enumeration e = plugin.getBundle().findEntries(
                "template/" + templateName, null, true); e != null
                && e.hasMoreElements();) {
            URL u = (URL) e.nextElement();
            InputStream src = null;
            try {
                String name = u.getFile().replaceAll(
                        "/template/" + templateName + "/", "");
                name = name.replaceAll(replaceQueryPath, rootpkgPath);
                if (0 <= name.indexOf(".svn")) {
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
                            && 0 < name.indexOf("-sources") == false) {
                        path.add(name);
                    }
                } else {
                    IPath p = new Path(name);
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

    protected void createClasspath(Collection path, IProgressMonitor monitor)
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
                " kind=\"src\" path=\"src/main/webapp/view\"/>");
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

    protected void createTomcatConfig(IProgressMonitor monitor)
            throws Exception {
        String lineDelim = ProjectUtil
                .getLineDelimiterPreference(getProjectHandle());
        StringBuffer xml = new StringBuffer();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        xml.append(lineDelim);
        xml.append("<tomcatProjectProperties>");
        xml.append(lineDelim);
        xml.append("<rootDir>/src/main/webapp/</rootDir>");
        xml.append(lineDelim);
        xml.append("<exportSource>false</exportSource>");
        xml.append(lineDelim);
        xml.append("<reloadable>false</reloadable>");
        xml.append(lineDelim);
        xml.append("<redirectLogger>true</redirectLogger>");
        xml.append(lineDelim);
        xml.append("<updateXml>true</updateXml>");
        xml.append(lineDelim);
        xml.append("<warLocation></warLocation>");
        xml.append(lineDelim);
        xml.append("<extraInfo></extraInfo>");
        xml.append(lineDelim);
        xml.append("<webPath>/");
        xml.append(getProjectHandle().getName());
        xml.append("</webPath>");
        xml.append(lineDelim);
        xml.append("</tomcatProjectProperties>");
        IFile f = getProjectHandle().getFile(".tomcatplugin");
        createNewFile(f, xml.toString(), monitor);
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
