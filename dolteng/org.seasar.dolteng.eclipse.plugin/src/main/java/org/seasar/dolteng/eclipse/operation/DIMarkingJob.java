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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
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

    private ICompilationUnit unit;

    public DIMarkingJob(ICompilationUnit unit) {
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
    @Override
    @SuppressWarnings("unchecked")
    public IStatus runInWorkspace(IProgressMonitor monitor) {
        monitor = ProgressMonitorUtil.care(monitor);
        monitor.beginTask(Messages.bind(Messages.PROCESS_MAPPING, unit
                .getElementName()), 3);
        try {
            if (unit.exists()) {
                IResource resource = unit.getResource();
                if (resource.exists()) {
                    resource.deleteMarkers(Constants.ID_DI_MAPPER, true,
                            IResource.DEPTH_ZERO);
                }
                ProgressMonitorUtil.isCanceled(monitor, 1);
                final IJavaProject project = unit.getJavaProject();
                ProgressMonitorUtil.isCanceled(monitor, 1);
                DoltengPreferences pref = DoltengCore.getPreferences(project);
                NamingConvention nc = pref.getNamingConvention();
                IType[] types = unit.getAllTypes();
                for (IType type : types) {
                    IField[] fields = type.getFields();
                    for (IField field : fields) {
                        String fieldType = TypeUtil.getResolvedTypeName(field
                                .getTypeSignature(), type);
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
                            is = t != null;
                            if (is && t.exists()) {
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
                ProgressMonitorUtil.isCanceled(monitor, 1);
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        } finally {
            monitor.done();
        }

        return Status.OK_STATUS;
    }

}
