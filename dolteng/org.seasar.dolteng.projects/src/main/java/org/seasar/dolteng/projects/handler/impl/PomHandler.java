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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

@SuppressWarnings("serial")
public class PomHandler extends DefaultHandler {

    protected HashMap<String, String> kindMapping = new HashMap<String, String>();

    protected transient IFile pomFile;

    @Override
    public String getType() {
        return "pom";
    }

    @Override
    public void handle(ProjectBuilder builder, IProgressMonitor monitor) {
        pomFile = builder.getProjectHandle().getFile("pom.xml");

        if (!pomFile.exists()) {
            Entry entry = new Entry(new CompositeResourceLoader());
            entry.attribute.put("kind", "file");
            entry.attribute.put("path", "pom.xml");
            processTxt(builder, entry);
        }
        outputXML(builder, createDocument(), pomFile);
    }

    protected Document createDocument() {
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
                // TODO 未完成
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
}