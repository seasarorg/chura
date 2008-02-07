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

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.seasar.dolteng.eclipse.DoltengCore;

/**
 * @author taichi
 * 
 */
public class JDTDomUtil {

    public static IType resolve(Type t, IType primary, IJavaProject project) {
        IType result = null;
        try {
            if (t == null) {
            } else if (t.isArrayType()) {
                ArrayType type = (ArrayType) t;
                result = resolve(type.getComponentType(), primary, project);
            } else if (t.isParameterizedType()) {
                ParameterizedType type = (ParameterizedType) t;
                List args = type.typeArguments();
                if (args.size() == 1) {
                    result = resolve((Type) args.get(0), primary, project);
                }
            } else if (t.isQualifiedType()) {
                QualifiedType type = (QualifiedType) t;
                String qn = type.getName().getIdentifier();
                result = project.findType(qn);
            } else if (t.isSimpleType()) {
                SimpleType type = (SimpleType) t;
                String qn = type.getName().getFullyQualifiedName();
                qn = TypeUtil.resolveType(qn, primary);
                result = project.findType(qn);
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        }
        return result;
    }
}
