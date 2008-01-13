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
package org.seasar.dolteng.eclipse.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.seasar.dolteng.eclipse.DoltengCore;

public class ConventionChangeListener implements IResourceChangeListener {

    public void resourceChanged(IResourceChangeEvent event) {
        try {
            event.getDelta().accept(new IResourceDeltaVisitor() {
                public boolean visit(IResourceDelta delta) throws CoreException {
                    if (delta.getKind() == IResourceDelta.CHANGED) {
                        IResource resource = delta.getResource();
                        if (resource != null
                                && resource.getType() == IResource.FILE
                                && "convention.dicon"
                                        .equals(resource.getName())) {
                            IProject project = resource.getProject();
                            IJavaProject javap = JavaCore.create(project);
                            for (IClasspathEntry entry : javap.getResolvedClasspath(true)) {
                                IPath path = entry.getOutputLocation();
                                if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE
                                        && path != null
                                        && path.isPrefixOf(resource
                                                .getFullPath())) {
                                    DoltengPreferences pref = DoltengCore
                                            .getPreferences(project);
                                    if (pref != null) {
                                        pref.setUpValues();
                                    }
                                    return false;
                                }
                            }
                        }
                    }
                    return true;
                }
            });
        } catch (CoreException e) {
            DoltengCore.log(e);
        }
    }
}