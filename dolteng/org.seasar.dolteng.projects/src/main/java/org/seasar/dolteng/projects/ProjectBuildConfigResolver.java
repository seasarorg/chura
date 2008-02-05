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

import static org.seasar.dolteng.projects.Constants.EXTENSION_POINT_NEW_PROJECT;
import static org.seasar.dolteng.projects.Constants.EXTENSION_POINT_RESOURCE_HANDLER;
import static org.seasar.dolteng.projects.Constants.EXTENSION_POINT_RESOURCE_LOADER;
import static org.seasar.dolteng.projects.Constants.ID_PLUGIN;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaCore;
import org.seasar.dolteng.eclipse.loader.ResourceLoader;
import org.seasar.dolteng.eclipse.util.ScriptingUtil;
import org.seasar.dolteng.projects.handler.ResourceHandler;
import org.seasar.dolteng.projects.handler.impl.DefaultHandler;
import org.seasar.dolteng.projects.model.Entry;
import org.seasar.dolteng.projects.model.ProjectConfig;
import org.seasar.dolteng.projects.model.ProjectDisplay;
import org.seasar.eclipse.common.util.ExtensionAcceptor;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class ProjectBuildConfigResolver {

    private Map<String, IConfigurationElement> handlerFactories = new HashMap<String, IConfigurationElement>();

    private Map<String, ProjectConfig> mantis = new HashMap<String, ProjectConfig>();

    private Map<String, ProjectConfig> tiger = new HashMap<String, ProjectConfig>();

    private Map<String, ProjectConfig> all = new HashMap<String, ProjectConfig>();

    public ProjectBuildConfigResolver() {
    }

    public void initialize() {
        handlerFactories = new HashMap<String, IConfigurationElement>();

        ExtensionAcceptor.accept(ID_PLUGIN, EXTENSION_POINT_RESOURCE_HANDLER,
                new ExtensionAcceptor.ExtensionVisitor() {
                    public void visit(IConfigurationElement e) {
                        if (EXTENSION_POINT_RESOURCE_HANDLER
                                .equals(e.getName())) {
                            handlerFactories.put(e.getAttribute("name"), e);
                        }
                    }
                });

        ExtensionAcceptor.accept(ID_PLUGIN, EXTENSION_POINT_NEW_PROJECT,
                new ExtensionAcceptor.ExtensionVisitor() {
                    public void visit(IConfigurationElement e) {
                        if ("project".equals(e.getName())) {
                            ProjectConfig pc = new ProjectConfig(e);
                            if (pc.isVisibleProjectType()) {
                                if (JavaCore.VERSION_1_4.equals(pc.getJre())) {
                                    mantis.put(pc.getId(), pc);
                                } else {
                                    tiger.put(pc.getId(), pc);
                                }
                            }
                            all.put(pc.getId(), pc);
                        }
                    }
                });
    }

    public ProjectDisplay[] getProjects(String jre) {
        List<ProjectConfig> result = null;
        if (JavaCore.VERSION_1_4.equals(jre)) {
            result = new ArrayList<ProjectConfig>(this.mantis.values());
        } else {
            result = new ArrayList<ProjectConfig>(this.tiger.values());
        }
        return result.toArray(new ProjectDisplay[result.size()]);
    }

    public ProjectBuilder resolve(String[] projectTypeIds, IProject project,
            IPath location, Map<String, String> configContext)
            throws CoreException {

        // FIXME 複合プロジェクトメカニズムの布石…
        ProjectBuilder builder = null;
        for (String projectTypeId : projectTypeIds) {
            ProjectConfig pc = all.get(projectTypeId);
            IConfigurationElement ce = pc.getConfigurationElement();
            ResourceLoader loader = (ResourceLoader) ce
                    .createExecutableExtension(EXTENSION_POINT_RESOURCE_LOADER);
            builder = new ProjectBuilder(project, location, configContext,
                    loader);

            resolve(projectTypeId, builder, new HashSet<String>(),
                    new HashSet<String>());
        }
        return builder;
    }

    protected void resolve(String projectTypeId, ProjectBuilder builder,
            Set<String> proceedIds, Set<String> propertyNames)
            throws CoreException {
        if (proceedIds.contains(projectTypeId)) {
            return;
        }
        proceedIds.add(projectTypeId);

        ProjectConfig pc = all.get(projectTypeId);
        IConfigurationElement current = pc.getConfigurationElement();

        IConfigurationElement[] propertyElements = current
                .getChildren("property");
        for (IConfigurationElement handNode : propertyElements) {
            String name = handNode.getAttribute("name");
            if (propertyNames.contains(name) == false) {
                builder.addProperty(name, handNode.getAttribute("value"));
                propertyNames.add(name);
            }
        }

        String extendsAttr = current.getAttribute("extends");
        if (StringUtil.isEmpty(extendsAttr) == false) {
            for (String parentId : extendsAttr.split("[ ]*,[ ]*")) {
                resolve(parentId, builder, proceedIds, propertyNames);
            }
        }

        String rootAttr = current.getAttribute("root");
        if (StringUtil.isEmpty(rootAttr) == false) {
            for (String root : rootAttr.split("[ ]*,[ ]*")) {
                builder.addRoot(root);
            }
        }

        for (IConfigurationElement handNode : current.getChildren("handler")) {
            ResourceHandler handler = createHandler(handNode);
            addEntries(handNode, builder, handler);
            builder.addHandler(handler);
        }

    }

    private ResourceHandler createHandler(IConfigurationElement handNode) {
        ResourceHandler handler = null;
        String type = handNode.getAttribute("type");
        IConfigurationElement factory = handlerFactories.get(type);
        try {
            handler = (ResourceHandler) factory
                    .createExecutableExtension("class");
        } catch (CoreException e) {
            Activator.log(e);
        } catch (NullPointerException e) {
            Activator.log(new Exception("[ERROR] resource handler (" + type
                    + ") is not defined.", e));
        }
        if (handler == null) {
            handler = new DefaultHandler();
        }
        return handler;
    }

    private void addEntries(IConfigurationElement handNode,
            ProjectBuilder builder, ResourceHandler handler) {
        for (IConfigurationElement e : handNode.getChildren("entry")) {
            Entry entry = new Entry();
            for (String key : e.getAttributeNames()) {
                String value = e.getAttribute(key);
                if (StringUtil.isEmpty(value) == false) {
                    value = ScriptingUtil.resolveString(value, builder
                            .getConfigContext());
                    entry.attribute.put(key, value);
                }
            }
            handler.add(entry);
        }
    }
}
