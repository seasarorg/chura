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
import java.net.URL;

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
import org.seasar.dolteng.projects.ProjectBuilder;
import org.seasar.dolteng.projects.model.Entry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author daisuke
 */
public class CustomizerDiconHandler extends DefaultHandler {

    protected IFile customizerDiconFile;

    public CustomizerDiconHandler() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.template.DefaultHandler#getType()
     */
    @Override
	public String getType() {
        return "customizerDicon";
    }

    @Override
	public void handle(ProjectBuilder builder, IProgressMonitor monitor) {
		customizerDiconFile = builder.getProjectHandle().getFile("src/main/resources/customizer.dicon");
		outputXML(builder, processDocument(builder), customizerDiconFile);
    }
    
    protected Document processDocument(ProjectBuilder builder) {
    	Document document = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			document = db.parse(customizerDiconFile.getContents());
			
			XPathFactory xpf = XPathFactory.newInstance();
			XPath xpath = xpf.newXPath();
			
			for(Entry entry : entries) {
				XPathExpression expression = xpath.compile(
						"//components/component[@name=\"" + entry.attribute.get("output") + "\"]");
				NodeList result = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
				
				if (result.getLength() > 0) {
					Element targetElement = (Element) result.item(0);
					if("remove".equals(entry.getKind())) {
						document.removeChild(targetElement);
					} else {
						URL sourceFile = builder.findResource(entry.getPath());
						Document sourceDocument = db.parse(sourceFile.toString());
						Node contents = document.importNode(sourceDocument.getDocumentElement(), true);
						targetElement.appendChild(contents);
					}
				}
			}
			dtdPublic = "-//SEASAR//DTD S2Container 2.4//EN";
			dtdSystem = "http://www.seasar.org/dtd/components24.dtd";
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
