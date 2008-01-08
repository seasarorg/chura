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
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.seasar.dolteng.projects.ProjectBuilder;
import org.seasar.dolteng.projects.model.Entry;
import org.seasar.framework.util.FileOutputStreamUtil;

public class ClasspathHandler extends DefaultHandler {

    private Map<String, String> kindMapping = new HashMap<String, String>();

    private Map<String, String> compareKinds = new HashMap<String, String>();

    private PrintWriter xml;

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
        IFile file = builder.getProjectHandle().getFile(".classpath");
        try {
            xml = new PrintWriter(new OutputStreamWriter(FileOutputStreamUtil
                    .create(file.getLocation().toFile())));
            xml.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            xml.println("<classpath>");
            super.handle(builder, monitor);
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

            for (final Iterator i = entries.iterator(); i.hasNext();) {
                Entry e = (Entry) i.next();
                xml.print("    <classpathentry");
                xml.print(" kind=\"");
                xml.print(kindMapping.get(e.getKind()));
                if (e.attribute.containsKey("sourcepath")) {
                    xml.print("\" sourcepath=\"");
                    xml.print(e.attribute.get("sourcepath"));
                }
                if (e.attribute.containsKey("output")) {
                    xml.print("\" output=\"");
                    xml.print(e.attribute.get("output"));
                }
                xml.print("\" path=\"");
                xml.print(e.getPath());
                xml.println("\"/>");
            }
            xml.println("</classpath>");
            xml.flush();
        } finally {
            xml.close();
        }
    }

}