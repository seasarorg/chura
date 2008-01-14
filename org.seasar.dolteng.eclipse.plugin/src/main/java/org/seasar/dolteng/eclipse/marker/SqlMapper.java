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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Images;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.operation.SqlMarkingJob;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.util.DoltengProjectUtil;
import org.seasar.dolteng.eclipse.util.JavaElementDeltaAcceptor;
import org.seasar.dolteng.eclipse.util.ResourcesUtil;
import org.seasar.dolteng.eclipse.util.TextEditorUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.dolteng.eclipse.wizard.NewSqlWizard;
import org.seasar.framework.util.InputStreamReaderUtil;
import org.seasar.framework.util.ReaderUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class SqlMapper implements IMarkerResolutionGenerator2,
        IResourceChangeListener, IElementChangedListener {

    public static Pattern matchSql = Pattern.compile(".*(Dao)_.*sql$",
            Pattern.CASE_INSENSITIVE);

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
     */
    public void resourceChanged(IResourceChangeEvent event) {
        try {
            IResourceDelta delta = event.getDelta();
            if(delta == null) {
                return;
            }
            delta.accept(new IResourceDeltaVisitor() {
                public boolean visit(IResourceDelta delta) throws CoreException {
                    IResource resource = delta.getResource();

                    switch (resource.getType()) {
                    case IResource.PROJECT: {
                        IProject p = (IProject) resource;
                        DoltengPreferences pref = DoltengCore.getPreferences(p);
                        return pref != null && pref.isUseSqlMarker();
                    }
                    case IResource.FILE: {
                        IFile f = (IFile) resource;
                        if (matchSql.matcher(resource.getName()).matches()
                                && (delta.getFlags() & IResourceDelta.CONTENT) != 0) {
                            IMethod m = DoltengProjectUtil.findMethodBySql(f);
                            if (m != null) {
                                SqlMarkingJob op = new SqlMarkingJob(m
                                        .getCompilationUnit());
                                op.schedule(10L);
                            }
                        }
                    }
                        break;
                    default:
                        break;
                    }
                    return true;
                }
            });
        } catch (CoreException e) {
            DoltengCore.log(e);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.core.IElementChangedListener#elementChanged(org.eclipse.jdt.core.ElementChangedEvent)
     */
    public void elementChanged(ElementChangedEvent event) {
        JavaElementDeltaAcceptor.accept(event.getDelta(),
                new JavaElementDeltaAcceptor.Visitor() {
                    @Override
                    protected boolean preVisit(IJavaElementDelta delta) {
                        // 無限イベント発生のみ回避
                        return delta.getFlags() != IJavaElementDelta.F_AST_AFFECTED;
                    }

                    @Override
                    protected boolean visit(IJavaProject project) {
                        boolean result = false;
                        DoltengPreferences pref = DoltengCore
                                .getPreferences(project);
                        if (pref != null) {
                            result = pref.isUseSqlMarker();
                        }
                        return result;
                    }

                    @Override
                    protected boolean visit(ICompilationUnit unit) {
                        SqlMarkingJob job = new SqlMarkingJob(unit);
                        job.schedule(10L);
                        return false;
                    }
                });
    }

    private static Set<String> IDs = new HashSet<String>();

    static {
        IDs.add(Constants.ID_SQL_MAPPER);
        IDs.add(Constants.ID_SQL_ERROR);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IMarkerResolutionGenerator2#hasResolutions(org.eclipse.core.resources.IMarker)
     */
    public boolean hasResolutions(IMarker marker) {
        try {
            return IDs.contains(marker.getType());
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
        IMarkerResolution[] result = null;
        try {
            String type = marker.getType();
            if (Constants.ID_SQL_MAPPER.equals(type)) {
                result = new IMarkerResolution[] { new SqlMapperResolution(
                        marker) };
            } else if (Constants.ID_SQL_ERROR.equals(type)) {
                result = new IMarkerResolution[] { new SqlErrorResolution(
                        marker) };
            }
        } catch (CoreException e) {
            DoltengCore.log(e);
        }
        return result;
    }

    private class SqlMapperResolution implements IMarkerResolution2 {

        IFile sql;

        IMethod method;

        String desc;

        SqlMapperResolution(IMarker marker) {
            try {
                Map m = marker.getAttributes();
                String path = (String) m
                        .get(Constants.MARKER_ATTR_MAPPING_SQL_PATH);
                if (StringUtil.isEmpty(path) == false) {
                    IProject project = marker.getResource().getProject();
                    this.sql = (IFile) project.getParent().findMember(path);
                    if (this.sql != null) {
                        this.desc = ReaderUtil.readText(InputStreamReaderUtil
                                .create(this.sql.getContents(), this.sql
                                        .getCharset()));
                    }
                }
                IResource r = marker.getResource();
                if (matchSql.matcher(r.getName()).matches()) {
                    this.method = DoltengProjectUtil.findMethodBySql(r);
                    if (this.method != null) {
                        this.desc = this.method.getSource();
                    }
                }
            } catch (CoreException e) {
                DoltengCore.log(e);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IMarkerResolution2#getDescription()
         */
        public String getDescription() {
            return desc;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IMarkerResolution2#getImage()
         */
        public Image getImage() {
            return Images.SYNCED;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IMarkerResolution#getLabel()
         */
        public String getLabel() {
            return Labels.bind(Labels.JUMP_TO, new String[] { sql.getName() });
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IMarkerResolution#run(org.eclipse.core.resources.IMarker)
         */
        public void run(IMarker marker) {
            try {
                if (sql != null) {
                    WorkbenchUtil.openResource(sql);
                } else if (this.method != null) {
                    TextEditorUtil.selectAndReveal(this.method);
                }
            } catch (CoreException e) {
                DoltengCore.log(e);
            }
        }
    }

    private class SqlErrorResolution implements IMarkerResolution2 {

        IMarker marker;

        SqlErrorResolution(IMarker marker) {
            this.marker = marker;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IMarkerResolution2#getDescription()
         */
        public String getDescription() {
            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IMarkerResolution2#getImage()
         */
        public Image getImage() {
            return Images.GENERATE_CODE.createImage();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IMarkerResolution#getLabel()
         */
        public String getLabel() {
            return Messages.CREATE_NEW_SQL;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IMarkerResolution#run(org.eclipse.core.resources.IMarker)
         */
        public void run(IMarker marker) {
            try {
                IResource r = marker.getResource();
                DoltengPreferences pref = DoltengCore.getPreferences(r
                        .getProject());

                IFile f = ResourcesUtil.toFile(r);
                if (pref != null && f != null) {
                    ICompilationUnit unit = JavaCore
                            .createCompilationUnitFrom(f);
                    IType type = unit.findPrimaryType();
                    String pkg = type.getPackageFragment().getElementName();
                    NewSqlWizard wiz = new NewSqlWizard();
                    wiz.setContainerFullPath(pref.getDefaultResourcePath()
                            .append(pkg.replace('.', '/')));

                    IJavaElement elem = unit.getElementAt(marker.getAttribute(
                            IMarker.CHAR_START, 0));
                    if (elem != null
                            && elem.getElementType() == IJavaElement.METHOD) {
                        IMethod m = (IMethod) elem;
                        wiz.setFileName(type.getElementName() + "_"
                                + m.getElementName() + ".sql");
                        WorkbenchUtil.startWizard(wiz);
                    }
                }
            } catch (Exception e) {
                DoltengCore.log(e);
            }
        }
    }
}
