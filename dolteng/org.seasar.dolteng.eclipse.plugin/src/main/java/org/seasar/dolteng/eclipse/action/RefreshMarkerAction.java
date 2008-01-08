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
package org.seasar.dolteng.eclipse.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.seasar.dolteng.eclipse.operation.DIMarkingJob;
import org.seasar.dolteng.eclipse.operation.PageMarkingJob;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.util.DoltengProjectUtil;
import org.seasar.framework.convention.NamingConvention;

/**
 * @author taichi
 * 
 */
public class RefreshMarkerAction extends AbstractWorkbenchWindowActionDelegate {

    /**
     * 
     */
    public RefreshMarkerAction() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.action.AbstractEditorActionDelegate#processJava(org.eclipse.core.resources.IProject,
     *      org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences,
     *      org.eclipse.jdt.core.IJavaElement)
     */
    @Override
    protected void processJava(IProject project, DoltengPreferences pref,
            IJavaElement element) throws Exception {
        if (element.getElementType() == IJavaElement.COMPILATION_UNIT) {
            ICompilationUnit unit = (ICompilationUnit) element;
            NamingConvention nc = pref.getNamingConvention();
            IType type = unit.findPrimaryType();
            String typeName = type.getFullyQualifiedName();
            if (pref.isUsePageMarker()
                    && (nc.isTargetClassName(typeName, nc.getPageSuffix()) || nc
                            .isTargetClassName(typeName, nc.getActionSuffix()))) {
                IFile file = DoltengProjectUtil.findHtmlByJava(project, pref,
                        unit);
                if (file != null) {
                    PageMarkingJob op = new PageMarkingJob(file);
                    op.schedule();
                }
            }
            if (pref.isUseDIMarker()) {
                DIMarkingJob diMarker = new DIMarkingJob(unit);
                diMarker.schedule();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.action.AbstractEditorActionDelegate#processResource(org.eclipse.core.resources.IProject,
     *      org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences,
     *      org.eclipse.core.resources.IResource)
     */
    @Override
    protected void processResource(IProject project, DoltengPreferences pref,
            IResource resource) throws Exception {
        if (resource instanceof IFile) {
            IFile f = (IFile) resource;
            if (DoltengProjectUtil.isInViewPkg(f)) {
                PageMarkingJob job = new PageMarkingJob(f);
                job.schedule();
            }
        }
    }

}
