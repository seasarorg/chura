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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.marker.SqlMapper;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.operation.TypeHierarchyFieldProcessor.FieldHandler;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;
import org.seasar.dolteng.eclipse.util.ResourcesUtil;
import org.seasar.dolteng.eclipse.util.StatusUtil;

/**
 * @author taichi
 * 
 */
public class SqlMarkingJob extends WorkspaceJob {

    private ICompilationUnit unit;

    public SqlMarkingJob(ICompilationUnit unit) {
        super(Messages.bind(Messages.PROCESS_MAPPING, unit.getElementName()));
        setPriority(Job.SHORT);
        this.unit = unit;
        setRule(this.unit.getSchedulingRule());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
     */
    @Override
    public boolean belongsTo(Object family) {
        return family == ResourcesPlugin.FAMILY_AUTO_BUILD
                || family == ResourcesPlugin.FAMILY_MANUAL_BUILD;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.WorkspaceJob#runInWorkspace(org.eclipse.core.runtime.IProgressMonitor)
     */
    @SuppressWarnings("unchecked")
    @Override
    public IStatus runInWorkspace(final IProgressMonitor monitor) {
        try {
            if (unit.exists()) {
                final IResource resource = unit.getResource();
                if (!resource.exists()) {
                    return Status.OK_STATUS;
                }

                resource.deleteMarkers(Constants.ID_SQL_MAPPER, true,
                        IResource.DEPTH_ZERO);

                final DoltengPreferences pref = DoltengCore.getPreferences(unit
                        .getJavaProject());
                IType type = unit.findPrimaryType();
                if (type == null) {
                    return Status.OK_STATUS;
                }
                IMethod[] methods = type.getMethods();
                if (pref != null && methods != null && 0 < methods.length) {
                    monitor.beginTask(Messages.bind(Messages.PROCESS_MAPPING,
                            unit.getElementName()), methods.length + 3);

                    final Set<String> annon = collectSqlFileAnnon(this.unit);
                    ProgressMonitorUtil.isCanceled(monitor, 1);

                    final List<IFile> files = collectSqlFiles(this.unit);
                    ProgressMonitorUtil.isCanceled(monitor, 1);

                    ASTParser parser = ASTParser.newParser(AST.JLS3);
                    parser.setSource(this.unit);
                    ASTNode node = parser.createAST(new NullProgressMonitor());

                    node.accept(new ASTVisitor() {
                        @Override
                        public boolean visit(FieldDeclaration node) {
                            return false;
                        }

                        @Override
                        public boolean visit(MethodDeclaration method) {
                            try {
                                IType type = unit.findPrimaryType();
                                String methodName = method.getName()
                                        .getIdentifier();
                                String typeName = type.getElementName();
                                Pattern pattern = Pattern.compile(typeName
                                        + "_" + methodName + "_?.*\\.sql",
                                        Pattern.CASE_INSENSITIVE);
                                boolean found = false;
                                for (IFile sql : files) {
                                    found = pattern.matcher(sql.getName())
                                            .matches();
                                    if (found) {
                                        sql.deleteMarkers(
                                                Constants.ID_SQL_MAPPER, true,
                                                IResource.DEPTH_ZERO);

                                        IJavaElement e = unit
                                                .getElementAt(method
                                                        .getStartPosition());
                                        if (e != null
                                                && e.getElementType() == IJavaElement.METHOD
                                                && e
                                                        .getElementName()
                                                        .equals(
                                                                method
                                                                        .getName()
                                                                        .getIdentifier())) {
                                            IMarker marker = sql
                                                    .createMarker(Constants.ID_SQL_MAPPER);
                                            marker.setAttribute(
                                                    IMarker.CHAR_START, 0);
                                            marker.setAttribute(
                                                    IMarker.CHAR_END, 1);

                                            marker = resource
                                                    .createMarker(Constants.ID_SQL_MAPPER);
                                            Map m = new HashMap();
                                            m
                                                    .put(
                                                            IMarker.CHAR_START,
                                                            new Integer(
                                                                    method
                                                                            .getStartPosition()));
                                            m
                                                    .put(
                                                            IMarker.CHAR_END,
                                                            new Integer(
                                                                    method
                                                                            .getStartPosition()
                                                                            + method
                                                                                    .getLength()));
                                            m
                                                    .put(
                                                            Constants.MARKER_ATTR_MAPPING_SQL_PATH,
                                                            sql.getFullPath()
                                                                    .toString());
                                            marker.setAttributes(m);
                                        }
                                        break;
                                    }
                                }
                                if (found == false) {
                                    if (Constants.DAO_TYPE_S2DAO.equals(pref
                                            .getDaoType())
                                            && hasSqlFileAnnotation(method,
                                                    annon)) {
                                        Map m = new HashMap();
                                        m.put(IMarker.SEVERITY,
                                                IMarker.SEVERITY_ERROR);
                                        m.put(IMarker.CHAR_START, new Integer(
                                                method.getStartPosition()));
                                        m.put(IMarker.CHAR_END, new Integer(
                                                method.getStartPosition()
                                                        + method.getLength()));
                                        m.put(IMarker.MESSAGE,
                                                Messages.SQL_NOT_FOUND);
                                        IMarker marker = resource
                                                .createMarker(Constants.ID_SQL_ERROR);
                                        marker.setAttributes(m);
                                    }
                                }
                            } catch (CoreException e) {
                                DoltengCore.log(e);
                            } finally {
                                ProgressMonitorUtil.isCanceled(monitor, 1);
                            }
                            return false;
                        }
                    });
                    ProgressMonitorUtil.isCanceled(monitor, 1);
                }
            }
            return Status.OK_STATUS;
        } catch (OperationCanceledException e) {
            throw e;
        } catch (CoreException e) {
            // 対象リソースがリポジトリ上のファイルだった場合など、
            // ファイルシステムのリソースではなかった場合。
            DoltengCore.log(e);
            return Status.CANCEL_STATUS;
        } catch (Exception e) {
            DoltengCore.log(e);
            return StatusUtil.createError(DoltengCore.getDefault(), 1000, e);
        } finally {
            monitor.done();
        }
    }

    private List<IFile> collectSqlFiles(ICompilationUnit unit)
            throws CoreException {
        List<IFile> files = new ArrayList<IFile>();
        IJavaProject javap = unit.getJavaProject();
        IType type = unit.findPrimaryType();
        String pkgname = type.getPackageFragment().getElementName();
        IPackageFragmentRoot[] roots = javap.getPackageFragmentRoots();
        for (IPackageFragmentRoot root : roots) {
            if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
                IPackageFragment pf = root.getPackageFragment(pkgname);
                if (pf.exists()) {
                    Object[] objs = pf.getNonJavaResources();
                    for (int j = 0; objs != null && j < objs.length; j++) {
                        IFile file = ResourcesUtil.toFile(objs[j]);
                        if (file != null
                                && SqlMapper.matchSql.matcher(file.getName())
                                        .matches()) {
                            files.add(file);
                        }
                    }
                }
            }
        }
        return files;
    }

    private Set<String> collectSqlFileAnnon(ICompilationUnit unit) {
        final Pattern pattern = Pattern.compile(".*_SQL_FILE",
                Pattern.CASE_INSENSITIVE);
        final Set<String> result = new HashSet<String>();
        new TypeHierarchyFieldProcessor(unit.findPrimaryType(),
                new FieldHandler() {
                    public void begin() {
                    }

                    public void done() {
                    }

                    public void process(IField field) {
                        String name = field.getElementName();
                        if (pattern.matcher(name).matches()) {
                            result.add(name.substring(0, name.length()
                                    - "_SQL_FILE".length()));
                        }
                    }
                }).run(new NullProgressMonitor());
        return result;
    }

    private boolean hasSqlFileAnnotation(MethodDeclaration m, Set<String> names) {
        if (names.contains(m.getName().getIdentifier())) {
            return true;
        }

        final boolean[] result = { false };
        m.accept(new ASTVisitor() {
            @Override
            public boolean visit(MarkerAnnotation node) {
                if (result[0] == false) {
                    String fqn = node.getTypeName().getFullyQualifiedName();
                    result[0] = fqn.endsWith("SqlFile");
                }
                return super.visit(node);
            }
        });
        return result[0];
    }
}
