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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;
import org.seasar.dolteng.eclipse.util.ResourcesUtil;
import org.seasar.dolteng.eclipse.util.ScriptingUtil;
import org.seasar.dolteng.projects.ProjectBuilder;
import org.seasar.dolteng.projects.handler.ResourceHandler;
import org.seasar.dolteng.projects.model.Entry;
import org.seasar.framework.util.FileOutputStreamUtil;
import org.seasar.framework.util.InputStreamUtil;
import org.seasar.framework.util.URLUtil;
import org.w3c.dom.Document;

@SuppressWarnings("serial")
public class DefaultHandler implements ResourceHandler {
    protected Pattern txtExtensions = Pattern
            .compile(
                    ".*\\.(txt|java|dicon|properties|tomcatplugin|component|mf|x?html?|m?xml|prefs?|sql|jsp?)$",
                    Pattern.CASE_INSENSITIVE);

    protected ArrayList<Entry> entries = new ArrayList<Entry>();

    protected transient PrintWriter xml;

    protected String dtdPublic = null;

    protected String dtdSystem = null;

    public String getType() {
        return "default";
    }

    public int getNumberOfFiles() {
        return entries.size();
    }

    public void add(Entry entry) {
        if (entries.contains(entry) == false) {
            entries.add(entry);
        }
    }

    public void merge(ResourceHandler handler) {
        if (handler instanceof DefaultHandler) {
            DefaultHandler arh = (DefaultHandler) handler;
            for (Entry e : arh.entries) {
                add(e);
            }
        }
    }

    public void handle(ProjectBuilder builder, IProgressMonitor monitor) {
        for (Entry e : entries) {
            handle(builder, e);
            ProgressMonitorUtil.isCanceled(monitor, 1);
        }
    }

    protected void handle(ProjectBuilder builder, Entry e) {
        if ("path".equals(e.getKind())) {
            ResourcesUtil.createDir(builder.getProjectHandle(), e.getPath());
        } else if ("file".equals(e.getKind())) {
            ResourcesUtil.createDir(builder.getProjectHandle(), new Path(e
                    .getPath()).removeLastSegments(1).toString());
            if (txtExtensions.matcher(e.getPath()).matches()) {
                processTxt(builder, e);
            } else {
                process(builder, e);
            }
        }
    }

    protected void processTxt(ProjectBuilder builder, Entry entry) {
        URL url = builder.findResource(entry);
        if (url != null) {
            String txt = ResourcesUtil.getTemplateResourceTxt(url);
            txt = ScriptingUtil.resolveString(txt, builder.getConfigContext());
            txt = additionalProcessing(txt);
            IFile handle = builder.getProjectHandle().getFile(entry.getPath());
            InputStream src = null;
            try {
                byte[] bytes = txt.getBytes("UTF-8");
                if (handle.exists() == false) {
                    src = new ByteArrayInputStream(bytes);
                    handle.create(src, IResource.FORCE, null);
                }
            } catch (Exception e) {
                DoltengCore.log(e);
            } finally {
                InputStreamUtil.close(src);
            }
        } else {
            DoltengCore.log("missing txt " + entry.getPath());
        }
    }

    protected String additionalProcessing(String txt) {
        return txt;
    }

    protected void processBinary(ProjectBuilder builder, Entry entry) {
        if (copyBinary(builder, builder.findResource(entry), entry.getPath()) == false) {
            DoltengCore.log("missing binary " + entry.getPath());
        }
    }

    protected void process(ProjectBuilder builder, Entry entry) {
        IPath copyTo = new Path(entry.getPath());
        String jar = copyTo.lastSegment();

        entry.attribute.put("mavenResource", "jar");
        if (copyBinary(builder, entry)) {
            String srcJar = new StringBuffer(jar).insert(jar.lastIndexOf('.'),
                    "-sources").toString();
            IPath srcPath = copyTo.removeLastSegments(1).append("sources")
                    .append(srcJar);
            Entry srcEntry = new Entry(entry.getLoader());
            srcEntry.attribute.putAll(entry.attribute);
            srcEntry.attribute.put("path", srcPath.toString());
            srcEntry.attribute.put("mavenResource", "src");
            if (copyBinary(builder, srcEntry)) {
                entry.attribute.put("sourcepath", srcPath.toString());
            }
        } else {
            DoltengCore.log("missing jar " + jar);
        }
    }

    protected boolean copyBinary(ProjectBuilder builder, Entry entry) {
        URL url = builder.findResource(entry);
        if (url != null) {
            return copyBinary(builder, url, entry.getPath());
        }
        return false;
    }

    protected boolean copyBinary(ProjectBuilder builder, URL url, String dest) {
        InputStream in = null;
        try {
            IFile f = builder.getProjectHandle().getFile(dest);
            if (f.exists() == false) {
                ResourcesUtil.createDir(builder.getProjectHandle(), f
                        .getParent().getProjectRelativePath().toString());
                in = URLUtil.openStream(url);
                f.create(in, true, null);
            }
            return true;
        } catch (Exception e) {
            DoltengCore.log(e);
        } finally {
            InputStreamUtil.close(in);
        }
        return false;
    }

    protected void outputXML(ProjectBuilder builder, Document doc,
            IFile outputFile) {
        try {
            xml = new PrintWriter(new OutputStreamWriter(FileOutputStreamUtil
                    .create(outputFile.getLocation().toFile())));

            TransformerFactory transformerFactory = TransformerFactory
                    .newInstance();
            Transformer transformer = transformerFactory
                    .newTransformer(/* xslSource */);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer
                    .setOutputProperty(
                            org.apache.xml.serializer.OutputPropertiesFactory.S_KEY_INDENT_AMOUNT,
                            "4");
            if (dtdPublic != null) {
                transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC,
                        dtdPublic);
            }
            if (dtdSystem != null) {
                transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
                        dtdSystem);
            }
            transformer
                    .setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.transform(new DOMSource(doc), new StreamResult(xml));

            xml.flush();
        } catch (TransformerConfigurationException e) {
            DoltengCore.log(e);
        } catch (TransformerException e) {
            DoltengCore.log(e);
        } finally {
            if (xml != null) {
                xml.close();
            }
        }
    }

    @Override
    public String toString() {
        return getType() + " " + entries.toString();
    }
}