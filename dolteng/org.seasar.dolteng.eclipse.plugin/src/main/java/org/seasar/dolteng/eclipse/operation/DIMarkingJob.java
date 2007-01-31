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
package org.seasar.dolteng.eclipse.operation;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;
import org.seasar.dolteng.eclipse.util.TypeUtil;
import org.seasar.framework.convention.NamingConvention;

/**
 * @author taichi
 * 
 */
public class DIMarkingJob extends WorkspaceJob {

    private IResource resource;

    private DoltengPreferences pref;

    /**
     * @param name
     */
    public DIMarkingJob(IResource resource, DoltengPreferences pref) {
        super(Messages.bind(Messages.PROCESS_MAPPING, resource.getName()));
        setPriority(Job.SHORT);
        this.resource = resource;
        this.pref = pref;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.WorkspaceJob#runInWorkspace(org.eclipse.core.runtime.IProgressMonitor)
     */
    public IStatus runInWorkspace(IProgressMonitor monitor)
            throws CoreException {
        monitor = ProgressMonitorUtil.care(monitor);
        monitor.beginTask(Messages.bind(Messages.PROCESS_MAPPING, resource
                .getName()), 3);
        try {
            if (resource.exists()) {
                resource.deleteMarkers(Constants.ID_DI_MAPPER, true,
                        IResource.DEPTH_ZERO);
            }
            ProgressMonitorUtil.isCanceled(monitor, 1);

            IJavaElement e = JavaCore.create(resource);
            final IJavaProject project = e.getJavaProject();
            if (e.getElementType() == IJavaElement.COMPILATION_UNIT) {
                ICompilationUnit unit = (ICompilationUnit) e;

                ProgressMonitorUtil.isCanceled(monitor, 1);

                NamingConvention nc = pref.getNamingConvention();
                IType[] types = unit.getAllTypes();
                for (int i = 0; i < types.length; i++) {
                    IField[] fields = types[i].getFields();
                    for (int j = 0; j < fields.length; j++) {
                        IField field = fields[j];
                        String fieldType = TypeUtil.getResolvedTypeName(field
                                .getTypeSignature(), types[i]);
                        if (fieldType.startsWith("java")) {
                            continue;
                        }
                        boolean is = nc.isTargetClassName(fieldType, nc
                                .getDaoSuffix())
                                || nc.isTargetClassName(fieldType, nc
                                        .getDxoSuffix())
                                || nc.isTargetClassName(fieldType, nc
                                        .getActionSuffix())
                                || nc.isTargetClassName(fieldType, nc
                                        .getPageSuffix());
                        if (is == false) {
                            String name = nc
                                    .toImplementationClassName(fieldType);
                            IType t = project.findType(name);
                            if (is = t != null && t.exists()) {
                                fieldType = name;
                            }
                        }
                        if (is) {
                            Map m = new HashMap();
                            ISourceRange range = field.getNameRange();
                            m.put(IMarker.CHAR_START, new Integer(range
                                    .getOffset()));
                            m.put(IMarker.CHAR_END, new Integer(range
                                    .getOffset()
                                    + range.getLength()));
                            m.put(Constants.MARKER_ATTR_MAPPING_TYPE_NAME,
                                    fieldType);
                            IMarker marker = resource
                                    .createMarker(Constants.ID_DI_MAPPER);
                            marker.setAttributes(m);
                        }
                    }
                }
            }

            ProgressMonitorUtil.isCanceled(monitor, 1);
        } finally {
            monitor.done();
        }

        return Status.OK_STATUS;
    }

}
