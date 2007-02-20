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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.util.JDTDomUtil;
import org.seasar.dolteng.eclipse.util.JavaElementUtil;
import org.seasar.dolteng.eclipse.util.TypeUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class KuinaDaoErrorReportJob extends WorkspaceJob {

    private ICompilationUnit unit;

    public KuinaDaoErrorReportJob(ICompilationUnit target) {
        super(Messages.PROCESS_VALIDATE);
        this.unit = target;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.WorkspaceJob#runInWorkspace(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStatus runInWorkspace(IProgressMonitor monitor)
            throws CoreException {
        IResource r = unit.getResource();
        r.deleteMarkers(Constants.ID_KUINA_ERROR, false, 0);
        ASTNode node = JavaElementUtil.parse(r);
        if (node != null) {
            node.accept(new Visitor(unit.getJavaProject(), unit
                    .findPrimaryType()));
        }
        return Status.OK_STATUS;
    }

    private class Visitor extends ASTVisitor {
        IJavaProject project = null;

        IType primary = null;

        public Visitor(IJavaProject project, IType type) {
            this.project = project;
            this.primary = type;
        }

        public boolean visit(MethodDeclaration method) {
            Type t = method.getReturnType2();
            IType returnType = resolve(t);
            String methodName = method.getName().getIdentifier();
            List params = method.parameters();
            if (params.size() == 1) {
                SingleVariableDeclaration param = (SingleVariableDeclaration) params
                        .get(0);
                IType type = resolve(param.getType());
                if (type != null
                        && type.exists()
                        && returnType != null
                        && returnType.getFullyQualifiedName().equals(
                                type.getFullyQualifiedName())) {
                    return false;
                }
            }
            for (Iterator i = params.iterator(); i.hasNext();) {
                SingleVariableDeclaration param = (SingleVariableDeclaration) i
                        .next();
                process(param, returnType, methodName);
            }
            return false;
        }

        public IType resolve(Type t) {
            return JDTDomUtil.resolve(t, primary, project);
        }

        public void process(SingleVariableDeclaration param, IType returnType,
                String methodName) {
            try {
                if (returnType == null || StringUtil.isEmpty(methodName)) {
                    return;
                }
                String name = param.getName().getIdentifier();
                IField f = returnType.getField(name);
                if (f == null || f.exists() == false) {
                    String msg = Messages.bind(Messages.ILLEGAL_PARAMETER_NAME,
                            new String[] { name,
                                    returnType.getFullyQualifiedName() });
                    report(param.getName(), Constants.ERROR_TYPE_KUINA_NAME,
                            methodName, name, msg);
                } else {
                    Type t = param.getType();
                    String fieldType = TypeUtil.getResolvedTypeName(f
                            .getTypeSignature(), primary);
                    String paramType = "";
                    if (t.isPrimitiveType()) {
                        PrimitiveType pt = (PrimitiveType) t;
                        paramType = pt.getPrimitiveTypeCode().toString();
                    } else {
                        IType type = resolve(param.getType());
                        paramType = type.getFullyQualifiedName();
                    }
                    if (fieldType.equals(paramType) == false) {
                        String msg = Messages.bind(
                                Messages.ILLEGAL_PARAMETER_TYPE, name);
                        report(param.getType(),
                                Constants.ERROR_TYPE_KUINA_TYPE, methodName,
                                name, msg);
                    }
                }
            } catch (Exception e) {
                DoltengCore.log(e);
            }
        }

        @SuppressWarnings("unchecked")
        public void report(ASTNode node, String errorType, String methodName,
                String paramName, String msg) {
            try {
                Map m = new HashMap();
                m.put(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
                m.put(IMarker.CHAR_START, new Integer(node.getStartPosition()));
                m.put(IMarker.CHAR_END, new Integer(node.getStartPosition()
                        + node.getLength()));
                m.put(IMarker.MESSAGE, msg);
                m.put(Constants.MARKER_ATTR_ERROR_TYPE_KUINA, errorType);
                m.put(Constants.MARKER_ATTR_METHOD_NAME, methodName);
                m.put(Constants.MARKER_ATTR_PARAMETER_NAME, paramName);
                IResource r = unit.getResource();
                IMarker marker = r.createMarker(Constants.ID_KUINA_ERROR);
                marker.setAttributes(m);
            } catch (Exception e) {
                DoltengCore.log(e);
            }
        }
    }
}
