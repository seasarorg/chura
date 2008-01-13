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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.util.ActionScriptUtil;
import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;

import uk.co.badgersinfoil.metaas.dom.ASCompilationUnit;
import uk.co.badgersinfoil.metaas.dom.ASMethod;
import uk.co.badgersinfoil.metaas.dom.ASType;
import uk.co.badgersinfoil.metaas.dom.Visibility;

/**
 * @author taichi
 * 
 */
public class AddRemoteServiceCallJob extends WorkspaceJob {

    // FIXME : 未完成

    private IFile as;

    private String[] methods;

    /**
     * @param as
     * @param methods
     */
    public AddRemoteServiceCallJob(IFile as, String[] methods) {
        super("AddRemoteServiceCallJob");
        this.as = as;
        this.methods = methods;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.WorkspaceJob#runInWorkspace(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStatus runInWorkspace(IProgressMonitor monitor) {
        monitor = ProgressMonitorUtil.care(monitor);
        try {
            ASCompilationUnit unit = ActionScriptUtil.parse(as);
            ASType type = unit.getType();
            for (String methodName : methods) {
                if (methodName.endsWith("OnSuccess")) {
                    addSuccessEvent(type, methodName);
                } else if (methodName.endsWith("OnFault")) {
                    addFaultEvent(type, methodName);
                } else {
                    addRemoteCall(type, methodName);
                }
            }

            ActionScriptUtil.write(unit, as);
        } catch (Exception e) {
            DoltengCore.log(e);
        }
        return Status.OK_STATUS;
    }

    private void addRemoteCall(ASType type, String name) {
        ASMethod m = type.newMethod(name, Visibility.PUBLIC, "void");
        // XXX NewASPageAction 辺りと処理の統合を考えるべぇし。
    }

    private void addSuccessEvent(ASType type, String name) {

    }

    private void addFaultEvent(ASType type, String name) {

    }

}
