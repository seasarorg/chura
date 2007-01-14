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

import java.io.Reader;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.JavadocContentAccess;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Images;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.operation.DIMarkingJob;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.dolteng.eclipse.util.ElementMarkingWalker;
import org.seasar.framework.util.ReaderUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class DIMapper implements IMarkerResolutionGenerator2,
        IElementChangedListener, ElementMarkingWalker.EventHandler {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.core.IElementChangedListener#elementChanged(org.eclipse.jdt.core.ElementChangedEvent)
     */
    public void elementChanged(ElementChangedEvent event) {
        ElementMarkingWalker.walk(event, this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.util.ElementMarkingWalker.EventHandler#isUseMarker(org.eclipse.core.resources.IResource,
     *      org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences)
     */
    public boolean isUseMarker(IResource resource,
            DoltengProjectPreferences pref) {
        try {
            IContainer c = resource.getParent();
            String s = c.getPersistentProperty(Constants.PROP_USE_DI_MARKER);
            return pref.isUseDIMarker() && Boolean.getBoolean(s);
        } catch (CoreException e) {
            DoltengCore.log(e);
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.util.ElementMarkingWalker.EventHandler#tryMarking(org.eclipse.core.resources.IResource,
     *      org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences)
     */
    public void tryMarking(final IResource resource,
            final DoltengProjectPreferences pref) throws CoreException {
        DIMarkingJob job = new DIMarkingJob(resource, pref);
        job.schedule(10L);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.util.ElementMarkingWalker.EventHandler#removeMarker(org.eclipse.core.resources.IResource,
     *      org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences)
     */
    public void removeMarker(IResource r, DoltengProjectPreferences pref)
            throws CoreException {
        r.deleteMarkers(Constants.ID_DI_MAPPER, true, IResource.DEPTH_ZERO);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IMarkerResolutionGenerator2#hasResolutions(org.eclipse.core.resources.IMarker)
     */
    public boolean hasResolutions(IMarker marker) {
        try {
            return Constants.ID_DI_MAPPER.equals(marker.getType());
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
        return new IMarkerResolution2[] { new DIMappingResolution(marker) };
    }

    private class DIMappingResolution implements IMarkerResolution2 {

        private IType injectionType;

        private String javadoc;

        public DIMappingResolution(IMarker marker) {
            try {
                Map m = marker.getAttributes();
                String typename = (String) m
                        .get(Constants.MARKER_ATTR_MAPPING_TYPE_NAME);
                IJavaProject project = JavaCore.create(marker.getResource()
                        .getProject());
                if (StringUtil.isEmpty(typename)) {
                    return;
                }
                injectionType = project.findType(typename);
                if (injectionType == null) {
                    return;
                }

                Reader reader = JavadocContentAccess.getHTMLContentReader(
                        injectionType, false);
                if (reader != null) {
                    javadoc = ReaderUtil.readText(reader);
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
            return javadoc;
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
            return Labels.bind(Labels.JUMP_TO_CLASS, new String[] {
                    injectionType.getElementName(), "" });
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IMarkerResolution#run(org.eclipse.core.resources.IMarker)
         */
        public void run(IMarker marker) {
            try {
                JavaUI.openInEditor(injectionType);
            } catch (CoreException e) {
                DoltengCore.log(e);
            }
        }

    }

}
