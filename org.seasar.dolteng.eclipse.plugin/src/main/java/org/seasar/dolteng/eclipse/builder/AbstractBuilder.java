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
package org.seasar.dolteng.eclipse.builder;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author taichi
 * 
 */
public abstract class AbstractBuilder extends IncrementalProjectBuilder {

    private BuildEventExecutor executor;

    protected AbstractBuilder(BuildEventExecutor executor) {
        super();
        this.executor = executor;
    }

    /**
     * @return Returns the executor.
     */
    public BuildEventExecutor getExecutor() {
        return executor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.IncrementalProjectBuilder#build(int,
     *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
            throws CoreException {
        if (kind == FULL_BUILD) {
            fullBuild(monitor);
        } else {
            IResourceDelta delta = getDelta(getProject());
            if (delta == null) {
                fullBuild(monitor);
            } else {
                incrementalBuild(delta, monitor);
            }
        }
        return null;
    }

    protected void fullBuild(IProgressMonitor monitor) {
        Job job = new WorkspaceJob(getExecutor().getTaskName()) {
            @Override
            public IStatus runInWorkspace(final IProgressMonitor monitor)
                    throws CoreException {
                getExecutor().beginingOfFullBuild(monitor);
                try {
                    getProject().accept(new IResourceVisitor() {
                        public boolean visit(IResource resource)
                                throws CoreException {
                            getExecutor().build(resource, monitor);
                            return true;
                        }
                    });
                } finally {
                    getExecutor().afterTheFullBuild(monitor);
                }
                return Status.OK_STATUS;
            }

        };
        job.setPriority(Job.SHORT);
        job.schedule();

    }

    protected void incrementalBuild(IResourceDelta delta,
            final IProgressMonitor monitor) throws CoreException {
        monitor.beginTask(getExecutor().getTaskName(), 1);
        try {
            delta.accept(new IResourceDeltaVisitor() {
                public boolean visit(IResourceDelta delta) throws CoreException {
                    switch (delta.getKind()) {
                    case IResourceDelta.ADDED:
                        getExecutor().added(delta.getResource());
                        break;
                    case IResourceDelta.CHANGED:
                        getExecutor().changed(delta.getResource());
                        break;
                    case IResourceDelta.REMOVED:
                        getExecutor().removed(delta.getResource());
                        break;
                    default:
                        // do nothing ...
                        break;
                    }
                    return true;
                }
            });
            monitor.worked(1);
        } finally {
            monitor.done();
        }
    }
}
