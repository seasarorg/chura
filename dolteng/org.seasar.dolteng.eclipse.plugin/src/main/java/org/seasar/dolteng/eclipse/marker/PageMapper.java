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

import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.IMarkerResolutionGenerator2;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Images;
import org.seasar.dolteng.eclipse.operation.PageMarkingJob;
import org.seasar.dolteng.eclipse.util.DoltengProjectUtil;

/**
 * @author taichi
 * 
 */
public class PageMapper implements IMarkerResolutionGenerator2,
        IResourceChangeListener {

    private static final Pattern matchHtml = Pattern.compile(".*html?$",
            Pattern.CASE_INSENSITIVE);

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
     */
    public void resourceChanged(IResourceChangeEvent event) {
        try {
            event.getDelta().accept(new IResourceDeltaVisitor() {
                public boolean visit(IResourceDelta delta) throws CoreException {
                    if (delta.getKind() == IResourceDelta.CHANGED
                            && (delta.getFlags() & IResourceDelta.CONTENT) != 0) {
                        IResource resource = delta.getResource();
                        System.out.println("Process ... " + resource.getName());
                        if (resource != null
                                && resource.getType() == IResource.FILE
                                && matchHtml.matcher(resource.getName())
                                        .matches()) {
                            IFile f = (IFile) resource;
                            if (DoltengProjectUtil.isInViewPkg(f)) {
                                PageMarkingJob op = new PageMarkingJob(f);
                                op.schedule();
                            }
                        }
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
        IMarkerResolution2[] resolutions = new IMarkerResolution2[1];
        resolutions[0] = new IMarkerResolution2() {
            public String getLabel() {
                return null;
            }

            public void run(IMarker marker) {
            }

            public String getDescription() {
                return null;
            }

            public Image getImage() {
                return Images.SYNCED;
            }

        };
        return resolutions;
    }

}
