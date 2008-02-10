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

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
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
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.util.JavaElementDeltaAcceptor;
import org.seasar.dolteng.eclipse.util.TextEditorUtil;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.ReaderUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class DIMapper implements IMarkerResolutionGenerator2,
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
                            result = pref.isUseDIMarker();
                        }
                        return result;
                    }

                    @Override
                    protected boolean visit(ICompilationUnit unit) {
                        DIMarkingJob job = new DIMarkingJob(unit);
                        job.schedule(10L);
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

                javadoc = readClassJavadoc(injectionType);
                if (StringUtil.isEmpty(javadoc) && injectionType.isClass()) {
                    NamingConvention nc = DoltengCore.getPreferences(project)
                            .getNamingConvention();
                    typename = nc.toInterfaceClassName(injectionType
                            .getFullyQualifiedName());
                    IType t = project.findType(typename);
                    if (t != null && t.exists()) {
                        javadoc = readClassJavadoc(t);
                    }
                }
            } catch (CoreException e) {
                DoltengCore.log(e);
            }
        }

        private String readClassJavadoc(IType type) {
            String result = "";
            Reader reader = null;
            try {
                reader = JavadocContentAccess.getHTMLContentReader(type, true,
                        false);
                if (reader != null) {
                    result = ReaderUtil.readText(reader);
                }
            } catch (Exception e) {
                DoltengCore.log(e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                    }
                }
            }
            return result;
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
                TextEditorUtil.selectAndReveal(injectionType);
            } catch (CoreException e) {
                DoltengCore.log(e);
            }
        }

    }

}
