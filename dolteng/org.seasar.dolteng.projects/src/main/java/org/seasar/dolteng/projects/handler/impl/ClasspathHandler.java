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

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.projects.ProjectBuilder;
import org.seasar.dolteng.projects.model.Entry;
import org.seasar.framework.util.FileOutputStreamUtil;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ClasspathHandler extends DefaultHandler {

    protected Map<String, String> kindMapping = new HashMap<String, String>();

    private Map<String, String> compareKinds = new HashMap<String, String>();

    protected PrintWriter xml;
    
    protected IFile classpathFile;

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
        
		classpathFile = builder.getProjectHandle().getFile(".classpath");
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
        
		outputXML(builder, createDocument());
    }
    
    protected Document createDocument() {
    	Document document = null;
    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			DOMImplementation domImpl = db.getDOMImplementation();
			document = domImpl.createDocument("", "classpath", null);
			
			Element classpath = document.getDocumentElement();
			
			for(Entry entry: entries) {
				Element classpathentry = document.createElement("classpathentry");
				classpathentry.setAttribute("kind", kindMapping.get(entry.getKind()));
				if (entry.attribute.containsKey("sourcepath")) {
					classpathentry.setAttribute("sourcepath", entry.attribute.get("sourcepath"));
				}
				if (entry.attribute.containsKey("output")) {
					classpathentry.setAttribute("output", entry.attribute.get("output"));
				}
				classpathentry.setAttribute("path", entry.attribute.get("path"));
				
				classpath.appendChild(classpathentry);
			}
		} catch (ParserConfigurationException e) {
            DoltengCore.log(e);
		}
    	return document;
    }

    protected void outputXML(ProjectBuilder builder, Document doc) {
        try {
            xml = new PrintWriter(new OutputStreamWriter(FileOutputStreamUtil
                    .create(classpathFile.getLocation().toFile())));

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer(/*xslSource*/);
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(javax.xml.transform.OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(org.apache.xml.serializer.OutputPropertiesFactory.S_KEY_INDENT_AMOUNT, "2");
            transformer.transform(new DOMSource(doc), new StreamResult(xml));
            
            xml.flush();
		} catch (TransformerConfigurationException e) {
            DoltengCore.log(e);
		} catch (TransformerException e) {
            DoltengCore.log(e);
		} finally {
        	if(xml != null) {
        		xml.close();
        	}
        }
    }
}