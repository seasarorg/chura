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
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import jp.aonir.fuzzyxml.FuzzyXMLAttribute;
import jp.aonir.fuzzyxml.FuzzyXMLCDATA;
import jp.aonir.fuzzyxml.FuzzyXMLDocument;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLNode;
import jp.aonir.fuzzyxml.FuzzyXMLParser;
import jp.aonir.fuzzyxml.XPath;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Plugin;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;
import org.seasar.dolteng.eclipse.util.ResourcesUtil;
import org.seasar.dolteng.eclipse.util.ScriptingUtil;
import org.seasar.framework.util.InputStreamReaderUtil;
import org.seasar.framework.util.InputStreamUtil;
import org.seasar.framework.util.ReaderUtil;
import org.seasar.framework.util.StringUtil;
import org.seasar.framework.util.URLUtil;

/**
 * @author taichi
 * 
 */
public class ProjectBuildConfigResolver {
    private static final Pattern txtextensions = Pattern.compile(
            ".*\\.(txt|java|dicon|properties|tomcatplugin|mf|x?html?)$",
            Pattern.CASE_INSENSITIVE);

    private Map configContext;

    private Map handlerfactories = new HashMap();

    private FuzzyXMLDocument projectConfig;

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
     * <li>testJavaPath</li>
     * <li>testResourcePath</li>
     * <li>testOutPath</li>
     * </ul>
     * 
     * @param m
     */
    public ProjectBuildConfigResolver(Map m) {
        this.configContext = m;
    }

    public void initialize() {
        URL url = getTemplateResourceURL("projects.xml");
        InputStream in = null;
        try {
            FuzzyXMLParser parser = new FuzzyXMLParser();
            projectConfig = parser.parse(new BufferedInputStream(URLUtil
                    .openStream(url)));
            handlerfactories.put("default", new DefaultHandlerFactory());
            handlerfactories.put("classpath", new ClasspathHandlerFactory());
            // handlerfactories.put("dolteng", null);
            // handlerfactories.put("diigu", null);
            // handlerfactories.put("tomcat", null);
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
                p.name = e.getAttributeNode("name").getValue();
            }
        }
        return (ProjectDisplay[]) result.toArray(new ProjectDisplay[result
                .size()]);
    }

    public class ProjectDisplay {
        public String name;

        public String description;
    }

    public static String getTemplateResourceTxt(String path) {
        URL url = getTemplateResourceURL(path);
        return getTemplateResourceTxt(url);
    }

    private static String getTemplateResourceTxt(URL url) {
        Reader reader = InputStreamReaderUtil.create(URLUtil.openStream(url),
                "UTF-8");
        return ReaderUtil.readText(reader);
    }

    private static URL getTemplateResourceURL(String path) {
        Plugin plugin = DoltengCore.getDefault();
        return plugin.getBundle().getEntry("template/" + path);
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
        StringBuffer stb = new StringBuffer();
        stb.append("//project id=\"");
        stb.append(id);
        stb.append("\"/handler");

        FuzzyXMLNode[] nodes = XPath.selectNodes(projectConfig
                .getDocumentElement(), stb.toString());

        if (0 < nodes.length) {
            FuzzyXMLElement parent = (FuzzyXMLElement) nodes[0].getParentNode();
            if (parent.hasAttribute("root")) {
                builder.add(parent.getAttributeNode("root").getValue());
            }
            if (parent.hasAttribute("extends")) {
                String parentId = parent.getAttributeNode("extends").getValue();
                internalResolve(parentId, builder, proceedIds);
            }
        }
        for (int i = 0; i < nodes.length; i++) {
            FuzzyXMLNode node = nodes[i];
            if (node instanceof FuzzyXMLElement) {
                FuzzyXMLElement handNode = (FuzzyXMLElement) node;
                ResourceHandler handler = createHandler(handNode);
                addEntries(handNode, handler);
                builder.addHandler(handler);
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

    private void addEntries(FuzzyXMLElement handNode, ResourceHandler handler) {
        FuzzyXMLNode[] entries = handNode.getChildren();
        for (int j = 0; j < entries.length; j++) {
            FuzzyXMLNode entNode = entries[j];
            if (entNode instanceof FuzzyXMLElement) {
                Entry entry = new Entry();
                FuzzyXMLElement e = (FuzzyXMLElement) entNode;
                FuzzyXMLAttribute[] attrs = e.getAttributes();
                for (int k = 0; k < attrs.length; k++) {
                    FuzzyXMLAttribute a = attrs[k];
                    entry.attribute.put(a.getName(), a.getValue());
                }
                String kind = (String) entry.attribute.remove("kind");
                if (StringUtil.isEmpty(kind) == false) {
                    entry.kind = kind;
                }
                entry.path = ScriptingUtil.resolveString(
                        (String) entry.attribute.remove("path"), configContext);
                if (StringUtil.isEmpty(entry.path)) {
                    continue;
                }
                handler.add(entry);
            }
        }
    }

    public class Entry {
        private Map attribute = new HashMap();

        private String kind = "path";

        private String path = "";

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

    private abstract class AbstractResourceHandler implements ResourceHandler {
        protected Set entries = new HashSet();

        public int getNumberOfFiles() {
            return entries.size();
        }

        public void add(Entry entry) {
            entries.add(entry);
        }

        public void merge(ResourceHandler handler) {
            AbstractResourceHandler arh = (AbstractResourceHandler) handler;
            this.entries.addAll(arh.entries);
        }

        public void handle(ProjectBuilder builder, IProgressMonitor monitor) {
            for (final Iterator i = entries.iterator(); i.hasNext();) {
                handle(builder, (Entry) i.next());
                ProgressMonitorUtil.isCanceled(monitor, 1);
            }
        }

        protected void handle(ProjectBuilder builder, Entry e) {
            if ("path".equals(e.kind)) {
                ResourcesUtil.createDir(builder.getProjectHandle(), e.path);
            } else if ("file".equals(e.kind)) {
                URL url = builder.findResource(e.path);
                if (url != null) {
                    if (txtextensions.matcher(url.getPath()).matches()) {
                        processTxt(builder, url);
                    } else {
                        process(builder, url);
                    }
                } else {
                    DoltengCore.log("missing ..." + e.path);
                }
            }
        }

        protected void processTxt(ProjectBuilder builder, URL url) {
            // TODO 未実装
        }

        protected void process(ProjectBuilder builder, URL url) {
            // TODO 未実装
        }
    }

    private class DefaultHandler extends AbstractResourceHandler {
        public String getType() {
            return "default";
        }
    }

    private class ClasspathHandler extends AbstractResourceHandler {

        public String getType() {
            return "classpath";
        }

        public void handle(IProject project, IProgressMonitor monitor) {
            // TODO 未実装
        }

    }
}
