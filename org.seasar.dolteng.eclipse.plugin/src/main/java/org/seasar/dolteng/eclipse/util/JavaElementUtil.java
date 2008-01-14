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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;

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

    public static ICompilationUnit toCompilationUnit(IResource r) {
        if (r == null || r.isAccessible() == false) {
            return null;
        }
        IFile f = ResourcesUtil.toFile(r);
        if (f == null) {
            return null;
        }
        return JavaCore.createCompilationUnitFrom(f);
    }

    public static ASTNode parse(IResource r) {
        if (r.getType() != IResource.FILE) {
            return null;
        }
        IFile file = (IFile) r;
        ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
        return parse(unit);
    }

    public static ASTNode parse(ICompilationUnit unit) {
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setCompilerOptions(unit.getJavaProject().getOptions(true));
        parser.setSource(unit);
        return parser.createAST(null);
    }
}
