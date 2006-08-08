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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.DoltengProject;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.dolteng.eclipse.preferences.impl.DoltengProjectPreferencesImpl;

/**
 * @author taichi
 * 
 */
public class DoltengNature implements DoltengProject, IProjectNature {

    private IProject project;

    private DoltengProjectPreferences preference;

    public DoltengNature() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.IProjectNature#configure()
     */
    public void configure() throws CoreException {
        init();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.IProjectNature#deconfigure()
     */
    public void deconfigure() throws CoreException {
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
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.DoltengProject#getProjectPreferences()
     */
    public synchronized DoltengProjectPreferences getProjectPreferences() {
        if (this.preference == null) {
            init();
        }
        return this.preference;
    }

    public void init() {
        try {
            preference = new DoltengProjectPreferencesImpl(getProject());
        } catch (Exception e) {
            DoltengCore.log(e);
        }
    }

    public synchronized void destroy() {
        try {
            this.preference.getRawPreferences().save();
        } catch (Exception e) {
            DoltengCore.log(e);
        }
    }

    public static DoltengNature getInstance(IProject project) {
        if (project != null && project.isOpen()) {
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

}
