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

package org.seasar.dolteng.eclipse.nature;

import java.io.BufferedInputStream;
import java.net.URLClassLoader;
import java.util.Iterator;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.DoltengProject;
import org.seasar.dolteng.eclipse.exception.XMLStreamRuntimeException;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.dolteng.eclipse.preferences.impl.DoltengProjectPreferencesImpl;
import org.seasar.dolteng.eclipse.util.JavaProjectClassLoader;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.XMLStreamReaderUtil;
import org.seasar.framework.container.S2Container;
import org.seasar.framework.container.factory.S2ContainerFactory;
import org.seasar.framework.container.impl.S2ContainerImpl;

/**
 * @author taichi
 * 
 */
public class DoltengNature implements DoltengProject, IProjectNature {

	private static final IPath TOMCAT_PLUGIN_PREF = new Path(".tomcatplugin");

	private IProject project;

	private S2Container container;

	private DoltengProjectPreferences preference;

	public DoltengNature() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.seasar.dolteng.eclipse.DoltengProject#getContainer()
	 */
	public S2Container getContainer() {
		return this.container;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException {
		ProjectUtil.addNature(getProject(), Constants.ID_NATURE);
		init();
		loadfromOtherPlugin();
	}

	protected void loadfromOtherPlugin() {
		try {
			IFile file = getProject().getFile(TOMCAT_PLUGIN_PREF);
			if (file.exists()) {
				readFromTomcatPlugin(file);
				// TODO WTPからも取ってくる？
			}
		} catch (Exception e) {
			DoltengCore.log(e);
		}
	}

	protected void readFromTomcatPlugin(IFile file) throws CoreException {
		XMLStreamReader reader = null;
		try {
			XMLInputFactory factory = XMLInputFactory.newInstance();
			reader = factory.createXMLStreamReader(new BufferedInputStream(file
					.getContents()));
			while (reader.hasNext()) {
				if ("rootDir".equals(reader.getLocalName())) {
					this.preference.setWebContentsRoot(reader.getElementText());
					break;
				} else {
					reader.next();
				}
			}
		} catch (XMLStreamException e) {
			throw new XMLStreamRuntimeException(e);
		} finally {
			XMLStreamReaderUtil.close(reader);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {
		ProjectUtil.removeNature(getProject(), Constants.ID_NATURE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	public IProject getProject() {
		return this.project;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject project) {
		this.project = project;
		init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.seasar.dolteng.eclipse.DoltengProject#getProjectPreferences()
	 */
	public DoltengProjectPreferences getProjectPreferences() {
		return this.preference;
	}

	public synchronized void init() {
		try {
			preference = new DoltengProjectPreferencesImpl(getProject());
			initContainer();
		} catch (Exception e) {
			DoltengCore.log(e);
		}
	}

	public synchronized void destroy() {
		try {
			if (this.container != null) {
				this.container.destroy();
				this.container = null;
			}
		} catch (Exception e) {
			DoltengCore.log(e);
		}
	}

	public static DoltengNature getInstance(IProject project) {
		if (project != null) {
			try {
				IProjectNature nature = project.getNature(Constants.ID_NATURE);
				if (nature instanceof DoltengNature) {
					return (DoltengNature) nature;
				}
			} catch (CoreException e) {
				DoltengCore.log(e);
			}
		}
		return null;
	}

	protected void initContainer() throws CoreException {
		this.destroy();

		IJavaProject javap = JavaCore.create(getProject());
		IPath outloc = javap.getOutputLocation();
		URLClassLoader loader = new JavaProjectClassLoader(javap);

		this.container = new S2ContainerImpl();
		for (Iterator i = this.preference.getNecessaryDicons().iterator(); i
				.hasNext();) {
			String s = i.next().toString();
			IPath path = outloc.append(s);
			IFile f = getProject().getFile(path);
			if (f.exists()) {
				this.container.include(S2ContainerFactory.create(s, loader));
			}
		}
		this.container.init();
	}
}
