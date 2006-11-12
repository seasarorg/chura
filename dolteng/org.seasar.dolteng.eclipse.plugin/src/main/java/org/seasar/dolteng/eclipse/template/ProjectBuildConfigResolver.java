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

import jp.aonir.fuzzyxml.FuzzyXMLCDATA;
import jp.aonir.fuzzyxml.FuzzyXMLDocument;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLNode;
import jp.aonir.fuzzyxml.FuzzyXMLParser;
import jp.aonir.fuzzyxml.XPath;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Plugin;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;
import org.seasar.dolteng.eclipse.util.ResourcesUtil;
import org.seasar.framework.util.InputStreamReaderUtil;
import org.seasar.framework.util.InputStreamUtil;
import org.seasar.framework.util.ReaderUtil;
import org.seasar.framework.util.URLUtil;

/**
 * @author taichi
 * 
 */
public class ProjectBuildConfigResolver {
    private static final Pattern txtextensions = Pattern.compile(
            "\\.(txt|java|dicon|properties|tomcatplugin|mf|x?html?)",
            Pattern.CASE_INSENSITIVE);

    private FuzzyXMLDocument projectConfig;

    public void initialize() {
        URL url = getTemplateResourceURL("projects.xml");
        InputStream in = null;
        try {
            FuzzyXMLParser parser = new FuzzyXMLParser();
            projectConfig = parser.parse(new BufferedInputStream(URLUtil
                    .openStream(url)));
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
        StringBuffer stb = new StringBuffer();
        stb.append("//project id=\"");
        stb.append(id);
        stb.append("\"/handler");

        FuzzyXMLNode[] nodes = XPath.selectNodes(projectConfig
                .getDocumentElement(), stb.toString());

        for (int i = 0; i < nodes.length; i++) {
            FuzzyXMLNode node = nodes[i];
            if (i == 0) {
                FuzzyXMLElement parent = (FuzzyXMLElement) node.getParentNode();
                if (parent.hasAttribute("extends")) {
                    String parentId = parent.getAttributeNode("extends")
                            .getValue();
                    resolve(parentId, builder); // TODO 無限ループ対策が必要

                }
            }
            if (node instanceof FuzzyXMLElement) {
                ResourceHandler handler = null;
                FuzzyXMLElement e = (FuzzyXMLElement) node;
                if (e.hasAttribute("type")) {

                } else {
                    handler = new DefaultHandler();
                }
            }
        }
    }

    private class Entry {
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

    private class DefaultHandler implements ResourceHandler {
        private Set entries = new HashSet();

        public int getNumberOfFiles() {
            return entries.size();
        }

        public String getType() {
            return "default";
        }

        public void handle(IProject project, IProgressMonitor monitor) {
            for (final Iterator i = entries.iterator(); i.hasNext();) {
                Entry e = (Entry) i.next();
                if ("path".equals(e.kind)) {
                    ResourcesUtil.createDir(project, e.path);
                }
                if ("file".equals(e.kind)) {
                    IFile f = project.getFile(e.path);
                    if (txtextensions.matcher(f.getFileExtension()).matches()) {
                        processTxt(f);
                    } else {
                        process(f);
                    }
                }
            }
            ProgressMonitorUtil.isCanceled(monitor, 1);
        }

        protected void processTxt(IFile file) {

        }

        protected void process(IFile file) {

        }

        public void merge(ResourceHandler handler) {
            DefaultHandler d = (DefaultHandler) handler;
            entries.addAll(d.entries);
        }
    }
}
