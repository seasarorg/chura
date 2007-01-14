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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;
import org.seasar.framework.convention.NamingConvention;

/**
 * @author taichi
 * 
 */
public class DIMarkingJob extends WorkspaceJob {

    private IResource resource;

    private DoltengProjectPreferences pref;

    /**
     * @param name
     */
    public DIMarkingJob(IResource resource, DoltengProjectPreferences pref) {
        super(Messages.bind(Messages.PROCESS_MAPPING, resource.getName()));
        setPriority(Job.SHORT);
        this.resource = resource;
        this.pref = pref;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.WorkspaceJob#runInWorkspace(org.eclipse.core.runtime.IProgressMonitor)
     */
    public IStatus runInWorkspace(IProgressMonitor monitor)
            throws CoreException {
        monitor = ProgressMonitorUtil.care(monitor);
        monitor.beginTask(Messages.bind(Messages.PROCESS_MAPPING, resource
                .getName()), 3);
        try {
            IJavaElement e = JavaCore.create(resource);
            final IJavaProject project = e.getJavaProject();
            if (e.getElementType() == IJavaElement.COMPILATION_UNIT) {
                ICompilationUnit unit = (ICompilationUnit) e;
                ASTParser parser = ASTParser.newParser(AST.JLS3);
                parser.setSource(unit);
                ASTNode node = parser.createAST(new NullProgressMonitor());

                ProgressMonitorUtil.isCanceled(monitor, 1);

                node.accept(new ASTVisitor() {
                    public void endVisit(FieldDeclaration node) {
                        String field = node.getType().resolveBinding()
                                .getQualifiedName();
                        NamingConvention nc = pref.getNamingConvention();
                        try {
                            boolean is = nc.isTargetClassName(field, nc
                                    .getDaoSuffix())
                                    || nc.isTargetClassName(field, nc
                                            .getDxoSuffix());
                            if (is == false) {
                                String name = nc
                                        .toImplementationClassName(field);
                                IType type = project.findType(name);
                                is = type != null && type.exists();
                            }
                            if (is) {
                                Map m = new HashMap();
                                m.put(IMarker.CHAR_START, new Integer(node
                                        .getStartPosition()));
                                m.put(IMarker.CHAR_END, new Integer(node
                                        .getStartPosition()
                                        + node.getLength()));
                                m.put(Constants.MARKER_ATTR_MAPPING_TYPE_NAME,
                                        field);
                                IMarker marker = resource
                                        .createMarker(Constants.ID_DI_MAPPER);
                                marker.setAttributes(m);
                            }
                        } catch (CoreException e) {
                            DoltengCore.log(e);
                        }
                    }
                });
                ProgressMonitorUtil.isCanceled(monitor, 1);
            }
        } finally {
            monitor.done();
        }

        return Status.OK_STATUS;
    }

}
