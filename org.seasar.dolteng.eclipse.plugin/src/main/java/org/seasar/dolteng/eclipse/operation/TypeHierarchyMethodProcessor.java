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
package org.seasar.dolteng.eclipse.operation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.framework.util.ArrayUtil;

/**
 * @author taichi
 * 
 */
public class TypeHierarchyMethodProcessor implements IRunnableWithProgress {

    private IType type;

    private MethodHandler handler;

    public TypeHierarchyMethodProcessor(IType type, MethodHandler handler) {
        super();
        this.type = type;
        this.handler = handler;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(IProgressMonitor monitor) {
        handler.begin();
        try {
            if (monitor == null) {
                monitor = new NullProgressMonitor();
            }
            ITypeHierarchy hierarchy = type.newTypeHierarchy(type
                    .getJavaProject(), monitor);
            IType[] superTypes = hierarchy.getAllSuperclasses(type);
            superTypes = (IType[]) ArrayUtil.add(superTypes, type);
            for (IType superType : superTypes) {
                if (superType.getPackageFragment().getElementName().startsWith(
                        "java")
                        || superType.exists() == false) {
                    continue;
                }
                IMethod[] methods = superType.getMethods();
                for (IMethod method : methods) {
                    handler.process(method);
                }
            }
            this.handler.done();
        } catch (Exception e) {
            DoltengCore.log(e);
        }
    }

    public interface MethodHandler {
        void begin();

        void process(IMethod method);

        void done();
    }

}
