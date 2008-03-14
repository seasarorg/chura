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

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.seasar.dolteng.eclipse.DoltengCore;

/**
 * @author taichi
 * 
 */
public class TextFileBufferUtil {

    public static ITextFileBuffer acquire(ICompilationUnit cu) {
        return acquire(cu.getResource());
    }

    public static ITextFileBuffer acquire(IResource resource) {
        try {
            if (resource != null && resource.getType() == IResource.FILE) {
                final IPath path = resource.getFullPath();
                FileBuffers.getTextFileBufferManager().connect(path,
                        new NullProgressMonitor());
                return FileBuffers.getTextFileBufferManager()
                        .getTextFileBuffer(path);
            }
        } catch (CoreException e) {
            DoltengCore.log(e);
        }
        return null;
    }

    public static void release(ICompilationUnit cu) {
        release(cu.getResource());
    }

    public static void release(IResource resource) {
        try {
            if (resource != null && resource.getType() == IResource.FILE) {
                FileBuffers.getTextFileBufferManager().disconnect(
                        resource.getFullPath(), new NullProgressMonitor());
            }
        } catch (CoreException e) {
            DoltengCore.log(e);
        }
    }
}
