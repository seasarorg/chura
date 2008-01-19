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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.projects.ProjectBuilder;
import org.seasar.dolteng.projects.model.Entry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * @author daisuke
 */
public class AppDiconHandler extends DefaultHandler {

    protected IFile appDiconFile;

    public AppDiconHandler() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.template.DefaultHandler#getType()
     */
    @Override
	public String getType() {
        return "appDicon";
    }

    @Override
	public void handle(ProjectBuilder builder, IProgressMonitor monitor) {
		appDiconFile = builder.getProjectHandle().getFile("src/main/resources/app.dicon");
		outputXML(builder, processDocument(), appDiconFile);
    }
    
    protected Document processDocument() {
    	Document document = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			document = db.parse(appDiconFile.getContents());
			Element root = document.getDocumentElement();
			for(Entry e : entries) {
				Element newInclude = document.createElement("include");
				newInclude.setAttribute("path", e.getPath());
				root.appendChild(document.createTextNode("\t"));
				root.appendChild(newInclude);
				root.appendChild(document.createTextNode("\n"));
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
		}
		
		return document;
    }
}
