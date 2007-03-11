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
package org.seasar.dolteng.eclipse.util;

import java.io.Reader;
import java.net.URL;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.framework.util.InputStreamReaderUtil;
import org.seasar.framework.util.ReaderUtil;
import org.seasar.framework.util.URLUtil;

/**
 * @author taichi
 * 
 */
public class ResourcesUtil {

    public static IFile findFile(final String name, IContainer root) {
        final IFile[] file = new IFile[1];
        try {
            IResourceVisitor visitor = new IResourceVisitor() {
                public boolean visit(IResource resource) throws CoreException {
                    if (name.equalsIgnoreCase(resource.getName())
                            && resource instanceof IFile) {
                        file[0] = (IFile) resource;
                        return false;
                    }
                    return true;
                }
            };
            root.accept(visitor);
        } catch (CoreException e) {
            DoltengCore.log(e);
        }
        return file[0];
    }

    public static void createDir(IContainer container, String path) {
        try {
            IPath fullpath = new Path(path);
            if (container.exists(fullpath) == false) {
                String[] ary = fullpath.segments();
                StringBuffer stb = new StringBuffer();
                for (int i = 0; i < ary.length; i++) {
                    IPath p = new Path(stb.append(ary[i]).toString());
                    if (container.exists(p) == false) {
                        IFolder f = container.getFolder(p);
                        f.create(true, true, null);
                    }
                    stb.append('/');
                }
            }
        } catch (CoreException e) {
            DoltengCore.log(e);
        }
    }

    public static String getTemplateResourceTxt(URL url) {
        Reader reader = InputStreamReaderUtil.create(URLUtil.openStream(url),
                "UTF-8");
        return ReaderUtil.readText(reader);
    }

    public static IResource toResource(Object adaptable) {
        if (adaptable instanceof IResource) {
            return (IResource) adaptable;
        } else if (adaptable instanceof IAdaptable) {
            IAdaptable a = (IAdaptable) adaptable;
            return (IResource) a.getAdapter(IResource.class);
        }
        return null;
    }

    public static IFile toFile(Object adaptable) {
        if (adaptable instanceof IFile) {
            return (IFile) adaptable;
        } else if (adaptable instanceof IAdaptable) {
            IAdaptable a = (IAdaptable) adaptable;
            return (IFile) a.getAdapter(IFile.class);
        }
        return null;
    }
}
