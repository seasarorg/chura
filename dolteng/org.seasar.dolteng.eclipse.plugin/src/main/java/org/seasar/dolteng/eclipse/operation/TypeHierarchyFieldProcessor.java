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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.framework.util.ArrayUtil;

/**
 * @author taichi
 * 
 */
public class TypeHierarchyFieldProcessor implements IRunnableWithProgress {

    private IType type;

    private FieldHandler handler;

    /**
     * 
     */
    public TypeHierarchyFieldProcessor(IType type, FieldHandler handler) {
        super();
        this.type = type;
        this.handler = handler;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(IProgressMonitor monitor) throws InvocationTargetException,
            InterruptedException {
        handler.begin();
        try {
            if (monitor == null) {
                monitor = new NullProgressMonitor();
            }
            ITypeHierarchy hierarchy = type.newTypeHierarchy(type
                    .getJavaProject(), monitor);
            IType[] superTypes = hierarchy.getAllSuperclasses(type);
            superTypes = (IType[]) ArrayUtil.add(superTypes, type);
            for (int i = 0; i < superTypes.length; i++) {
                IType superType = superTypes[i];
                if (superType.getPackageFragment().getElementName().startsWith(
                        "java")
                        || superType.exists() == false) {
                    continue;
                }
                IField[] fields = superType.getFields();
                for (int j = 0; j < fields.length; j++) {
                    handler.process(fields[j]);
                }
            }
            this.handler.done();
        } catch (Exception e) {
            DoltengCore.log(e);
        }
    }

    public interface FieldHandler {
        void begin();

        void process(IField field);

        void done();
    }

}
