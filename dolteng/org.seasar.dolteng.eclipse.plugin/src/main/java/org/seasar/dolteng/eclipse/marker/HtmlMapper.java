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
package org.seasar.dolteng.eclipse.marker;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.IDE;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Images;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.operation.PageMarkingJob;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.dolteng.eclipse.util.DoltengProjectUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class HtmlMapper implements IMarkerResolutionGenerator2,
        IElementChangedListener {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.core.IElementChangedListener#elementChanged(org.eclipse.jdt.core.ElementChangedEvent)
     */
    public void elementChanged(ElementChangedEvent event) {
        try {
            IJavaElementDelta[] children = event.getDelta()
                    .getAffectedChildren();
            for (int i = 0; children != null && i < children.length; i++) {
                IResourceDelta[] ary = children[i].getResourceDeltas();
                for (int j = 0; ary != null && j < ary.length; j++) {
                    ary[j].accept(new IResourceDeltaVisitor() {
                        public boolean visit(IResourceDelta delta)
                                throws CoreException {
                            IResource r = delta.getResource();
                            DoltengProjectPreferences pref = DoltengCore
                                    .getPreferences(r.getProject());
                            if (pref != null && pref.isUsePageMarker()
                                    && r.getType() == IResource.FILE
                                    && "java".equals(r.getFileExtension())) {
                                if (delta.getKind() == IResourceDelta.REMOVED) {
                                    removeHtmlMarker(r, pref);
                                } else {
                                    tryMarking(r, pref);
                                }
                            }

                            return true;
                        }
                    });
                }
            }
        } catch (CoreException e) {
            DoltengCore.log(e);
        }
    }

    private static void removeHtmlMarker(IResource r,
            DoltengProjectPreferences pref) throws CoreException {
        String maybePkg = r.getFullPath().removeFileExtension().toString()
                .replace('/', '.');
        NamingConvention nc = pref.getNamingConvention();
        if (maybePkg.endsWith(nc.getPageSuffix())) {
            maybePkg = maybePkg.substring(0, maybePkg.lastIndexOf(nc
                    .getPageSuffix()));
        } else if (maybePkg.endsWith(nc.getActionSuffix())) {
            maybePkg = maybePkg.substring(0, maybePkg.lastIndexOf(nc
                    .getActionSuffix()));
        } else {
            return;
        }

        String[] pkgNames = nc.getRootPackageNames();
        for (int i = 0; i < pkgNames.length; i++) {
            int index = maybePkg.indexOf(pkgNames[i]);
            if (-1 < index) {
                index = index + pkgNames[i].length()
                        + nc.getSubApplicationRootPackageName().length() + 1;
                String underView = nc.getViewRootPath()
                        + maybePkg.substring(index).replace('.', '/');
                IPath p = new Path(underView);
                IPath web = new Path(pref.getWebContentsRoot());
                web = web.append(p.removeLastSegments(1));
                web = web.append(StringUtil.decapitalize(p.lastSegment()
                        + nc.getViewExtension()));
                IResource html = r.getProject().findMember(web);
                if (html != null && html.exists()
                        && html.getType() == IResource.FILE) {
                    PageMarkingJob op = new PageMarkingJob((IFile) html);
                    op.schedule(10L);
                }
            }
        }
    }

    private static void tryMarking(IResource r, DoltengProjectPreferences pref) {
        NamingConvention nc = pref.getNamingConvention();
        IJavaElement element = JavaCore.create(r);
        if (r.getType() == IResource.FILE && element != null
                && element.exists()
                && element.getElementType() == IJavaElement.COMPILATION_UNIT) {
            ICompilationUnit unit = (ICompilationUnit) element;
            IType type = unit.findPrimaryType();
            if (type != null
                    && (nc.isTargetClassName(type.getElementName(), nc
                            .getPageSuffix()) || nc.isTargetClassName(type
                            .getElementName(), nc.getActionSuffix()))) {
                IFile file = DoltengProjectUtil.findHtmlByJava(r.getProject(),
                        pref, unit);
                if (file != null) {
                    PageMarkingJob op = new PageMarkingJob(file);
                    op.schedule(10L);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IMarkerResolutionGenerator2#hasResolutions(org.eclipse.core.resources.IMarker)
     */
    public boolean hasResolutions(IMarker marker) {
        try {
            return Constants.ID_HTML_MAPPER.equals(marker.getType());
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
        return new IMarkerResolution2[] { new HtmlMappingResolution(marker) };
    }

    private class HtmlMappingResolution implements IMarkerResolution2 {
        private IFile html;

        private String id;

        private String element;

        public HtmlMappingResolution(IMarker marker) {
            try {
                Map m = marker.getAttributes();
                this.id = (String) m.get(Constants.MARKER_ATTR_MAPPING_HTML_ID);
                String path = (String) m
                        .get(Constants.MARKER_ATTR_MAPPING_HTML_PATH);
                if (StringUtil.isEmpty(path) == false) {
                    IProject project = marker.getResource().getProject();
                    this.html = (IFile) project.getParent().findMember(path);
                }
                this.element = (String) m
                        .get(Constants.MARKER_ATTR_MAPPING_ELEMENT);
            } catch (CoreException e) {
                DoltengCore.log(e);
            }
        }

        public String getLabel() {
            return Labels.bind(Labels.JUMP_TO_HTML, html.getName());
        }

        public void run(IMarker marker) {
            try {
                IMarker[] markers = html.findMarkers(Constants.ID_PAGE_MAPPER,
                        false, IResource.DEPTH_ZERO);
                if (this.id == null) {
                    return;
                }
                IWorkbenchWindow window = WorkbenchUtil.getWorkbenchWindow();
                if (window == null) {
                    return;
                }
                final IWorkbenchPage activePage = window.getActivePage();
                for (int i = 0; markers != null && i < markers.length; i++) {
                    String n = markers[i].getAttribute(
                            Constants.MARKER_ATTR_MAPPING_FIELD_NAME, "");
                    if (this.id.equalsIgnoreCase(n)) {
                        IDE.openEditor(activePage, markers[i]);
                        return;
                    }
                }
            } catch (CoreException e) {
                DoltengCore.log(e);
            }

        }

        public String getDescription() {
            return element;
        }

        public Image getImage() {
            return Images.SYNCED;
        }
    }
}
