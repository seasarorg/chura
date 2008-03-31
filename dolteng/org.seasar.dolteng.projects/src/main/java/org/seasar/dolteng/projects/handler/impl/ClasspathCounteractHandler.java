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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.projects.model.Entry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * ClasspathHandlerの効果を打ち消す。
 * 
 * @author daisuke
 */
@SuppressWarnings("serial")
public class ClasspathCounteractHandler extends ClasspathHandler {

    public ClasspathCounteractHandler() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.template.DefaultHandler#getType()
     */
    @Override
    public String getType() {
        return "classpathCounteract";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.template.DefaultHandler#handle(org.seasar.dolteng.eclipse.template.ProjectBuilder,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected Document createClasspathDocument() {
        Document document = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            document = db.parse(classpathFile.getContents());
            Element root = document.getDocumentElement();
            NodeList nl = root.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                if ("classpathentry".equals(node.getNodeName())) {
                    NamedNodeMap attr = node.getAttributes();
                    String kind = attr.getNamedItem("kind").getNodeValue();
                    String path = attr.getNamedItem("path").getNodeValue();
                    for (Entry e : entries) {
                        if (kindMapping.get(e.getKind()).equals(kind)
                                && e.getPath().equals(path)) {
                            root.removeChild(node);
                        }
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
        }

        return document;
    }
}
