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
package org.seasar.dolteng.eclipse.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.seasar.dolteng.eclipse.Constants;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 * @author taichi
 */
public class ProjectUtil {

    private static List getCommands(IProjectDescription desc, String[] ignore)
            throws CoreException {
        ICommand[] commands = desc.getBuildSpec();
        List newCommands = new ArrayList();
        for (int i = 0; i < commands.length; i++) {
            boolean flag = true;
            for (int k = 0; k < ignore.length; k++) {
                if (commands[i].getBuilderName().equals(ignore[k])) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                newCommands.add(commands[i]);
            } else {
                flag = true;
            }
        }
        return newCommands;
    }

    private static void setCommands(IProjectDescription desc, List newCommands) {
        desc.setBuildSpec((ICommand[]) newCommands
                .toArray(new ICommand[newCommands.size()]));
    }

    public static void addBuilders(IProject project, String[] id)
            throws CoreException {
        IProjectDescription desc = project.getDescription();
        List newCommands = getCommands(desc, id);
        for (int i = 0; i < id.length; i++) {
            ICommand command = desc.newCommand();
            command.setBuilderName(id[i]);
            newCommands.add(command);
        }
        setCommands(desc, newCommands);
        project.setDescription(desc, null);
    }

    public static void removeBuilders(IProject project, String[] id)
            throws CoreException {
        IProjectDescription desc = project.getDescription();
        List newCommands = getCommands(desc, id);
        setCommands(desc, newCommands);
        project.setDescription(desc, null);
    }

    public static void addNature(IProject project, String natureID)
            throws CoreException {
        if ((project != null) && project.isAccessible()) {
            IProjectDescription desc = project.getDescription();
            String[] natureIDs = desc.getNatureIds();
            int length = natureIDs.length;
            String[] newIDs = new String[length + 1];
            for (int i = 0; i < length; i++) {
                if (natureIDs[i].equals(natureID)) {
                    return;
                }
                newIDs[i] = natureIDs[i];
            }
            newIDs[length] = natureID;
            desc.setNatureIds(newIDs);
            project.setDescription(desc, null);
        }
    }

    public static void removeNature(IProject project, String natureID)
            throws CoreException {
        if ((project != null) && project.isAccessible()) {
            IProjectDescription desc = project.getDescription();
            String[] natureIDs = desc.getNatureIds();
            int length = natureIDs.length;
            for (int i = 0; i < length; i++) {
                if (natureIDs[i].equals(natureID)) {
                    String[] newIDs = new String[length - 1];
                    System.arraycopy(natureIDs, 0, newIDs, 0, i);
                    System.arraycopy(natureIDs, i + 1, newIDs, i, length - i
                            - 1);
                    desc.setNatureIds(newIDs);
                    project.setDescription(desc, null);
                    return;
                }
            }
        }
    }

    public static IProjectNature getNature(IProject project, String natureID)
            throws CoreException {
        if ((project != null) && (project.isOpen())) {
            return project.getNature(natureID);
        }
        return null;
    }

    public static boolean hasNature(IProject project, String natureID) {
        try {
            return getNature(project, natureID) != null;
        } catch (CoreException e) {
            return false;
        }
    }

    public static String[] getNatureIds(IProject project) {
        try {
            return project.getDescription().getNatureIds();
        } catch (CoreException e) {
            return new String[0];
        }
    }

    public static IWorkspace getWorkspace() {
        return ResourcesPlugin.getWorkspace();
    }

    public static IWorkspaceRoot getWorkspaceRoot() {
        return getWorkspace().getRoot();
    }

    public static IProject[] getAllProjects() {
        return getWorkspaceRoot().getProjects();
    }

    public static IProject getProject(String projectName) {
        return getWorkspaceRoot().getProject(projectName);
    }

    public static IJavaProject getJavaProject(String projectName) {
        return JavaCore.create(getProject(projectName));
    }

    public static IJavaProject getJavaProject(IResource resource) {
        return JavaCore.create(resource.getProject());
    }

    public static IJavaProject[] getJavaProjects() throws CoreException {
        return getJavaModel().getJavaProjects();
    }

    public static IJavaModel getJavaModel() {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        return JavaCore.create(workspace.getRoot());
    }

    public static IJavaProject getJavaProject(IPath path) throws CoreException {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
                path.segment(0));
        return JavaCore.create(project);
    }

    public static IJavaProject[] getDoltengProjects() throws CoreException {
        List result = new ArrayList();
        IJavaProject[] javaps = getJavaProjects();
        for (int i = 0; i < javaps.length; i++) {
            IProject project = javaps[i].getProject();
            if (hasNature(project, Constants.ID_NATURE)) {
                result.add(javaps[i]);
            }
        }
        return (IJavaProject[]) result.toArray(new IJavaProject[result.size()]);
    }

    public static String getProjectLineDelimiter(IJavaProject javaProject) {
        IProject project = null;
        if (javaProject != null)
            project = javaProject.getProject();

        String lineDelimiter = getLineDelimiterPreference(project);
        if (lineDelimiter != null)
            return lineDelimiter;

        return System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static String getLineDelimiterPreference(IProject project) {
        IScopeContext[] scopeContext;
        if (project != null) {
            // project preference
            scopeContext = new IScopeContext[] { new ProjectScope(project) };
            String lineDelimiter = Platform.getPreferencesService().getString(
                    Platform.PI_RUNTIME, Platform.PREF_LINE_SEPARATOR, null,
                    scopeContext);
            if (lineDelimiter != null)
                return lineDelimiter;
        }
        // workspace preference
        scopeContext = new IScopeContext[] { new InstanceScope() };
        String platformDefault = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
        return Platform.getPreferencesService().getString(Platform.PI_RUNTIME,
                Platform.PREF_LINE_SEPARATOR, platformDefault, scopeContext);
    }
}