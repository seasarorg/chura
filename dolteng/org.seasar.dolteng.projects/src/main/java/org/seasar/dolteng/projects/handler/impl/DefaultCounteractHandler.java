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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.util.ResourcesUtil;
import org.seasar.dolteng.projects.ProjectBuilder;
import org.seasar.dolteng.projects.model.Entry;

/**
 * Defaultハンドラの効果を打ち消す。
 * 
 * @author daisuke
 */
@SuppressWarnings("serial")
public class DefaultCounteractHandler extends DefaultHandler {

    public DefaultCounteractHandler() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.template.DefaultHandler#getType()
     */
    @Override
    public String getType() {
        return "defaultCounteract";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.template.DefaultHandler#handle(org.seasar.dolteng.eclipse.template.ProjectBuilder,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected void handle(ProjectBuilder builder, Entry e) {
        if ("path".equals(e.getKind())) {
            if (ResourcesUtil.isDirEmpty(builder.getProjectHandle(), e
                    .getPath())) {
                ResourcesUtil
                        .removeDir(builder.getProjectHandle(), e.getPath());
            }
        } else if ("file".equals(e.getKind())) {
            process(builder, e);

            IPath path = new Path(e.getPath()).removeLastSegments(1);

            // 再帰的に消しちゃっても大丈夫かなぁ。。
            while (ResourcesUtil.isDirEmpty(builder.getProjectHandle(), path
                    .toString())) {
                ResourcesUtil.removeDir(builder.getProjectHandle(), path
                        .toString());
                path = path.removeLastSegments(1);
            }
        }
    }

    @Override
    protected void process(ProjectBuilder builder, Entry entry) {
        IPath removeTarget = new Path(entry.getPath());
        String jar = removeTarget.lastSegment();
        try {
            ResourcesUtil.removeFile(builder.getProjectHandle(), entry
                    .getPath());
            String srcJar = new StringBuffer(jar).insert(jar.lastIndexOf('.'),
                    "-sources").toString();
            IPath srcPath = removeTarget.removeLastSegments(1)
                    .append("sources").append(srcJar);
            try {
                ResourcesUtil.removeFile(builder.getProjectHandle(), srcPath
                        .toString());
                entry.attribute.remove("sourcepath");
            } catch (Exception e) {
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        }

    }

    protected boolean delete(ProjectBuilder builder, String target) {
        IFile f = builder.getProjectHandle().getFile(target);
        if (f.exists() == true) {
            try {
                f.delete(false, null);
            } catch (Exception e) {
                DoltengCore.log(e);
                return false;
            }
        }
        return true;
    }
}
