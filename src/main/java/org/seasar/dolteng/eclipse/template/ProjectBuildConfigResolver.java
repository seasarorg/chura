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
package org.seasar.dolteng.eclipse.template;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import jp.aonir.fuzzyxml.FuzzyXMLAttribute;
import jp.aonir.fuzzyxml.FuzzyXMLCDATA;
import jp.aonir.fuzzyxml.FuzzyXMLDocument;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLNode;
import jp.aonir.fuzzyxml.FuzzyXMLParser;
import jp.aonir.fuzzyxml.XPath;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.util.FuzzyXMLUtil;
import org.seasar.dolteng.eclipse.util.ResourcesUtil;
import org.seasar.dolteng.eclipse.util.ScriptingUtil;
import org.seasar.framework.util.InputStreamUtil;
import org.seasar.framework.util.StringUtil;
import org.seasar.framework.util.URLUtil;

/**
 * @author taichi
 * 
 */
public class ProjectBuildConfigResolver {

    private Map<String, IConfigurationElement> handlerfactories = new HashMap<String, IConfigurationElement>();

    private FuzzyXMLDocument projectConfig;

    public ProjectBuildConfigResolver() {
    }

    public void initialize() {
        URL url = ResourcesUtil.getTemplateResourceURL("projects.xml");
        InputStream in = null;
        try {
            FuzzyXMLParser parser = new FuzzyXMLParser();
            projectConfig = parser.parse(new BufferedInputStream(URLUtil
                    .openStream(url)));
            this.handlerfactories = DoltengCore.loadHandlerFactries();
        } catch (Exception e) {
            DoltengCore.log(e);
        } finally {
            InputStreamUtil.close(in);
        }
    }

    public ProjectDisplay[] getProjects(String jre) {
        StringBuffer stb = new StringBuffer();
        stb.append("//project[@name][@jre=\"");
        stb.append(jre);
        stb.append("\"]");
        stb.append("/description[@language=\"");
        stb.append(Locale.getDefault().getLanguage());
        stb.append("\"]/text()");

        FuzzyXMLNode[] nodes = XPath.selectNodes(projectConfig
                .getDocumentElement(), stb.toString());
        List<ProjectDisplay> result = new ArrayList<ProjectDisplay>(
                nodes.length);
        for (int i = 0; i < nodes.length; i++) {
            FuzzyXMLNode node = nodes[i];
            if (node instanceof FuzzyXMLCDATA) {
                FuzzyXMLCDATA data = (FuzzyXMLCDATA) node;
                ProjectDisplay p = new ProjectDisplay();
                p.description = data.getValue();
                FuzzyXMLElement e = (FuzzyXMLElement) data.getParentNode()
                        .getParentNode();
                p.id = FuzzyXMLUtil.getAttribute(e, "id");
                p.name = FuzzyXMLUtil.getAttribute(e, "name");
                p.displayOrder = FuzzyXMLUtil.getAttribute(e, "order");
                result.add(p);
            }
        }
        return (ProjectDisplay[]) result.toArray(new ProjectDisplay[result
                .size()]);
    }

    public void resolve(String id, ProjectBuilder builder) {
        internalResolve(id, builder, new HashSet<String>());
    }

    protected void internalResolve(String id, ProjectBuilder builder,
            Set<String> proceedIds) {
        if (proceedIds.contains(id)) {
            return;
        }
        proceedIds.add(id);

        FuzzyXMLNode[] parents = XPath.selectNodes(projectConfig
                .getDocumentElement(), "//project[@id=\"" + id + "\"]");

        if (1 == parents.length) {
            FuzzyXMLElement parent = (FuzzyXMLElement) parents[0];
            if (parent.hasAttribute("root")) {
                String[] roots = parent.getAttributeNode("root").getValue()
                        .split("[ ]*,[ ]*");
                for (int i = 0; i < roots.length; i++) {
                    builder.add(roots[i]);
                }
            }
            if (parent.hasAttribute("extends")) {
                String[] parentIds = parent.getAttributeNode("extends")
                        .getValue().split("[ ]*,[ ]*");
                for (int i = 0; i < parentIds.length; i++) {
                    internalResolve(parentIds[i], builder, proceedIds);
                }
            }
            FuzzyXMLNode[] nodes = XPath.selectNodes(parent, "//handler");
            for (int i = 0; i < nodes.length; i++) {
                FuzzyXMLNode node = nodes[i];
                if (node instanceof FuzzyXMLElement) {
                    FuzzyXMLElement handNode = (FuzzyXMLElement) node;
                    ResourceHandler handler = createHandler(handNode);
                    addEntries(handNode, builder, handler);
                    builder.addHandler(handler);
                }
            }
        }
    }

    private ResourceHandler createHandler(FuzzyXMLElement handNode) {
        ResourceHandler handler = null;
        if (handNode.hasAttribute("type")) {
            String type = handNode.getAttributeNode("type").getValue();
            IConfigurationElement factory = this.handlerfactories.get(type);
            try {
                handler = (ResourceHandler) factory
                        .createExecutableExtension("class");
            } catch (CoreException e) {
                DoltengCore.log(e);
            }
        }
        if (handler == null) {
            handler = new DefaultHandler();
        }
        return handler;
    }

    private void addEntries(FuzzyXMLElement handNode, ProjectBuilder builder,
            ResourceHandler handler) {
        FuzzyXMLNode[] entries = handNode.getChildren();
        for (int j = 0; j < entries.length; j++) {
            FuzzyXMLNode entNode = entries[j];
            if (entNode instanceof FuzzyXMLElement) {
                Entry entry = new Entry();
                FuzzyXMLElement e = (FuzzyXMLElement) entNode;
                FuzzyXMLAttribute[] attrs = e.getAttributes();
                for (int k = 0; k < attrs.length; k++) {
                    FuzzyXMLAttribute a = attrs[k];
                    String value = ScriptingUtil.resolveString(a.getValue(),
                            builder.getConfigContext());
                    entry.attribute.put(a.getName(), value);
                }
                String kind = (String) entry.attribute.remove("kind");
                if (StringUtil.isEmpty(kind) == false) {
                    entry.kind = kind;
                }
                String path = (String) entry.attribute.remove("path");
                if (StringUtil.isEmpty(path) == false) {
                    entry.path = path;
                    handler.add(entry);
                }
            }
        }
    }

    public class Entry {
        Map<String, String> attribute = new HashMap<String, String>();

        String kind = "path";

        String path = "";

        public int hashCode() {
            return path.hashCode();
        }

        public boolean equals(Object obj) {
            if (obj instanceof Entry) {
                Entry e = (Entry) obj;
                return path.equals(e.path);
            }
            return false;
        }
    }
}
