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
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;
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
    
    /** app.diconの中でincludeする優先順位 */
	protected Map<String, Integer> priority = new HashMap<String, Integer>();

    public AppDiconHandler() {
        super();
        
        priority.put("convention.dicon", 100);
        priority.put("aop.dicon", 200);
        priority.put("app_aop.dicon", 300);
        priority.put("teedaExtension.dicon", 400);
        priority.put("dao.dicon", 500);
        priority.put("kuina-dao.dicon", 600);
        priority.put("dxo.dicon", 700);
        priority.put("javaee5.dicon", 800);
        priority.put("jms.dicon", 900);
        priority.put("remoting_amf3.dicon", 1000);
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
        monitor.setTaskName(Messages.bind(Messages.PROCESS, "app.dicon"));
        
		appDiconFile = builder.getProjectHandle().getFile(
				builder.getConfigContext().get(Constants.CTX_MAIN_RESOURCE_PATH) + "/app.dicon");
		outputXML(builder, processDocument(monitor), appDiconFile);
		
        ProgressMonitorUtil.isCanceled(monitor, 1);
    }
    
    protected Document processDocument(IProgressMonitor monitor) {
    	Document document = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			document = db.parse(appDiconFile.getContents());
			Element root = document.getDocumentElement();
			
			Collections.sort(entries, new Comparator<Entry>() {
				public int compare(Entry o1, Entry o2) {
					return priority.get(o1.getPath()) - priority.get(o2.getPath());
				}
			});
			
			for(Entry e : entries) {
				Element newInclude = document.createElement("include");
				newInclude.setAttribute("path", e.getPath());
				root.appendChild(document.createTextNode("\t"));
				root.appendChild(newInclude);
				root.appendChild(document.createTextNode("\n"));
				
	            ProgressMonitorUtil.isCanceled(monitor, 1);
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
