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
package org.seasar.dolteng.projects;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.loader.ResourceLoader;
import org.seasar.dolteng.eclipse.loader.impl.MavenResourceLoader;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.projects.handler.ResourceHandler;
import org.seasar.dolteng.projects.model.Entry;
import org.seasar.framework.util.ArrayMap;

/**
 * @author taichi
 */
public class ProjectBuilder {

    private transient IProject project;

    private transient IPath location;

    private ArrayMap/*<String, ResourceHandler>*/ handlers = new ArrayMap/*<String, ResourceHandler>*/();

    private LinkedList<String> resourceRoots = new LinkedList<String>();

    /** コンテキスト情報。Serializableを明示的に示す為にHashMap型 */
    private HashMap<String, String> configContext;

    /** 仕事量 */
    private int works = 1;

    /**
     * <ul>
     * <li>projectName</li>
     * <li>packageName</li>
     * <li>packagePath</li>
     * <li>jreContainer</li>
     * 出力パス周りは、Maven構成と標準構成をコンボで選ぶと、それぞれ勝手に入る様にする。 <br>
     * それで、気に入らなければ、編集出来る様にする。
     * <li>libPath</li>
     * <li>libSrcPath</li>
     * <li>testlibPath</li>
     * <li>testlibSrcPath</li>
     * <li>mainJavaPath</li>
     * <li>mainResourcePath</li>
     * <li>mainOutPath</li>
     * <li>webAppRoot</li>
     * <li>testJavaPath</li>
     * <li>testResourcePath</li>
     * <li>testOutPath</li>
     * </ul>
     * 
     * @param project
     * @param location
     * @param configContext
     */
    public ProjectBuilder(IProject project, IPath location,
            Map<String, String> configContext) {
        super();
        this.project = project;
        this.location = location;
        this.configContext = new HashMap<String, String>(configContext);
    }

    public void addHandler(ResourceHandler handler) {
        ResourceHandler master = (ResourceHandler) handlers.get(handler
                .getType());
        if (master == null) {
            works += handler.getNumberOfFiles();
            handlers.put(handler.getType(), handler);
        } else {
            works -= master.getNumberOfFiles();
            master.merge(handler);
            works += master.getNumberOfFiles();
        }
    }

    public void addRoot(String path) {
        resourceRoots.add(0, path);
    }

    public void addProperty(String key, String value) {
        configContext.put(key, value);
    }

    public IProject getProjectHandle() {
        return project;
    }

    public Map<String, String> getConfigContext() {
        return configContext;
    }

    public URL findResource(Entry entry) {
        URL result = null;
        ResourceLoader loader = entry.getLoader();
        String maven = entry.attribute.get("maven");
        String mavenResource = entry.attribute.get("mavenResource");
        if (loader instanceof MavenResourceLoader && maven != null
                && mavenResource != null) {
            result = loader.getResouce(maven + "," + mavenResource);
        }
        if (result == null) {
            result = findResource(loader, entry.getPath());
        }
        return result;
    }

    public URL findResource(ResourceLoader loader, String path) {
        URL result = null;
        path = new Path(path).lastSegment();
        for (String root : resourceRoots) {
            result = loader.getResouce(new Path(root).append(path).toString());
            if (result != null) {
                break;
            }
        }
        return result;
    }

    public void build(IProgressMonitor monitor) {
        try {
            monitor = ProgressMonitorUtil.care(monitor);
            monitor.beginTask(Messages.BEGINING_OF_CREATE, works);
            monitor.setTaskName(Messages.CREATE_BASE_PROJECT);

            ProjectUtil.createProject(project, location, null);
            ProgressMonitorUtil.isCanceled(monitor, 1);
            for (int i = 0; i < handlers.size(); i++) {
                ResourceHandler handler = (ResourceHandler) handlers.get(i);
                handler.handle(this, monitor);
                project.refreshLocal(IResource.DEPTH_INFINITE, null);
            }
        } catch (CoreException e) {
            DoltengCore.log(e);
        }
    }

    public void setProject(IProject project) {
        this.project = project;
    }

    public void setLocation(IPath location) {
        this.location = location;
    }
}
