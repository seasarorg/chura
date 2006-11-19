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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.template.ProjectBuildConfigResolver.Entry;
import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;
import org.seasar.dolteng.eclipse.util.ResourcesUtil;
import org.seasar.dolteng.eclipse.util.ScriptingUtil;
import org.seasar.framework.util.InputStreamUtil;
import org.seasar.framework.util.URLUtil;

class DefaultHandler implements ResourceHandler {
    private static final Pattern txtextensions = Pattern
            .compile(
                    ".*\\.(txt|java|dicon|properties|tomcatplugin|mf|x?html?|xml|pref|sql)$",
                    Pattern.CASE_INSENSITIVE);

    protected Set gard = new HashSet();

    protected List entries = new ArrayList();

    public String getType() {
        return "default";
    }

    public int getNumberOfFiles() {
        return entries.size();
    }

    public void add(Entry entry) {
        if (gard.add(entry)) {
            entries.add(entry);
        }
    }

    public void merge(ResourceHandler handler) {
        DefaultHandler arh = (DefaultHandler) handler;
        this.entries.addAll(arh.entries);
    }

    public void handle(ProjectBuilder builder, IProgressMonitor monitor) {
        for (final Iterator i = entries.iterator(); i.hasNext();) {
            handle(builder, (Entry) i.next());
            ProgressMonitorUtil.isCanceled(monitor, 1);
        }
    }

    protected void handle(ProjectBuilder builder, Entry e) {
        if ("path".equals(e.kind)) {
            ResourcesUtil.createDir(builder.getProjectHandle(), e.path);
        } else if ("file".equals(e.kind)) {
            if (txtextensions.matcher(e.path).matches()) {
                processTxt(builder, e);
            } else {
                process(builder, e);
            }
        }
    }

    protected void processTxt(ProjectBuilder builder, Entry entry) {
        URL url = builder.findResource(entry.path);
        if (url != null) {
            String txt = ResourcesUtil.getTemplateResourceTxt(url);
            txt = ScriptingUtil.resolveString(txt, builder.getConfigContext());
            IFile handle = builder.getProjectHandle().getFile(entry.path);
            InputStream src = null;
            try {
                byte[] bytes = txt.getBytes("UTF-8");
                if (handle.exists()) {
                    handle.delete(true, null);
                }
                src = new ByteArrayInputStream(bytes);
                handle.create(src, IResource.FORCE, null);
            } catch (Exception e) {
                DoltengCore.log(e);
            } finally {
                InputStreamUtil.close(src);
            }
        } else {
            DoltengCore.log("missing ..." + entry.path);
        }
    }

    protected void process(ProjectBuilder builder, Entry entry) {
        IPath copyTo = new Path(entry.path);
        String jar = copyTo.lastSegment();
        if (copyJar(builder, entry.path, "jars/" + jar)) {
            String srcJar = new StringBuffer(jar).insert(jar.lastIndexOf('.'),
                    "-sources").toString();
            IPath srcPath = copyTo.removeLastSegments(1).append("sources")
                    .append(srcJar);
            if (copyJar(builder, srcPath.toString(), "jars/sources/" + srcJar)) {
                entry.attribute.put("sourcepath", srcPath.toString());
            }
        } else {
            DoltengCore.log("missing .." + jar);
        }
    }

    private boolean copyJar(ProjectBuilder builder, String path, String jar) {
        InputStream src = null;
        try {
            URL url = ResourcesUtil.getTemplateResourceURL(jar);
            if (url != null) {
                src = URLUtil.openStream(url);
                IFile f = builder.getProjectHandle().getFile(path);
                ResourcesUtil.createDir(builder.getProjectHandle(), f
                        .getParent().getProjectRelativePath().toString());
                f.create(src, true, null);
                return true;
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        } finally {
            InputStreamUtil.close(src);
        }
        return false;
    }
}