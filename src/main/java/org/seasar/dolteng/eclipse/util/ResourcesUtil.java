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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.seasar.dolteng.eclipse.DoltengCore;

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
}
