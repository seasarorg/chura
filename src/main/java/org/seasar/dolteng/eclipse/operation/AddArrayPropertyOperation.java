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
package org.seasar.dolteng.eclipse.operation;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;

/**
 * @author taichi
 * 
 */
public class AddArrayPropertyOperation extends AddPropertyOperation {

    /**
     * @param unit
     * @param fieldType
     * @param fieldName
     */
    public AddArrayPropertyOperation(ICompilationUnit unit, IType fieldType,
            String fieldName) {
        super(unit, fieldType, fieldName);
    }

    /**
     * @param unit
     * @param fieldType
     */
    public AddArrayPropertyOperation(ICompilationUnit unit, IType fieldType) {
        super(unit, fieldType);
    }

    /**
     * @param unit
     * @param typeFQName
     * @param fieldName
     */
    public AddArrayPropertyOperation(ICompilationUnit unit, String typeFQName,
            String fieldName) {
        super(unit, typeFQName, fieldName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.operation.AddPropertyOperation#calculateFieldType(org.eclipse.jdt.core.IType)
     */
    protected String calculateFieldType(String typeName) {
        return typeName + "[]";
    }

}
