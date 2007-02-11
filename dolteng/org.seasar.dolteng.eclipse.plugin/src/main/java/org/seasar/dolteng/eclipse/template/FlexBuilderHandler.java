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

import java.util.regex.Pattern;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.template.ProjectBuildConfigResolver.Entry;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.ResourcesUtil;

/**
 * @author taichi
 * 
 */
public class FlexBuilderHandler extends DefaultHandler {

    private static final Pattern txtextensions = Pattern.compile(
            ".*\\.(flex|actionScript)Properties$", Pattern.CASE_INSENSITIVE);

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.template.DefaultHandler#getType()
     */
    @Override
    public String getType() {
        return "flexbuilder";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.template.DefaultHandler#handle(org.seasar.dolteng.eclipse.template.ProjectBuilder,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void handle(ProjectBuilder builder, IProgressMonitor monitor) {
        try {
            monitor.setTaskName(Messages.bind(Messages.ADD_NATURE_OF,
                    "FlexBuilder"));
            if (Platform.getBundle(Constants.ID_FLEX_BUILDER_PLUGIN) != null) {
                ProjectUtil.addNature(builder.getProjectHandle(),
                        Constants.ID_FLEX_BUILDER_FLEXNATURE);
                ProjectUtil.addNature(builder.getProjectHandle(),
                        Constants.ID_FLEX_BUILDER_ACTIONSCRIPTNATURE);
                IWorkspaceRoot root = ProjectUtil.getWorkspaceRoot();
                IPath p = root.getLocation();
                // Flex Builderが、.flexPropertiesの中身のパスを「/」だと適切に扱う事が出来ない為
                builder.getConfigContext().put("workspacelocation",
                        p.toOSString());
                builder.getConfigContext().put("FRAMEWORKS", "${FRAMEWORKS}");
                builder.getConfigContext().put("DOCUMENTS", "${DOCUMENTS}");

                super.handle(builder, monitor);
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        }
    }

    protected void handle(ProjectBuilder builder, Entry e) {
        if ("path".equals(e.kind)) {
            ResourcesUtil.createDir(builder.getProjectHandle(), e.path);
        } else if ("file".equals(e.kind)) {
            ResourcesUtil.createDir(builder.getProjectHandle(),
                    new Path(e.path).removeLastSegments(1).toString());
            if (txtextensions.matcher(e.path).matches()) {
                processTxt(builder, e);
            } else {
                processBinary(builder, e);
            }
        }
    }

}
