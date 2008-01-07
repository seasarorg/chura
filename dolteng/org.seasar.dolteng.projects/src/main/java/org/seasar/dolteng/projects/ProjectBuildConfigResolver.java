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

import static org.seasar.dolteng.projects.Constants.*;

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

	private Map<String, IConfigurationElement> handlerfactories = new HashMap<String, IConfigurationElement>();

	private Map<String, ProjectConfig> mantis = new HashMap<String, ProjectConfig>();

	private Map<String, ProjectConfig> tiger = new HashMap<String, ProjectConfig>();

	private Map<String, ProjectConfig> all = new HashMap<String, ProjectConfig>();

	public ProjectBuildConfigResolver() {
	}

	public void initialize() {
		this.handlerfactories = new HashMap<String, IConfigurationElement>();

		ExtensionAcceptor.accept(ID_PLUGIN, EXTENSION_POINT_RESOURCE_HANDLER,
				new ExtensionAcceptor.ExtensionVisitor() {
					public void visit(IConfigurationElement e) {
						if (EXTENSION_POINT_RESOURCE_HANDLER
								.equals(e.getName())) {
							handlerfactories.put(e.getAttribute("name"), e);
						}
					}
				});

		ExtensionAcceptor.accept(Constants.ID_PLUGIN,
				Constants.EXTENSION_POINT_NEW_PROJECT,
				new ExtensionAcceptor.ExtensionVisitor() {
					public void visit(IConfigurationElement e) {
						if ("project".equals(e.getName())) {
							ProjectConfig pc = new ProjectConfig(e);
							String display = e.getAttribute("displayOrder");
							if (StringUtil.isEmpty(display) == false) {
								String jre = e.getAttribute("jre");
								if (JavaCore.VERSION_1_4.equals(jre)) {
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
		return (ProjectDisplay[]) result.toArray(new ProjectDisplay[result
				.size()]);
	}

	public ProjectBuilder resolve(String id, IProject project, IPath location,
			Map<String, String> configContext) throws CoreException {
		ProjectConfig pc = this.all.get(id);
		IConfigurationElement ce = pc.getConfigurationElement();
		ResourceLoader loader = (ResourceLoader) ce
				.createExecutableExtension(Constants.EXTENSION_POINT_RESOURCE_LOADER);
		ProjectBuilder builder = new ProjectBuilder(project, location,
				configContext, loader);

		resolve(id, builder, new HashSet<String>(), new HashSet<String>());

		return builder;
	}

	protected void resolve(String id, ProjectBuilder builder,
			Set<String> proceedIds, Set<String> propertyNames)
			throws CoreException {
		if (proceedIds.contains(id)) {
			return;
		}
		proceedIds.add(id);

		ProjectConfig pc = all.get(id);
		IConfigurationElement current = pc.getConfigurationElement();

		IConfigurationElement[] propertyElements = current
				.getChildren("property");
		for (int i = 0; i < propertyElements.length; i++) {
			IConfigurationElement handNode = propertyElements[i];
			String name = handNode.getAttribute("name");
			if (propertyNames.contains(name) == false) {
				builder.addProperty(name, handNode.getAttribute("value"));
				propertyNames.add(name);
			}
		}

		String extendsAttr = current.getAttribute("extends");
		if (StringUtil.isEmpty(extendsAttr) == false) {
			String[] parentIds = extendsAttr.split("[ ]*,[ ]*");
			for (int i = 0; i < parentIds.length; i++) {
				resolve(parentIds[i], builder, proceedIds, propertyNames);
			}
		}
		String rootAttr = current.getAttribute("root");
		if (StringUtil.isEmpty(rootAttr) == false) {
			String[] roots = rootAttr.split("[ ]*,[ ]*");
			for (int i = 0; i < roots.length; i++) {
				builder.addRoot(roots[i]);
			}
		}
		IConfigurationElement[] handlerElements = current
				.getChildren("handler");
		for (int i = 0; i < handlerElements.length; i++) {
			IConfigurationElement handNode = handlerElements[i];
			ResourceHandler handler = createHandler(handNode);
			addEntries(handNode, builder, handler);
			builder.addHandler(handler);
		}

	}

	private ResourceHandler createHandler(IConfigurationElement handNode) {
		ResourceHandler handler = null;
		String type = handNode.getAttribute("type");
		IConfigurationElement factory = this.handlerfactories.get(type);
		try {
			handler = (ResourceHandler) factory
					.createExecutableExtension("class");
		} catch (CoreException e) {
			Activator.log(e);
		}
		if (handler == null) {
			handler = new DefaultHandler();
		}
		return handler;
	}

	private void addEntries(IConfigurationElement handNode,
			ProjectBuilder builder, ResourceHandler handler) {
		IConfigurationElement[] entries = handNode.getChildren("entry");
		for (int j = 0; j < entries.length; j++) {
			IConfigurationElement e = entries[j];
			Entry entry = new Entry();
			String[] names = e.getAttributeNames();
			for (int k = 0; k < names.length; k++) {
				String s = names[k];
				String value = e.getAttribute(s);
				if (StringUtil.isEmpty(value) == false) {
					value = ScriptingUtil.resolveString(value, builder
							.getConfigContext());
					entry.attribute.put(s, value);
				}
			}
			handler.add(entry);
		}
	}
}
