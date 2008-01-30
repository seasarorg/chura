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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.projects.ProjectBuilder;
import org.seasar.dolteng.projects.handler.impl.dicon.DiconBuilder;
import org.seasar.dolteng.projects.handler.impl.dicon.DiconModel;
import org.seasar.framework.exception.IORuntimeException;
import org.seasar.framework.util.InputStreamUtil;

/**
 * @author daisuke
 */
public class CustomizerDiconHandler extends DefaultHandler {

    protected IFile customizerDiconFile;

    public CustomizerDiconHandler() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.template.DefaultHandler#getType()
     */
    @Override
	public String getType() {
        return "customizerDicon";
    }

    @Override
	public void handle(ProjectBuilder builder, IProgressMonitor monitor) {
        monitor.setTaskName(Messages.bind(Messages.PROCESS, "customizer.dicon"));
        
        IFile output = builder.getProjectHandle().getFile(
        		builder.getConfigContext().get(Constants.CTX_MAIN_RESOURCE_PATH) + "/customizer.dicon");
        
		InputStream src = null;
		BufferedReader in = null;
        try {
	        DiconBuilder diconBuilder = new DiconBuilder(DiconModel.getInstance("customizer"));
	        src = new ByteArrayInputStream(diconBuilder.build().getBytes("UTF-8"));
	        output.create(src, IResource.FORCE, null);
		} catch (Exception e) {
	        DoltengCore.log(e);
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
					throw new IORuntimeException(e);
				}
			}
			InputStreamUtil.close(src);
		}
    }
}
