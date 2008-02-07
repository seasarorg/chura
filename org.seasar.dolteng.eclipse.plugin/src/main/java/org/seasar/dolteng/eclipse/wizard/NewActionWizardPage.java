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
package org.seasar.dolteng.eclipse.wizard;

import java.lang.reflect.Modifier;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.CodeGeneration;
import org.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.seasar.dolteng.core.entity.MethodMetaData;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.operation.AddPropertyOperation;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class NewActionWizardPage extends NewClassWizardPage {

    private NewPageWizardPage pagePage;

    private PageMappingPage mappingPage;

    public NewActionWizardPage(NewPageWizardPage pagePage,
            PageMappingPage mappingPage) {
        super();
        this.pagePage = pagePage;
        this.mappingPage = mappingPage;
    }

    @Override
    protected void createTypeMembers(IType type, ImportsManager imports,
            IProgressMonitor monitor) throws CoreException {
        try {
            String lineDelimiter = ProjectUtil.getProjectLineDelimiter(type
                    .getJavaProject());
            createActionMethod(type, imports,
                    new SubProgressMonitor(monitor, 1), lineDelimiter);

            IType pageType = pagePage.getCreatedType();
            AddPropertyOperation op = new AddPropertyOperation(type
                    .getCompilationUnit(), pageType, false);
            op.run(null);

            super.createTypeMembers(type, imports, monitor);
        } catch (CoreException e) {
            DoltengCore.log(e);
            throw e;
        } catch (Exception e) {
            DoltengCore.log(e);
        }
    }

    protected void createActionMethod(IType type, ImportsManager imports,
            IProgressMonitor monitor, String lineDelimiter)
            throws CoreException {
        for (MethodMetaData meta : this.mappingPage.getActionMethods()) {
            StringBuffer stb = new StringBuffer();
            if (isAddComments()) {
                String comment = CodeGeneration.getMethodComment(type
                        .getCompilationUnit(), type.getTypeQualifiedName('.'),
                        meta.getName(), StringUtil.EMPTY_STRINGS,
                        StringUtil.EMPTY_STRINGS, "QClass;", null,
                        lineDelimiter);
                if (StringUtil.isEmpty(comment) == false) {
                    stb.append(comment);
                    stb.append(lineDelimiter);
                }
            }

            stb.append(Modifier.toString(meta.getModifiers()));
            stb.append(" Class ");
            stb.append(meta.getName());
            stb.append("() {");
            stb.append(lineDelimiter);
            stb.append("return null;");
            stb.append(lineDelimiter);
            stb.append('}');

            type.createMethod(stb.toString(), null, false, monitor);
        }
    }
}
