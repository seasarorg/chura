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

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;

/**
 * @author taichi
 * 
 */
public class JavaElementDeltaAcceptor {

    public static void accept(IJavaElementDelta delta, Visitor visitor) {
        accept(delta.getAffectedChildren(), visitor);
    }

    public static void accept(IJavaElementDelta[] deltas, Visitor visitor) {
        for (int i = 0; i < deltas.length; i++) {
            IJavaElementDelta delta = deltas[i];
            if (visitor.preVisit(delta) == false) {
                return;
            }
            IJavaElement e = delta.getElement();
            switch (e.getElementType()) {
            case IJavaElement.JAVA_PROJECT: {
                if (visitor.visit((IJavaProject) e) == false) {
                    return;
                }
            }
                break;
            case IJavaElement.PACKAGE_FRAGMENT: {
                if (visitor.visit((IPackageFragment) e) == false) {
                    return;
                }
            }
                break;
            case IJavaElement.COMPILATION_UNIT: {
                if (visitor.visit((ICompilationUnit) e) == false) {
                    return;
                }
            }
                break;
            case IJavaElement.CLASS_FILE: {
                if (visitor.visit((IClassFile) e) == false) {
                    return;
                }
            }
                break;
            default:
                break;
            }
            if (visitor.postVisit(delta) == false) {
                return;
            }
            accept(delta.getAffectedChildren(), visitor);
        }
    }

    public static abstract class Visitor {

        protected boolean preVisit(IJavaElementDelta delta) {
            return true;
        }

        protected boolean postVisit(IJavaElementDelta delta) {
            return true;
        }

        protected boolean visit(IJavaProject project) {
            return true;
        }

        protected boolean visit(IPackageFragmentRoot root) {
            return true;
        }

        protected boolean visit(IPackageFragment fragment) {
            return true;
        }

        protected boolean visit(ICompilationUnit unit) {
            return true;
        }

        protected boolean visit(IClassFile clazz) {
            return true;
        }
    }
}
