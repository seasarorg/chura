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

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
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
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.util.DoltengProjectUtil;
import org.seasar.dolteng.eclipse.util.JavaElementDeltaAcceptor;
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
        JavaElementDeltaAcceptor.accept(event.getDelta(),
                new JavaElementDeltaAcceptor.Visitor() {
                    @Override
                    protected boolean visit(IJavaProject project) {
                        boolean result = false;
                        DoltengPreferences pref = DoltengCore
                                .getPreferences(project);
                        if (pref != null) {
                            result = pref.isUsePageMarker()
                                    && Constants.VIEW_TYPE_TEEDA.equals(pref
                                            .getViewType());
                        }
                        return result;
                    }

                    @Override
                    protected boolean visit(ICompilationUnit unit) {
                        IProject p = unit.getJavaProject().getProject();
                        DoltengPreferences pref = DoltengCore.getPreferences(p);
                        NamingConvention nc = pref.getNamingConvention();
                        IType type = unit.findPrimaryType();
                        if (type != null) {
                            String typeName = type.getFullyQualifiedName();
                            if (nc.isTargetClassName(typeName, nc
                                    .getPageSuffix())
                                    || nc.isTargetClassName(typeName, nc
                                            .getActionSuffix())) {
                                IFile file = DoltengProjectUtil.findHtmlByJava(
                                        p, pref, unit);
                                if (file != null) {
                                    PageMarkingJob op = new PageMarkingJob(file);
                                    op.schedule(10L);
                                }
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
            return Labels.bind(Labels.JUMP_TO, html.getName());
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
