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

import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.seasar.dolteng.core.template.TemplateConfig;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.impl.AsModel;
import org.seasar.dolteng.eclipse.nls.Messages;

/**
 * @author taichi
 * 
 */
public class ASPageTemplateHandler extends AbstractTemplateHandler {

    private int templateCount = 0;

    public ASPageTemplateHandler(IFile mxml, IProgressMonitor monitor) {
        super(mxml.getProject(), monitor, new AsModel(createVariables(mxml)));
    }

    private static Map<String, String> createVariables(IFile mxml) {
        Map<String, String> var = new HashMap<String, String>();
        // TODO 未実装
        return var;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.template.TemplateHandler#begin()
     */
    public void begin() {
        monitor.beginTask(Messages.GENERATE_CODES, templateCount);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.template.TemplateHandler#getTemplateConfigs()
     */
    public TemplateConfig[] getTemplateConfigs() {
        URL url = DoltengCore.getDefault().getBundle().getEntry(
                "template/fm/flex2_page.xml");
        TemplateConfig[] loaded = TemplateConfig.loadConfigs(url);
        templateCount = loaded.length;
        return loaded;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.template.TemplateHandler#open(org.seasar.dolteng.core.template.TemplateConfig)
     */
    public OutputStream open(TemplateConfig config) {
        // TODO Auto-generated method stub
        return null;
    }

}
