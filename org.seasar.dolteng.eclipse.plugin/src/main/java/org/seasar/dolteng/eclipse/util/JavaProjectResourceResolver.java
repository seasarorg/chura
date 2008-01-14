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
package org.seasar.dolteng.eclipse.util;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.framework.container.factory.ResourceResolver;

/**
 * @author taichi
 * 
 */
public class JavaProjectResourceResolver implements ResourceResolver {

    private Set<IContainer> rootdir = new HashSet<IContainer>();

    public JavaProjectResourceResolver(IProject project) {
        this(JavaCore.create(project));
    }

    public JavaProjectResourceResolver(IJavaProject project) {
        process(project);
    }

    protected void process(IJavaProject project) {
        if (project == null) {
            return;
        }
        this.rootdir.add(project.getProject());
        if (project.exists() == false) {
            return;
        }
        try {
            IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();
            for (IPackageFragmentRoot root : roots) {
                if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
                    IResource r = root.getResource();
                    if (r.getType() == IResource.FOLDER) {
                        this.rootdir.add((IContainer) r);
                    }
                }
            }
        } catch (JavaModelException e) {
            DoltengCore.log(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.framework.container.factory.ResourceResolver#getInputStream(java.lang.String)
     */
    public InputStream getInputStream(String path) {
        for (IContainer c : this.rootdir) {
            IResource r = c.findMember(path);
            if (r != null && IResource.FILE == r.getType()) {
                IFile f = (IFile) r;
                try {
                    return f.getContents();
                } catch (CoreException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
        return null;
    }

}
