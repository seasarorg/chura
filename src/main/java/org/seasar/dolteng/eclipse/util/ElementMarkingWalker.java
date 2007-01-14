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
package org.seasar.dolteng.eclipse.util;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;

/**
 * @author taichi
 * 
 */
public class ElementMarkingWalker {

    /**
     * 
     */
    public ElementMarkingWalker() {
        super();
    }

    public static void walk(ElementChangedEvent event,
            final EventHandler handler) {
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
                            if (pref != null && handler.isUseMarker(r, pref)
                                    && r.getType() == IResource.FILE
                                    && "java".equals(r.getFileExtension())) {
                                if (delta.getKind() == IResourceDelta.REMOVED) {
                                    handler.removeMarker(r, pref);
                                } else {
                                    handler.tryMarking(r, pref);
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

    public interface EventHandler {
        boolean isUseMarker(IResource resource, DoltengProjectPreferences pref);

        void tryMarking(IResource r, DoltengProjectPreferences pref)
                throws CoreException;

        void removeMarker(IResource r, DoltengProjectPreferences pref)
                throws CoreException;
    }
}
