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
package org.seasar.dolteng.projects.handler.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.loader.impl.CompositeResourceLoader;
import org.seasar.dolteng.projects.ProjectBuilder;
import org.seasar.dolteng.projects.model.Entry;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

@SuppressWarnings("serial")
public class ClasspathHandler extends DefaultHandler {

    protected HashMap<String, String> kindMapping = new HashMap<String, String>();

    private HashMap<String, String> compareKinds = new HashMap<String, String>();

    protected transient IFile classpathFile;

    protected transient IFile pomFile;

    @Override
    public String getType() {
        return "classpath";
    }

    public ClasspathHandler() {
        super();
        kindMapping.put("con", "con");
        kindMapping.put("path", "src");
        kindMapping.put("output", "output");
        kindMapping.put("file", "lib");

        compareKinds.put("con", "2");
        compareKinds.put("path", "1");
        compareKinds.put("output", "0");
        compareKinds.put("file", "3");
    }

    @Override
    public void handle(ProjectBuilder builder, IProgressMonitor monitor) {
        super.handle(builder, monitor);

        pomFile = builder.getProjectHandle().getFile("pom.xml");
        classpathFile = builder.getProjectHandle().getFile(".classpath");

        if (!pomFile.exists()) {
            Entry entry = new Entry(new CompositeResourceLoader());
            entry.attribute.put("kind", "file");
            entry.attribute.put("path", "pom.xml");
            processTxt(builder, entry);
        }

        Collections.sort(entries, new Comparator<Entry>() {
            public int compare(Entry l, Entry r) {
                int result = 0;
                String lk = compareKinds.get(l.getKind());
                String rk = compareKinds.get(r.getKind());
                if (lk != null && rk != null) {
                    result = lk.compareTo(rk);
                }
                if (result == 0) {
                    result = l.getPath().compareTo(r.getPath());
                }
                return result;
            }
        });

        outputXML(builder, createPomDocument(), pomFile);
        outputXML(builder, createClasspathDocument(), classpathFile);
    }

    protected Document createPomDocument() {
        Document document = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            document = db.parse(pomFile.getContents());

            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();
            XPathExpression expression = xpath.compile("/project/dependencies");
            Node dependencies = (Node) expression.evaluate(document,
                    XPathConstants.NODE);

            for (Entry entry : entries) {
                String maven = entry.attribute.get("maven");
                if (maven != null) {
                    String[] data = maven.split("[ ]*,[ ]*");
                    if (data.length == 3) {
                        Element dependency = document
                                .createElement("dependency");
                        Element groupId = document.createElement("groupId");
                        groupId.appendChild(document.createTextNode(data[0]));
                        Element artifactId = document
                                .createElement("artifactId");
                        artifactId
                                .appendChild(document.createTextNode(data[1]));
                        Element version = document.createElement("version");
                        version.appendChild(document.createTextNode(data[2]));
                        dependency.appendChild(groupId);
                        dependency.appendChild(artifactId);
                        dependency.appendChild(version);
                        dependencies.appendChild(dependency);
                    } else {
                        DoltengCore.log("invalid maven attribute("
                                + data.length + "): " + maven);
                    }
                }
            }
        } catch (ParserConfigurationException e) {
            DoltengCore.log(e);
        } catch (SAXException e) {
            DoltengCore.log(e);
        } catch (IOException e) {
            DoltengCore.log(e);
        } catch (CoreException e) {
            DoltengCore.log(e);
        } catch (XPathExpressionException e) {
            DoltengCore.log(e);
        }
        return document;
    }

    protected Document createClasspathDocument() {
        Document document = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            DOMImplementation domImpl = db.getDOMImplementation();
            document = domImpl.createDocument("", "classpath", null);

            Element classpath = document.getDocumentElement();

            for (Entry entry : entries) {
                Element classpathentry = document
                        .createElement("classpathentry");
                classpathentry.setAttribute("kind", kindMapping.get(entry
                        .getKind()));
                if (entry.attribute.containsKey("sourcepath")) {
                    classpathentry.setAttribute("sourcepath", entry.attribute
                            .get("sourcepath"));
                }
                if (entry.attribute.containsKey("output")) {
                    classpathentry.setAttribute("output", entry.attribute
                            .get("output"));
                }
                classpathentry
                        .setAttribute("path", entry.attribute.get("path"));

                classpath.appendChild(classpathentry);
            }
        } catch (ParserConfigurationException e) {
            DoltengCore.log(e);
        }
        return document;
    }
}