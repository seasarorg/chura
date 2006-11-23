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

    private Map handlerfactories = new HashMap();

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
            // TODO extension point で差し込める様にする。
            handlerfactories.put("default", new DefaultHandlerFactory());
            handlerfactories.put("classpath", new ClasspathHandlerFactory());
            handlerfactories.put("dolteng", new DoltengHandlerFactory());
            handlerfactories.put("diigu", new DiiguHandlerFactory());
            handlerfactories.put("tomcat", new TomcatHandlerFactory());
            handlerfactories.put("h2", new H2HandlerFactory());
            handlerfactories.put("jdt", new JDTHandlerFactory());
            handlerfactories.put("dblauncher", new DbLauncherHandlerFactory());
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
        List result = new ArrayList(nodes.length);
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

    public class ProjectDisplay implements Comparable {
        public String id;

        public String name;

        public String description;

        public String displayOrder;

        public int compareTo(Object o) {
            if (o instanceof ProjectDisplay) {
                ProjectDisplay other = (ProjectDisplay) o;
                return displayOrder.compareTo(other.displayOrder);
            }
            return 0;
        }
    }

    public void resolve(String id, ProjectBuilder builder) {
        internalResolve(id, builder, new HashSet());
    }

    protected void internalResolve(String id, ProjectBuilder builder,
            Set proceedIds) {
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
            ResourceHandlerFactory factory = (ResourceHandlerFactory) this.handlerfactories
                    .get(type);
            if (factory != null) {
                handler = factory.create();
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
        Map attribute = new HashMap();

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

    public interface ResourceHandlerFactory {
        ResourceHandler create();
    }

    private class DefaultHandlerFactory implements ResourceHandlerFactory {
        public ResourceHandler create() {
            return new DefaultHandler();
        }
    }

    private class ClasspathHandlerFactory implements ResourceHandlerFactory {
        public ResourceHandler create() {
            return new ClasspathHandler();
        }
    }

    private class DoltengHandlerFactory implements ResourceHandlerFactory {
        public ResourceHandler create() {
            return new DoltengHandler();
        }
    }

    private class DiiguHandlerFactory implements ResourceHandlerFactory {
        public ResourceHandler create() {
            return new DiiguHandler();
        }
    }

    private class TomcatHandlerFactory implements ResourceHandlerFactory {
        public ResourceHandler create() {
            return new TomcatHandler();
        }
    }

    private class H2HandlerFactory implements ResourceHandlerFactory {
        public ResourceHandler create() {
            return new H2Handler();
        }
    }

    private class JDTHandlerFactory implements ResourceHandlerFactory {
        public ResourceHandler create() {
            return new JDTHandler();
        }
    }

    private class DbLauncherHandlerFactory implements ResourceHandlerFactory {
        public ResourceHandler create() {
            return new DbLauncherHandler();
        }
    }
}
