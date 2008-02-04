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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.seasar.dolteng.eclipse.loader.ResourceLoader;
import org.seasar.dolteng.eclipse.util.ScriptingUtil;
import org.seasar.dolteng.projects.handler.ResourceHandler;
import org.seasar.dolteng.projects.handler.impl.DefaultHandler;
import org.seasar.dolteng.projects.handler.impl.dicon.ComponentModel;
import org.seasar.dolteng.projects.handler.impl.dicon.DiconModel;
import org.seasar.dolteng.projects.handler.impl.dicon.IncludeModel;
import org.seasar.dolteng.projects.model.Entry;
import org.seasar.dolteng.projects.model.FacetConfig;
import org.seasar.eclipse.common.util.ExtensionAcceptor;
import org.seasar.framework.util.ArrayMap;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class ProjectBuildConfigResolver {

	private Map<String, IConfigurationElement> handlerFactories = new HashMap<String, IConfigurationElement>();

	private Map<String, ArrayMap/*<String, FacetConfig>*/> facetMap
			= new HashMap<String, ArrayMap/*<String, FacetConfig>*/>();
	
	private Map<String, FacetConfig> all = new HashMap<String, FacetConfig>();
	
	private static final String TAG_FACET = "facet";
	private static final String ATTR_FACET_ROOT = "root";
	private static final String ATTR_FACET_EXTENDS = "extends";

	private static final String TAG_PROPERTY = "property";
	private static final String ATTR_PROP_NAME = "name";
	private static final String ATTR_PROP_VALUE = "value";

	private static final String TAG_IF = "if";
	private static final String ATTR_IF_JRE = "jre";

	private static final String TAG_HANDLER = "handler";
	private static final String ATTR_HAND_TYPE = "type";
	private static final String ATTR_HAND_CLASS = "class";

	private static final String TAG_ENTRY = "entry";

	private static final String TAG_INCLUDE = "include";
	private static final String ATTR_INCLUDE_PATH ="path";
	
	private static final String TAG_COMPONENT = "component";
	private static final String ATTR_COMPONENT_NAME = "name";
	private static final String ATTR_COMPONENT_CLASS = "class";
	private static final String ATTR_COMPONENT_ASPECT = "aspect";

	private static final String TAG_ADD_CUSTOMIZER = "addCustomizer";
	private static final String TAG_REMOVE_CUSTOMIZER = "removeCustomizer";
	private static final String ATTR_CUSTOMIZER_NAME = "name";

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
						if (TAG_FACET.equals(e.getName())) {
							FacetConfig fc = new FacetConfig(e);
							if (fc.isVisibleFacet()) {
								for(String jre : fc.getJres()) {
									ArrayMap/*<String, FacetConfig>*/ pt = facetMap.get(jre);
									if(pt == null) {
										pt = new ArrayMap/*<String, FacetConfig>*/();
										facetMap.put(jre, pt);
									}
									pt.put(fc.getId(), fc);
								}
							}
							all.put(fc.getId(), fc);
						}
					}
				});
	}

	public Map<String, ArrayMap/*<String, FacetConfig>*/> getFacetMap() {
		return facetMap;
	}

	public ProjectBuilder resolve(String[] facetIds, IProject project, IPath location,
			Map<String, String> configContext, Set<String> propertyNames) throws CoreException {
		
		ProjectBuilder builder = new ProjectBuilder(project, location, configContext);
		
		Set<String> proceedIds = new HashSet<String>();
		for(String facetId : facetIds) {
			resolveProperty(facetId, builder, proceedIds, propertyNames);
		}
		
		proceedIds = new HashSet<String>();
		for(String facetId : facetIds) {
			resolveFacet(facetId, builder, proceedIds);
		}
		return builder;
	}

	protected void resolveProperty(String facetId, ProjectBuilder builder,
			Set<String> proceedIds, Set<String> propertyNames)
			throws CoreException {
		
		if (proceedIds.contains(facetId)) {
			return;
		}
		proceedIds.add(facetId);

		FacetConfig pc = all.get(facetId);
		IConfigurationElement currentFacetElement = pc.getConfigurationElement();
		
		registerProperty(builder, propertyNames, currentFacetElement);
		resolveExtendsProperty(builder, proceedIds, propertyNames, currentFacetElement);
		resolveIfProperty(builder, propertyNames, currentFacetElement);
	}
	
	private void resolveExtendsProperty(ProjectBuilder builder, Set<String> proceedIds, Set<String> propertyNames,
			IConfigurationElement current) throws CoreException {
		String extendsAttr = current.getAttribute(ATTR_FACET_EXTENDS);
		if (StringUtil.isEmpty(extendsAttr) == false) {
			for (String parentId : extendsAttr.split("[ ]*,[ ]*")) {
				resolveProperty(parentId, builder, proceedIds, propertyNames);
			}
		}
	}
	
	private void resolveIfProperty(ProjectBuilder builder, Set<String> propertyNames,
			IConfigurationElement facetNode) {
		for (IConfigurationElement ifNode : facetNode.getChildren(TAG_IF)) {
			String ifAttr = ifNode.getAttribute(ATTR_IF_JRE);
			String jreVersion = builder.getConfigContext()
					.get(org.seasar.dolteng.eclipse.Constants.CTX_JAVA_VERSION);
			for (String ver : ifAttr.split("[ ]*,[ ]*")) {
				if(jreVersion.equals(ver)) {
					registerProperty(builder, propertyNames, ifNode);
				}
			}
		}
	}

	private void registerProperty(ProjectBuilder builder, Set<String> propertyNames, IConfigurationElement element) {
		IConfigurationElement[] propertyElements = element
				.getChildren(TAG_PROPERTY);
		for (IConfigurationElement propNode : propertyElements) {
			String name = propNode.getAttribute(ATTR_PROP_NAME);
			if (propertyNames.contains(name) == false) {
				builder.addProperty(name, propNode.getAttribute(ATTR_PROP_VALUE));
				propertyNames.add(name);
			}
		}
	}

	protected void resolveFacet(String facetId, ProjectBuilder builder,
			Set<String> proceedIds)
			throws CoreException {
		
		if (proceedIds.contains(facetId)) {
			return;
		}
		proceedIds.add(facetId);

		FacetConfig pc = all.get(facetId);
		IConfigurationElement current = pc.getConfigurationElement();
		ResourceLoader loader = (ResourceLoader) current
				.createExecutableExtension(EXTENSION_POINT_RESOURCE_LOADER);
		
		resolveExtends(builder, proceedIds, current);
		resolveMain(loader, current, builder);
		resolveIf(loader, builder, current);
	}
	
	protected void resolveMain(ResourceLoader loader, IConfigurationElement current, ProjectBuilder builder) {
		registerRoot(builder, current);
		registerHandler(loader, builder, current);
	}

	private void resolveExtends(ProjectBuilder builder, Set<String> proceedIds,
			IConfigurationElement current) throws CoreException {
		String extendsAttr = current.getAttribute(ATTR_FACET_EXTENDS);
		if (StringUtil.isEmpty(extendsAttr) == false) {
			for (String parentId : extendsAttr.split("[ ]*,[ ]*")) {
				resolveFacet(parentId, builder, proceedIds);
			}
		}
	}
	
	private void resolveIf(ResourceLoader loader, ProjectBuilder builder,
			IConfigurationElement facetNode) {
		for (IConfigurationElement ifNode : facetNode.getChildren(TAG_IF)) {
			String ifAttr = ifNode.getAttribute(ATTR_IF_JRE);
			String jreVersion = builder.getConfigContext()
					.get(org.seasar.dolteng.eclipse.Constants.CTX_JAVA_VERSION);
			for (String ver : ifAttr.split("[ ]*,[ ]*")) {
				if(jreVersion.equals(ver)) {
//					registerProperty(builder, propertyNames, ifNode);
					resolveMain(loader, ifNode, builder);
				}
			}
		}
	}

	private void registerRoot(ProjectBuilder builder, IConfigurationElement element) {
		String rootAttr = element.getAttribute(ATTR_FACET_ROOT);
		if (StringUtil.isEmpty(rootAttr) == false) {
			for (String root : rootAttr.split("[ ]*,[ ]*")) {
				builder.addRoot(root);
			}
		}
	}

	private void registerHandler(ResourceLoader loader, ProjectBuilder builder, IConfigurationElement element) {
		for (IConfigurationElement handNode : element.getChildren(TAG_HANDLER)) {
			ResourceHandler handler = createHandler(handNode);
			addEntries(loader, handNode, builder, handler);
			builder.addHandler(handler);
		}
	}

	private ResourceHandler createHandler(IConfigurationElement handNode) {
		ResourceHandler handler = null;
		String type = handNode.getAttribute(ATTR_HAND_TYPE);
		IConfigurationElement factory = handlerFactories.get(type);
		try {
			handler = (ResourceHandler) factory
					.createExecutableExtension(ATTR_HAND_CLASS);
		} catch (CoreException e) {
			Activator.log(e);
		} catch (NullPointerException e) {
			Activator.log(new Exception("[ERROR] resource handler ("+type+") is not defined.", e));
		}
		if (handler == null) {
			handler = new DefaultHandler();
		}
		return handler;
	}

	private void addEntries(ResourceLoader loader, IConfigurationElement handNode,
			ProjectBuilder builder, ResourceHandler handler) {
		// TODO ↓typeで判断するのが微妙。
		if(handNode.getAttribute("type").endsWith("Dicon")) {
			String diconName = handNode.getAttribute("type").substring(0, handNode.getAttribute("type").length() - 5);
			DiconModel model = DiconModel.getInstance(diconName);
			for (IConfigurationElement includeElement : handNode.getChildren(TAG_INCLUDE)) {
				String includePath = includeElement.getAttribute(ATTR_INCLUDE_PATH);
				model.addChild(new IncludeModel(includePath));
			}
			for (IConfigurationElement componentElement : handNode.getChildren(TAG_COMPONENT)) {
				String componentName = componentElement.getAttribute(ATTR_COMPONENT_NAME);
				if(componentElement.getChildren(TAG_ADD_CUSTOMIZER).length != 0
						|| componentElement.getChildren(TAG_REMOVE_CUSTOMIZER).length == 0) {
					model.addChild(new ComponentModel(componentName, componentElement.getAttribute(ATTR_COMPONENT_CLASS)));
				}
				for(IConfigurationElement customizerElement : componentElement.getChildren(TAG_ADD_CUSTOMIZER)) {
					model.addCustomizerTo(componentName,
							customizerElement.getAttribute(ATTR_CUSTOMIZER_NAME),
							customizerElement.getAttribute(ATTR_COMPONENT_ASPECT));
				}
				for(IConfigurationElement customizerElement : componentElement.getChildren(TAG_REMOVE_CUSTOMIZER)) {
					model.removeCustomizerFrom(componentName,
							customizerElement.getAttribute(ATTR_CUSTOMIZER_NAME));
				}
			}
		} else {
			for (IConfigurationElement entryElement : handNode.getChildren(TAG_ENTRY)) {
				Entry entry = new Entry(loader);
				for (String key : entryElement.getAttributeNames()) {
					String value = entryElement.getAttribute(key);
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
}
