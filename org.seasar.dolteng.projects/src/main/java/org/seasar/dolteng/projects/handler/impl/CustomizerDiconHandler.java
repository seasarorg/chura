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
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;
import org.seasar.dolteng.projects.ProjectBuilder;
import org.seasar.dolteng.projects.model.Entry;
import org.seasar.framework.util.URLUtil;

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
        
		// TODO: Velocityで、templateディレクトリを読みに行けないか？（現状のようにclasspathではなく）
        final Properties p = new Properties();
		p.setProperty("resource.loader", "class");
		p.setProperty("class.resource.loader.description", "Velocity Classpath Resource Loader");
		p.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
		p.setProperty("input.encoding", "UTF-8");
		p.setProperty("output.encoding", "UTF-8");
		p.setProperty("default.contextType", "text/html; charset=UTF8");
		
		InputStream src = null;
		BufferedReader in = null;
        try {
			Velocity.init(p);
			VelocityContext vc = new VelocityContext();
	        for (Entry e : entries) {
	        	URL valueFile = builder.findResource(e.getPath());
	        	in = new BufferedReader(new InputStreamReader(URLUtil.openStream(valueFile)));
	        	String line = null;
	        	StringBuilder sb = new StringBuilder();
	        	while((line = in.readLine()) != null) {
	        		sb.append(line).append("\r\n");
	        	}
	        	vc.put(e.attribute.get("output"), sb.toString());
	            ProgressMonitorUtil.isCanceled(monitor, 1);
	        }
	        
	        StringWriter sw = new StringWriter();
			final String templatePath =
				getClass().getPackage().getName().toString().replace(".", "/") + "/customizer.dicon";
	        Template template = Velocity.getTemplate(templatePath);
	        template.merge(vc, sw);
	        
	        String contents = sw.toString();
	        sw.flush();
	        
	        byte[] bytes = contents.getBytes("UTF-8");
	        src = new ByteArrayInputStream(bytes);

	        output.create(src, IResource.FORCE, null);
		} catch (Exception e) {
	        DoltengCore.log(e);
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) { /* ignore */ }
			}
			if(src != null) {
				try {
					src.close();
				} catch (IOException e) { /* ignore */ }
			}
		}
    }
}
