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

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.seasar.dolteng.eclipse.template.ProjectBuildConfigResolver.Entry;
import org.seasar.framework.util.FileOutputStreamUtil;

public class ClasspathHandler extends DefaultHandler {

    private Map kindMapping = new HashMap();

    private Map compareKinds = new HashMap();

    private PrintWriter xml;

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

    public void handle(ProjectBuilder builder, IProgressMonitor monitor) {
        IFile file = builder.getProjectHandle().getFile(".classpath");
        try {
            xml = new PrintWriter(new OutputStreamWriter(FileOutputStreamUtil
                    .create(file.getLocation().toFile())));
            xml.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            xml.println("<classpath>");
            super.handle(builder, monitor);
            Collections.sort(entries, new Comparator() {
                public int compare(Object left, Object right) {
                    Entry l = (Entry) left;
                    Entry r = (Entry) right;
                    int result = 0;
                    String lk = (String) compareKinds.get(l.kind);
                    String rk = (String) compareKinds.get(r.kind);
                    if (lk != null && rk != null) {
                        result = lk.compareTo(rk);
                    }
                    if (result == 0) {
                        result = l.path.compareTo(r.path);
                    }
                    return result;
                }
            });

            for (final Iterator i = entries.iterator(); i.hasNext();) {
                Entry e = (Entry) i.next();
                xml.print("    <classpathentry");
                xml.print(" kind=\"");
                xml.print(kindMapping.get(e.kind));
                if (e.attribute.containsKey("sourcepath")) {
                    xml.print("\" sourcepath=\"");
                    xml.print(e.attribute.get("sourcepath"));
                }
                if (e.attribute.containsKey("output")) {
                    xml.print("\" output=\"");
                    xml.print(e.attribute.get("output"));
                }
                xml.print("\" path=\"");
                xml.print(e.path);
                xml.println("\"/>");
            }
            xml.println("</classpath>");
            xml.flush();
        } finally {
            xml.close();
        }
    }

}