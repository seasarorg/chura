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

import static org.seasar.dolteng.projects.Constants.ATTR_APP_TYPE_ID;
import static org.seasar.dolteng.projects.Constants.ATTR_APP_TYPE_NAME;
import static org.seasar.dolteng.projects.Constants.ATTR_ASPECT_CUSTOMIZER_ARG;
import static org.seasar.dolteng.projects.Constants.ATTR_CATEGORY_ID;
import static org.seasar.dolteng.projects.Constants.ATTR_CATEGORY_KEY;
import static org.seasar.dolteng.projects.Constants.ATTR_CATEGORY_NAME;
import static org.seasar.dolteng.projects.Constants.ATTR_COMPONENT_CLASS;
import static org.seasar.dolteng.projects.Constants.ATTR_COMPONENT_NAME;
import static org.seasar.dolteng.projects.Constants.ATTR_CUSTOMIZER_ASPECT;
import static org.seasar.dolteng.projects.Constants.ATTR_CUSTOMIZER_NAME;
import static org.seasar.dolteng.projects.Constants.ATTR_DEFAULT_FACET;
import static org.seasar.dolteng.projects.Constants.ATTR_DISABLE_CATEGORY;
import static org.seasar.dolteng.projects.Constants.ATTR_DISABLE_FACET;
import static org.seasar.dolteng.projects.Constants.ATTR_FACET_EXTENDS;
import static org.seasar.dolteng.projects.Constants.ATTR_FACET_ROOT;
import static org.seasar.dolteng.projects.Constants.ATTR_FIRST_FACET;
import static org.seasar.dolteng.projects.Constants.ATTR_HAND_CLASS;
import static org.seasar.dolteng.projects.Constants.ATTR_HAND_TYPE;
import static org.seasar.dolteng.projects.Constants.ATTR_IF_JRE;
import static org.seasar.dolteng.projects.Constants.ATTR_INCLUDE_PATH;
import static org.seasar.dolteng.projects.Constants.ATTR_LAST_FACET;
import static org.seasar.dolteng.projects.Constants.ATTR_PROP_NAME;
import static org.seasar.dolteng.projects.Constants.ATTR_PROP_VALUE;
import static org.seasar.dolteng.projects.Constants.EXTENSION_POINT_NEW_PROJECT;
import static org.seasar.dolteng.projects.Constants.EXTENSION_POINT_RESOURCE_HANDLER;
import static org.seasar.dolteng.projects.Constants.EXTENSION_POINT_RESOURCE_LOADER;
import static org.seasar.dolteng.projects.Constants.ID_PLUGIN;
import static org.seasar.dolteng.projects.Constants.TAG_ADD_ASPECT_CUSTOMIZER;
import static org.seasar.dolteng.projects.Constants.TAG_ADD_CUSTOMIZER;
import static org.seasar.dolteng.projects.Constants.TAG_APP_TYPE;
import static org.seasar.dolteng.projects.Constants.TAG_CATEGORY;
import static org.seasar.dolteng.projects.Constants.TAG_COMPONENT;
import static org.seasar.dolteng.projects.Constants.TAG_DEFAULT;
import static org.seasar.dolteng.projects.Constants.TAG_DISABLE;
import static org.seasar.dolteng.projects.Constants.TAG_ENTRY;
import static org.seasar.dolteng.projects.Constants.TAG_FACET;
import static org.seasar.dolteng.projects.Constants.TAG_FIRST;
import static org.seasar.dolteng.projects.Constants.TAG_HANDLER;
import static org.seasar.dolteng.projects.Constants.TAG_IF;
import static org.seasar.dolteng.projects.Constants.TAG_INCLUDE;
import static org.seasar.dolteng.projects.Constants.TAG_LAST;
import static org.seasar.dolteng.projects.Constants.TAG_PROPERTY;
import static org.seasar.dolteng.projects.Constants.TAG_REMOVE_CUSTOMIZER;

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
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.loader.ResourceLoader;
import org.seasar.dolteng.eclipse.util.ScriptingUtil;
import org.seasar.dolteng.projects.handler.ResourceHandler;
import org.seasar.dolteng.projects.handler.impl.DefaultHandler;
import org.seasar.dolteng.projects.handler.impl.DiconHandler;
import org.seasar.dolteng.projects.model.ApplicationType;
import org.seasar.dolteng.projects.model.Entry;
import org.seasar.dolteng.projects.model.FacetCategory;
import org.seasar.dolteng.projects.model.FacetConfig;
import org.seasar.dolteng.projects.model.dicon.ComponentModel;
import org.seasar.dolteng.projects.model.dicon.DiconModel;
import org.seasar.dolteng.projects.model.dicon.IncludeModel;
import org.seasar.eclipse.common.util.ExtensionAcceptor;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class ProjectBuildConfigResolver {

    private Map<String, IConfigurationElement> handlerFactories = new HashMap<String, IConfigurationElement>();

    private List<FacetCategory> categoryList = new ArrayList<FacetCategory>();

    private List<ApplicationType> applicationTypeList = new ArrayList<ApplicationType>();

    private List<FacetConfig> allFacets = new ArrayList<FacetConfig>();

    public ProjectBuildConfigResolver() {
    }

    /**
     * リゾルバを初期化する。
     * 
     * 以前読み込んだ情報を全て破棄し、拡張ポイントを読み込み直す。
     */
    public void initialize() {
        handlerFactories = new HashMap<String, IConfigurationElement>();
        categoryList = new ArrayList<FacetCategory>();
        applicationTypeList = new ArrayList<ApplicationType>();
        allFacets = new ArrayList<FacetConfig>();

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
                            allFacets.add(new FacetConfig(e));
                        }
                    }
                });

        ExtensionAcceptor.accept(Constants.ID_PLUGIN, "projectType",
                new ExtensionAcceptor.ExtensionVisitor() {
                    public void visit(IConfigurationElement e) {
                        if (TAG_CATEGORY.equals(e.getName())) {
                            String categoryId = e
                                    .getAttribute(ATTR_CATEGORY_ID);
                            if (getCategoryById(categoryId) == null) {
                                FacetCategory category = new FacetCategory(
                                        categoryId,
                                        e.getAttribute(ATTR_CATEGORY_KEY),
                                        e.getAttribute(ATTR_CATEGORY_NAME));
                                categoryList.add(category);
                            }
                        } else if (TAG_APP_TYPE.equals(e.getName())) {
                            String applicationTypeId = e
                                    .getAttribute(ATTR_APP_TYPE_ID);
                            ApplicationType type = getApplicationType(applicationTypeId);
                            if (type == null) {
                                type = new ApplicationType(applicationTypeId, e
                                        .getAttribute(ATTR_APP_TYPE_NAME));
                                applicationTypeList.add(type);
                            }

                            IConfigurationElement[] defaultTag = e
                                    .getChildren(TAG_DEFAULT);
                            for (IConfigurationElement child : defaultTag) {
                                for (String facetId : child.getAttribute(
                                        ATTR_DEFAULT_FACET).split("[ ]*,[ ]*")) {
                                    type.addDefaultFacet(facetId);
                                }
                            }

                            IConfigurationElement[] disableTag = e
                                    .getChildren(TAG_DISABLE);
                            for (IConfigurationElement child : disableTag) {
                                String category = child
                                        .getAttribute(ATTR_DISABLE_CATEGORY);
                                String facet = child
                                        .getAttribute(ATTR_DISABLE_FACET);
                                type.disableCategory(category);
                                type.disableFacet(facet);
                            }

                            IConfigurationElement[] firstTag = e
                                    .getChildren(TAG_FIRST);
                            for (IConfigurationElement child : firstTag) {
                                String firstFacet = child
                                        .getAttribute(ATTR_FIRST_FACET);
                                type.addFirst(firstFacet);
                            }

                            IConfigurationElement[] lastTag = e
                                    .getChildren(TAG_LAST);
                            for (IConfigurationElement child : lastTag) {
                                String lastFacet = child
                                        .getAttribute(ATTR_LAST_FACET);
                                type.addLast(lastFacet);
                            }
                        }
                    }
                });
    }

    /**
     * 拡張ボインとに設定された全てのファセット情報のリストを得る。 選択不可ファセット（displayOrderが設定されていない）も含む。
     * 
     * @return ファセット情報のリスト
     */
    public List<FacetConfig> getAllFacets() {
        return allFacets;
    }

    /**
     * 選択可能なファセット（displayOrderが設定されている）のリストを得る。
     * 
     * @return ファセット情報のリスト
     */
    public List<FacetConfig> getSelectableFacets() {
        List<FacetConfig> result = new ArrayList<FacetConfig>();
        for (FacetConfig fc : allFacets) {
            if (fc.isSelectableFacet()) {
                result.add(fc);
            }
        }
        return result;
    }

    /**
     * ファセットIDからファセット情報を取得する。
     * 
     * @param facetId
     *            取得したいファセットID。nullであった場合は<code>IllegalArgumentException</code>がスローされる。
     * @return ファセット情報。見つからなかった場合は<code>null</code>を返す。
     */
    public FacetConfig getFacet(String facetId) {
        if (facetId == null) {
            throw new IllegalArgumentException("facetId is null.");
        }
        for (FacetConfig fc : allFacets) {
            if (facetId.equals(fc.getId())) {
                return fc;
            }
        }
        DoltengCore.log("facet not found: " + facetId);
        return null;
    }

    /**
     * 拡張ボインとに設定されたファセットカテゴリ情報のリストを得る。
     * 
     * @return ファセットカテゴリ情報のリスト
     */
    public List<FacetCategory> getCategoryList() {
        return categoryList;
    }

    /**
     * カテゴリIDからファセットカテゴリ情報を取得する。
     * 
     * @param categoryId
     *            取得したいファセットカテゴリID。nullであった場合は
     *            <code>IllegalArgumentException</code>がスローされる。
     * @return ファセットカテゴリ情報。見つからなかった場合は<code>null</code>を返す。
     */
    public FacetCategory getCategoryById(String categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("categoryId is null.");
        }
        for (FacetCategory category : categoryList) {
            if (categoryId.equals(category.getId())) {
                return category;
            }
        }
        return null;
    }

    /**
     * カテゴリキー（アルファベット2文字）からファセットカテゴリ情報を取得する。
     * 
     * @param categoryKey
     *            取得したいファセットカテゴリキー。nullであった場合や、アルファベット2文字でなかった場合は
     *            <code>IllegalArgumentException</code>がスローされる。
     * @return ファセットカテゴリ情報。見つからなかった場合は<code>null</code>を返す。
     */
    public FacetCategory getCategoryByKey(String categoryKey) {
        if (categoryKey == null || categoryKey.length() != 2) {
            throw new IllegalArgumentException("categoryKey is null.");
        }
        for (FacetCategory category : categoryList) {
            if (categoryKey.equals(category.getKey())) {
                return category;
            }
        }
        return null;
    }

    /**
     * 拡張ボインとに設定されたアプリケーションタイプ情報のリストを得る。
     * 
     * @return アプリケーションタイプ情報のリスト
     */
    public List<ApplicationType> getApplicationTypeList() {
        return applicationTypeList;
    }

    /**
     * アプリケーションタイプIDからアプリケーションタイプ情報を取得する。
     * 
     * @param applicationTypeId
     *            取得したいアプリケーションタイプID。nullであった場合は
     *            <code>IllegalArgumentException</code>がスローされる。
     * @return アプリケーションタイプ情報。見つからなかった場合は<code>null</code>を返す。
     */
    public ApplicationType getApplicationType(String applicationTypeId) {
        if (applicationTypeId == null) {
            throw new IllegalArgumentException("applicationTypeId is null.");
        }
        for (ApplicationType at : applicationTypeList) {
            if (applicationTypeId.equals(at.getId())) {
                return at;
            }
        }
        return null;
    }

    /**
     * プロジェクトビルダを生成する。
     * 
     * @param facetIds
     *            ファセットIDの配列
     * @param project
     *            ビルドされるプロジェクト
     * @param location
     *            プロジェクトロケーション
     * @param configContext
     *            ビルドコンテキスト情報
     * @param propertyNames
     *            ユーザによって設定されたプロパティ。ここにセットされたプロパティは拡張ポイント設定によって上書きしてはいけない。
     * @return 生成されたプロジェクトビルダ
     * @throws CoreException
     */
    public ProjectBuilder resolve(String[] facetIds, IProject project,
            IPath location, Map<String, String> configContext,
            Set<String> propertyNames) throws CoreException {

        ProjectBuilder builder = new ProjectBuilder(project, location,
                configContext);

        Set<String> proceedIds = new HashSet<String>();
        for (String facetId : facetIds) {
            resolveProperty(facetId, builder, proceedIds, propertyNames);
        }

        proceedIds = new HashSet<String>();
        for (String facetId : facetIds) {
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

        FacetConfig pc = getFacet(facetId);
        IConfigurationElement currentFacetElement = pc
                .getConfigurationElement();

        registerProperty(builder, propertyNames, currentFacetElement);
        resolveExtendsProperty(builder, proceedIds, propertyNames,
                currentFacetElement);
        resolveIfProperty(builder, propertyNames, currentFacetElement);
    }

    private void resolveExtendsProperty(ProjectBuilder builder,
            Set<String> proceedIds, Set<String> propertyNames,
            IConfigurationElement current) throws CoreException {
        String extendsAttr = current.getAttribute(ATTR_FACET_EXTENDS);
        if (StringUtil.isEmpty(extendsAttr) == false) {
            for (String parentId : extendsAttr.split("[ ]*,[ ]*")) {
                resolveProperty(parentId, builder, proceedIds, propertyNames);
            }
        }
    }

    private void resolveIfProperty(ProjectBuilder builder,
            Set<String> propertyNames, IConfigurationElement facetNode) {
        for (IConfigurationElement ifNode : facetNode.getChildren(TAG_IF)) {
            String ifAttr = ifNode.getAttribute(ATTR_IF_JRE);
            String jreVersion = builder.getConfigContext().get(
                    org.seasar.dolteng.eclipse.Constants.CTX_JAVA_VERSION);
            for (String ver : ifAttr.split("[ ]*,[ ]*")) {
                if (jreVersion.equals(ver)) {
                    registerProperty(builder, propertyNames, ifNode);
                }
            }
        }
    }

    private void registerProperty(ProjectBuilder builder,
            Set<String> propertyNames, IConfigurationElement element) {
        IConfigurationElement[] propertyElements = element
                .getChildren(TAG_PROPERTY);
        for (IConfigurationElement propNode : propertyElements) {
            String name = propNode.getAttribute(ATTR_PROP_NAME);
            if (propertyNames.contains(name) == false) {
                builder.addProperty(name, propNode
                        .getAttribute(ATTR_PROP_VALUE));
                propertyNames.add(name);
            }
        }
    }

    protected void resolveFacet(String facetId, ProjectBuilder builder,
            Set<String> proceedIds) throws CoreException {

        if (proceedIds.contains(facetId)) {
            return;
        }
        proceedIds.add(facetId);

        FacetConfig pc = getFacet(facetId);
        IConfigurationElement current = pc.getConfigurationElement();
        ResourceLoader loader = (ResourceLoader) current
                .createExecutableExtension(EXTENSION_POINT_RESOURCE_LOADER);

        resolveExtends(builder, proceedIds, current);
        resolveMain(loader, current, builder);
        resolveIf(loader, builder, current);
    }

    protected void resolveMain(ResourceLoader loader,
            IConfigurationElement current, ProjectBuilder builder) {
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
            String jreVersion = builder.getConfigContext().get(
                    org.seasar.dolteng.eclipse.Constants.CTX_JAVA_VERSION);
            for (String ver : ifAttr.split("[ ]*,[ ]*")) {
                if (jreVersion.equals(ver)) {
                    // registerProperty(builder, propertyNames, ifNode);
                    resolveMain(loader, ifNode, builder);
                }
            }
        }
    }

    private void registerRoot(ProjectBuilder builder,
            IConfigurationElement element) {
        String rootAttr = element.getAttribute(ATTR_FACET_ROOT);
        if (StringUtil.isEmpty(rootAttr) == false) {
            for (String root : rootAttr.split("[ ]*,[ ]*")) {
                builder.addRoot(root);
            }
        }
    }

    private void registerHandler(ResourceLoader loader, ProjectBuilder builder,
            IConfigurationElement element) {
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
            DoltengCore.log(e);
        } catch (NullPointerException e) {
            DoltengCore.log("resource handler (" + type + ") is not defined.",
                    e);
        }
        if (handler == null) {
            handler = new DefaultHandler();
        }
        return handler;
    }

    private void addEntries(ResourceLoader loader,
            IConfigurationElement handNode, ProjectBuilder builder,
            ResourceHandler handler) {
        if (handler instanceof DiconHandler) {
            DiconModel model = ((DiconHandler) handler).getModel();
            for (IConfigurationElement includeElement : handNode
                    .getChildren(TAG_INCLUDE)) {
                String includePath = includeElement
                        .getAttribute(ATTR_INCLUDE_PATH);
                model.addChild(new IncludeModel(includePath));
            }
            for (IConfigurationElement componentElement : handNode
                    .getChildren(TAG_COMPONENT)) {
                String componentName = componentElement
                        .getAttribute(ATTR_COMPONENT_NAME);
                if (componentElement.getChildren(TAG_ADD_CUSTOMIZER).length != 0
                        || componentElement.getChildren(TAG_REMOVE_CUSTOMIZER).length == 0) {
                    model
                            .addChild(new ComponentModel(componentName,
                                    componentElement
                                            .getAttribute(ATTR_COMPONENT_CLASS)));
                }
                for (IConfigurationElement customizerElement : componentElement
                        .getChildren(TAG_ADD_ASPECT_CUSTOMIZER)) {
                    model.addAspectCustomizerTo(componentName,
                            customizerElement
                                    .getAttribute(ATTR_ASPECT_CUSTOMIZER_ARG));
                }
                for (IConfigurationElement customizerElement : componentElement
                        .getChildren(TAG_ADD_CUSTOMIZER)) {
                    model.addCustomizerTo(componentName, customizerElement
                            .getAttribute(ATTR_CUSTOMIZER_NAME),
                            customizerElement
                                    .getAttribute(ATTR_CUSTOMIZER_ASPECT));
                }
                for (IConfigurationElement customizerElement : componentElement
                        .getChildren(TAG_REMOVE_CUSTOMIZER)) {
                    model.removeCustomizerFrom(componentName, customizerElement
                            .getAttribute(ATTR_CUSTOMIZER_NAME),
                            customizerElement
                                    .getAttribute(ATTR_CUSTOMIZER_ASPECT));
                }
            }
        } else {
            for (IConfigurationElement entryElement : handNode
                    .getChildren(TAG_ENTRY)) {
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
