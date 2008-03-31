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

import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.projects.ProjectBuilder;

/**
 * @author daisuke
 * 
 */
@SuppressWarnings("serial")
public class WTPHandler extends DefaultHandler {

    public WTPHandler() {
        super();

        txtExtensions = Pattern
                .compile(
                        ".*\\.(txt|java|dicon|properties|tomcatplugin|component|mf|x?html?|m?xml|prefs?|sql|jsp?)$",
                        Pattern.CASE_INSENSITIVE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.template.DefaultHandler#getType()
     */
    @Override
    public String getType() {
        return "wtp";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.template.DefaultHandler#handle(org.seasar.dolteng.eclipse.template.ProjectBuilder,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void handle(ProjectBuilder builder, IProgressMonitor monitor) {
        try {
            super.handle(builder, monitor);
            monitor.setTaskName(Messages.bind(Messages.ADD_NATURE_OF, "WTP"));
            if (Platform.getBundle(Constants.ID_WTP_FACET_PLUGIN) != null) {
                ProjectUtil.addNature(builder.getProjectHandle(),
                        Constants.ID_WTP_FACET_NATURE);
                ProjectUtil.addBuilders(builder.getProjectHandle(),
                        new String[] { Constants.ID_WTP_FACET_BUILDER });
            }
            if (Platform.getBundle(Constants.ID_WTP_VALIDATION_PLUGIN) != null) {
                ProjectUtil.addBuilders(builder.getProjectHandle(),
                        new String[] { Constants.ID_WTP_VALIDATION_BUILDER });
            }
            if (Platform.getBundle(Constants.ID_WTP_MODULE_PLUGIN) != null) {
                ProjectUtil.addNature(builder.getProjectHandle(),
                        Constants.ID_WTP_MODULE_NATURE);
            }
            if (Platform.getBundle(Constants.ID_JEM_PLUGIN) != null) {
                ProjectUtil.addNature(builder.getProjectHandle(),
                        Constants.ID_JEM_NATURE);
            }
            ProgressMonitorUtil.isCanceled(monitor, 1);
        } catch (final CoreException e) {
            DoltengCore.log(e);
        }
    }

    @Override
    protected String additionalProcessing(String txt) {
        if(txt.contains("classpath.helper")) {
            return txt.replace("/", "\\:\\:");
        }
        return txt;
    }

}
