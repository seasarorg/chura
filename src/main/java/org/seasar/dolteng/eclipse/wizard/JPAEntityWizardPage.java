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
package org.seasar.dolteng.eclipse.wizard;

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.CodeGeneration;
import org.seasar.dolteng.eclipse.model.EntityMappingRow;
import org.seasar.framework.util.CaseInsensitiveSet;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class JPAEntityWizardPage extends NewEntityWizardPage {

    private static final Set COLUMNS_VERSION_ANNOTATION = new CaseInsensitiveSet();
    static {
        COLUMNS_VERSION_ANNOTATION.add("VERSION");
        COLUMNS_VERSION_ANNOTATION.add("VERSION_NO");
    }

    public JPAEntityWizardPage(EntityMappingPage page) {
        super(page);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.ui.wizards.NewTypeWizardPage#constructCUContent(org.eclipse.jdt.core.ICompilationUnit,
     *      java.lang.String, java.lang.String)
     */
    protected String constructCUContent(ICompilationUnit cu,
            String typeContent, String lineDelimiter) throws CoreException {
        StringBuffer stb = new StringBuffer();
        stb.append("@Entity");
        stb.append(lineDelimiter);
        if (getPrimaryName(cu).equalsIgnoreCase(
                this.currentSelection.getMetaData().getName()) == false) {
            stb.append("@Table(name=\"");
            stb.append(this.currentSelection.getMetaData().getName());
            stb.append("\")");
            stb.append(lineDelimiter);
        }
        stb.append(typeContent);
        typeContent = stb.toString();
        return super.constructCUContent(cu, typeContent, lineDelimiter);
    }

    private String getPrimaryName(ICompilationUnit cu) {
        return cu.getPath().removeFileExtension().lastSegment();
    }

    protected void createTableAnnotation(IType type, ImportsManager imports,
            IProgressMonitor monitor, String lineDelimiter)
            throws JavaModelException {
        imports.addImport("javax.persistence.Table");
    }

    protected IField createField(IType type, ImportsManager imports,
            EntityMappingRow meta, IProgressMonitor monitor,
            String lineDelimiter) throws CoreException {
        imports.addImport("javax.persistence.Entity");

        StringBuffer stb = new StringBuffer();
        if (isAddComments()) {
            String comment = CodeGeneration.getFieldComment(type
                    .getCompilationUnit(), meta.getJavaClassName(), meta
                    .getJavaFieldName(), lineDelimiter);
            if (StringUtil.isEmpty(comment) == false) {
                stb.append(comment);
                stb.append(lineDelimiter);
            }
        }
        if (meta.isPrimaryKey()) {
            stb.append('@');
            stb.append(imports.addImport("javax.persistence.Id"));
            stb.append(lineDelimiter);
            stb.append('@');
            stb.append(imports.addImport("javax.persistence.GeneratedValue"));
            stb.append(lineDelimiter);
        }
        if (COLUMNS_VERSION_ANNOTATION.contains(meta.getSqlColumnName())) {
            stb.append('@');
            stb.append(imports.addImport("javax.persistence.Version"));
            stb.append(lineDelimiter);
        }
        if (meta.getSqlColumnName().equalsIgnoreCase(meta.getJavaFieldName()) == false) {
            stb.append('@');
            stb.append(imports.addImport("javax.persistence.Column"));
            stb.append("(name=\"");
            stb.append(meta.getSqlColumnName());
            stb.append("\")");
            stb.append(lineDelimiter);
        }
        stb.append("private ");
        stb.append(imports.addImport(meta.getJavaClassName()));
        stb.append(' ');
        stb.append(meta.getJavaFieldName());
        stb.append(';');
        stb.append(lineDelimiter);
        IField result = type.createField(stb.toString(), null, false, monitor);

        return result;
    }
}
