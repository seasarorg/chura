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
package org.seasar.dolteng.eclipse.marker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.ast.ImportsStructure;
import org.seasar.dolteng.eclipse.nls.Images;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.operation.KuinaDaoErrorReportJob;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.util.JavaElementDeltaAcceptor;
import org.seasar.dolteng.eclipse.util.JavaElementUtil;
import org.seasar.dolteng.eclipse.util.TypeUtil;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class KuinaDaoErrorReporter implements IMarkerResolutionGenerator2,
        IElementChangedListener {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.core.IElementChangedListener#elementChanged(org.eclipse.jdt.core.ElementChangedEvent)
     */
    public void elementChanged(ElementChangedEvent event) {
        JavaElementDeltaAcceptor.accept(event.getDelta(),
                new JavaElementDeltaAcceptor.Visitor() {
                    @Override
                    protected boolean visit(IJavaProject project) {
                        boolean result = false;
                        DoltengPreferences pref = DoltengCore
                                .getPreferences(project);
                        if (pref != null) {
                            result = Constants.DAO_TYPE_KUINADAO.equals(pref
                                    .getDaoType());
                        }
                        return result;
                    }

                    @Override
                    protected boolean visit(ICompilationUnit unit) {
                        DoltengPreferences pref = DoltengCore
                                .getPreferences(unit.getJavaProject());
                        NamingConvention nc = pref.getNamingConvention();
                        IType type = unit.findPrimaryType();
                        if (type != null && nc != null) {
                            String fqn = type.getFullyQualifiedName();
                            if (nc.isTargetClassName(fqn, nc.getDaoSuffix())) {
                                KuinaDaoErrorReportJob job = new KuinaDaoErrorReportJob(
                                        unit);
                                job.schedule();
                            }
                        }
                        return false;
                    }
                });
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IMarkerResolutionGenerator2#hasResolutions(org.eclipse.core.resources.IMarker)
     */
    public boolean hasResolutions(IMarker marker) {
        try {
            return Constants.ID_KUINA_ERROR.equals(marker.getType());
        } catch (CoreException e) {
            DoltengCore.log(e);
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IMarkerResolutionGenerator#getResolutions(org.eclipse.core.resources.IMarker)
     */
    public IMarkerResolution[] getResolutions(IMarker marker) {
        try {
            String type = (String) marker
                    .getAttribute(Constants.MARKER_ATTR_ERROR_TYPE_KUINA);
            if (Constants.ERROR_TYPE_KUINA_NAME.equals(type)) {
                return createRenameResolutions(marker);
            } else if (Constants.ERROR_TYPE_KUINA_TYPE.equals(type)) {
                return new IMarkerResolution[] { new RetypeResolution(marker) };
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private IMarkerResolution[] createRenameResolutions(IMarker marker)
            throws CoreException {
        ICompilationUnit unit = JavaElementUtil.toCompilationUnit(marker
                .getResource());
        IType primary = unit.findPrimaryType();

        String paramName = (String) marker
                .getAttribute(Constants.MARKER_ATTR_PARAMETER_NAME);
        if (StringUtil.isEmpty(paramName)) {
            return null;
        }
        paramName = paramName.replaceAll("_", "");
        Integer pos = (Integer) marker.getAttribute(IMarker.CHAR_START);
        if (pos != null) {
            IJavaElement e = unit.getElementAt(pos.intValue());
            if (e != null && e.getElementType() == IJavaElement.METHOD) {
                IMethod m = (IMethod) e;
                String returnTypeName = TypeUtil.getResolvedTypeName(m
                        .getReturnType(), primary);
                IType returnType = unit.getJavaProject().findType(
                        returnTypeName);
                if (returnType != null && returnType.exists()) {
                    IField[] fields = returnType.getFields();
                    List resoluList = new ArrayList(fields.length);
                    for (IField field : fields) {
                        IMarkerResolution2 current = new RenameResolution(
                                marker, field.getElementName());
                        if (field.getElementName().equalsIgnoreCase(paramName)) {
                            return new IMarkerResolution[] { current };
                        }
                        resoluList.add(current);
                    }
                    return (IMarkerResolution[]) resoluList
                            .toArray(new IMarkerResolution[resoluList.size()]);
                }
            }
        }

        return null;
    }

    private abstract class AbstractResolution implements IMarkerResolution2 {
        public String getDescription() {
            return null;
        }

        public Image getImage() {
            return Images.RENAME;
        }

        public abstract TypeUtil.ModifyTypeHandler newModifier();

        public void run(IMarker marker) {
            IResource r = marker.getResource();
            ICompilationUnit unit = JavaCore
                    .createCompilationUnitFrom((IFile) r);
            try {
                unit.becomeWorkingCopy(null, null);
                ASTNode node = JavaElementUtil.parse(unit);
                if (node != null) {
                    TypeUtil.modifyType(unit, new NullProgressMonitor(),
                            newModifier());
                }
                WorkingCopyOwner owner = unit.getOwner();
                unit.reconcile(AST.JLS3, true, owner, null);
                unit.commitWorkingCopy(true, null);
            } catch (Exception e) {
                DoltengCore.log(e);
                try {
                    unit.discardWorkingCopy();
                } catch (Exception ee) {
                    DoltengCore.log(ee);
                }
            }
        }
    }

    private class RenameResolution extends AbstractResolution {
        String methodName = null;

        String paramName = null;

        String renameTo = null;

        public RenameResolution(IMarker marker, String renameTo)
                throws CoreException {
            this.renameTo = renameTo;
            methodName = (String) marker
                    .getAttribute(Constants.MARKER_ATTR_METHOD_NAME);
            paramName = (String) marker
                    .getAttribute(Constants.MARKER_ATTR_PARAMETER_NAME);
        }

        public String getLabel() {
            return Labels.bind(Labels.RENAME_TO, renameTo);
        }

        @Override
        public TypeUtil.ModifyTypeHandler newModifier() {
            return new TypeUtil.ModifyTypeHandler() {
                public void modify(ASTNode node, final ASTRewrite rewrite,
                        final ImportsStructure imports) {
                    node.accept(new ASTVisitor() {
                        @Override
                        public boolean visit(MethodDeclaration node) {
                            if (node.getName().getIdentifier().equals(
                                    methodName)) {
                                List args = node.parameters();
                                for (Iterator i = args.iterator(); i.hasNext();) {
                                    SingleVariableDeclaration arg = (SingleVariableDeclaration) i
                                            .next();
                                    if (arg.getName().getIdentifier().equals(
                                            paramName)) {
                                        SimpleName newone = node.getAST()
                                                .newSimpleName(renameTo);
                                        rewrite.replace(arg.getName(), newone,
                                                null);
                                    }
                                }
                            }
                            return false;
                        }

                    });
                }
            };
        }
    }

    private class RetypeResolution extends AbstractResolution {
        String methodName = null;

        String paramName = null;

        String fqname = null;

        public RetypeResolution(IMarker marker) throws CoreException {
            ICompilationUnit unit = JavaElementUtil.toCompilationUnit(marker
                    .getResource());
            IType primary = unit.findPrimaryType();
            methodName = (String) marker
                    .getAttribute(Constants.MARKER_ATTR_METHOD_NAME);
            paramName = (String) marker
                    .getAttribute(Constants.MARKER_ATTR_PARAMETER_NAME);
            Integer pos = (Integer) marker.getAttribute(IMarker.CHAR_START);
            if (pos != null) {
                IJavaElement e = unit.getElementAt(pos.intValue());
                if (e != null && e.getElementType() == IJavaElement.METHOD) {
                    IMethod m = (IMethod) e;
                    String returnTypeName = TypeUtil.getResolvedTypeName(m
                            .getReturnType(), primary);
                    IType returnType = unit.getJavaProject().findType(
                            returnTypeName);
                    if (returnType != null && returnType.exists()) {
                        String fn = (String) marker
                                .getAttribute(Constants.MARKER_ATTR_PARAMETER_NAME);
                        if (StringUtil.isEmpty(fn) == false) {
                            IField f = returnType.getField(fn);
                            if (f != null && f.exists()) {
                                fqname = TypeUtil.getResolvedTypeName(f
                                        .getTypeSignature(), primary);
                            }
                        }
                    }
                }
            }
        }

        public String getLabel() {
            return Labels.bind(Labels.RETYPE_TO, fqname);
        }

        @Override
        public TypeUtil.ModifyTypeHandler newModifier() {
            return new TypeUtil.ModifyTypeHandler() {
                public void modify(ASTNode node, final ASTRewrite rewrite,
                        final ImportsStructure imports) {
                    node.accept(new ASTVisitor() {
                        @Override
                        public boolean visit(MethodDeclaration node) {
                            if (node.getName().getIdentifier().equals(
                                    methodName)) {
                                List args = node.parameters();
                                for (Iterator i = args.iterator(); i.hasNext();) {
                                    SingleVariableDeclaration arg = (SingleVariableDeclaration) i
                                            .next();
                                    if (arg.getName().getIdentifier().equals(
                                            paramName)) {
                                        modifyType(arg.getType(), rewrite,
                                                imports.addImport(fqname));
                                    }
                                }
                            }
                            return false;
                        }

                        private void modifyType(Type t, ASTRewrite rewrite,
                                String changeTo) {
                            if (t instanceof ArrayType) {
                                ArrayType type = (ArrayType) t;
                                modifyType(type.getComponentType(), rewrite,
                                        changeTo);
                            } else if (t instanceof QualifiedType) {
                                QualifiedType type = (QualifiedType) t;
                                SimpleName newone = type.getAST()
                                        .newSimpleName(changeTo);
                                rewrite.replace(type.getName(), newone, null);
                            } else if (t instanceof SimpleType) {
                                SimpleType type = (SimpleType) t;
                                SimpleName newone = type.getAST()
                                        .newSimpleName(changeTo);
                                rewrite.replace(type.getName(), newone, null);
                            }
                        }
                    });
                }
            };
        }
    }

}
