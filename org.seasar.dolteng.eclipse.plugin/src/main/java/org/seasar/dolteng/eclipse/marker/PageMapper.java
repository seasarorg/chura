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
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Images;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.operation.PageMarkingJob;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.util.DoltengProjectUtil;

/**
 * @author taichi
 * 
 */
public class PageMapper implements IMarkerResolutionGenerator2,
        IResourceChangeListener {

    private static final Pattern matchHtml = Pattern.compile(".*\\.x?html?$",
            Pattern.CASE_INSENSITIVE);

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
     */
    public void resourceChanged(IResourceChangeEvent event) {
        try {
            event.getDelta().accept(new IResourceDeltaVisitor() {
                public boolean visit(IResourceDelta delta) {
                    IResource resource = delta.getResource();
                    switch (resource.getType()) {
                    case IResource.PROJECT: {
                        IProject p = (IProject) resource;
                        DoltengPreferences pref = DoltengCore.getPreferences(p);
                        return pref != null && pref.isUsePageMarker();
                    }
                    case IResource.FILE: {
                        IFile f = (IFile) resource;
                        if (matchHtml.matcher(resource.getName()).matches()
                                && (delta.getFlags() & IResourceDelta.CONTENT) != 0
                                && DoltengProjectUtil.isInViewPkg(f)) {
                            PageMarkingJob op = new PageMarkingJob(f);
                            op.schedule(10L);
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
     * @see org.eclipse.ui.IMarkerResolutionGenerator2#hasResolutions(org.eclipse.core.resources.IMarker)
     */
    public boolean hasResolutions(IMarker marker) {
        try {
            return Constants.ID_PAGE_MAPPER.equals(marker.getType());
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
        return new IMarkerResolution2[] { new PageMappingResolution(marker) };
    }

    private class PageMappingResolution implements IMarkerResolution2 {
        private String typeName;

        private String fieldName;

        private IType type;

        private IField field;

        private PageMappingResolution(IMarker marker) {
            super();
            lookupJavaElements(marker);
        }

        private void lookupJavaElements(IMarker marker) {
            try {
                Map m = marker.getAttributes();
                this.typeName = (String) m
                        .get(Constants.MARKER_ATTR_MAPPING_TYPE_NAME);
                this.fieldName = (String) m
                        .get(Constants.MARKER_ATTR_MAPPING_FIELD_NAME);
                IResource resource = marker.getResource();
                IJavaProject javap = JavaCore.create(resource.getProject());
                this.type = javap.findType(typeName);
                if (type == null || type.exists() == false) {
                    return;
                }
                this.field = type.getField(fieldName);
            } catch (CoreException e) {
                DoltengCore.log(e);
            }
        }

        public String getLabel() {
            return Labels.bind(Labels.JUMP_TO_CLASS, new String[] { typeName,
                    fieldName });
        }

        public void run(IMarker marker) {
            try {
                if (type == null || type.exists() == false) {
                    return;
                }
                if (field != null && field.exists()) {
                    JavaUI.openInEditor(field);
                } else {
                    JavaUI.openInEditor(type);
                }
            } catch (Exception e) {
                DoltengCore.log(e);
            }
        }

        public String getDescription() {
            String desc = "";
            try {
                if (field != null && field.exists()
                        && field.isBinary() == false) {
                    desc = field.getSource();
                }
            } catch (JavaModelException e) {
                DoltengCore.log(e);
            }
            return desc;
        }

        public Image getImage() {
            return Images.SYNCED;
        }
    }

}
