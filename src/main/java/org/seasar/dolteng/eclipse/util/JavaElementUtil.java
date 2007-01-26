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

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;

/**
 * @author taichi
 * 
 */
public class JavaElementUtil {

    public static ICompilationUnit toCompilationUnit(IJavaElement element) {
        if (element == null) {
            return null;
        }
        if (element.getElementType() == IJavaElement.COMPILATION_UNIT) {
            return (ICompilationUnit) element;
        }
        if (element instanceof IMember) {
            IMember m = (IMember) element;
            return m.getCompilationUnit();
        }
        return null;
    }
}
