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

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.NamingConventions;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.ui.CodeGeneration;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.EntityMappingRow;
import org.seasar.dolteng.eclipse.model.impl.TableNode;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class NewEntityWizardPage extends NewClassWizardPage {
	// FIXME : 要IF文削除

	private MetaDataMappingPage mappingPage = null;

	private TableNode currentSelection = null;

	public NewEntityWizardPage(MetaDataMappingPage page) {
		this.mappingPage = page;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.ui.wizards.NewTypeWizardPage#constructCUContent(org.eclipse.jdt.core.ICompilationUnit,
	 *      java.lang.String, java.lang.String)
	 */
	protected String constructCUContent(ICompilationUnit cu,
			String typeContent, String lineDelimiter) throws CoreException {
		if (isUseS2Dao(cu) == false) {
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
		}
		return super.constructCUContent(cu, typeContent, lineDelimiter);
	}

	private String getPrimaryName(ICompilationUnit cu) {
		return cu.getPath().removeFileExtension().lastSegment();
	}

	private boolean isUseS2Dao(ICompilationUnit cu) {
		return this.isUseS2Dao(cu.getJavaProject());
	}

	private boolean isUseS2Dao(IJavaProject javap) {
		DoltengProjectPreferences pref = DoltengCore.getPreferences(javap);
		if (pref != null) {
			return pref.isUseS2Dao();
		}
		return false;
	}

	protected void createTypeMembers(IType type, ImportsManager imports,
			IProgressMonitor monitor) throws CoreException {
		imports.addImport("javax.persistence.Entity");
		String lineDelimiter = ProjectUtil.getProjectLineDelimiter(type
				.getJavaProject());
		// メンバフィールド及び、アクセサの生成。処理順が超微妙。
		if (type.getElementName().equalsIgnoreCase(
				this.currentSelection.getMetaData().getName()) == false) {
			imports.addImport("javax.persistence.Table");
			if (isUseS2Dao(type.getJavaProject())) {
				// TABLEアノテーション
				StringBuffer stb = new StringBuffer();
				stb.append("public static final ");
				stb.append(imports.addImport("java.lang.String"));
				stb.append(" TABLE = \"");
				stb.append(this.currentSelection.getMetaData().getName());
				stb.append("\";");
				stb.append(lineDelimiter);
				type.createField(stb.toString(), null, false, monitor);
			}
		}

		// 後で、設定通りの改行コードに変換して貰える。
		List fields = mappingPage.getMappingRows();
		for (final Iterator i = fields.iterator(); i.hasNext();) {
			EntityMappingRow meta = (EntityMappingRow) i.next();
			IField field = createField(type, imports, meta,
					new SubProgressMonitor(monitor, 1), lineDelimiter);
			createGetter(type, imports, meta, field, new SubProgressMonitor(
					monitor, 1), lineDelimiter);
			createSetter(type, imports, meta, field, new SubProgressMonitor(
					monitor, 1), lineDelimiter);
		}

		super.createTypeMembers(type, imports, monitor);
	}

	/**
	 * @param type
	 * @param imports
	 * @param meta
	 * @param monitor
	 * @return
	 * @throws CoreException
	 * @throws JavaModelException
	 */
	private IField createField(IType type, ImportsManager imports,
			EntityMappingRow meta, IProgressMonitor monitor,
			String lineDelimiter) throws CoreException, JavaModelException {
		StringBuffer stb = new StringBuffer();
		boolean diff = meta.getSqlColumnName().equalsIgnoreCase(
				meta.getJavaFieldName()) == false;
		if (isAddComments()) {
			String comment = CodeGeneration.getFieldComment(type
					.getCompilationUnit(), meta.getJavaClassName(), meta
					.getJavaFieldName(), lineDelimiter);
			if (StringUtil.isEmpty(comment) == false) {
				stb.append(comment);
				stb.append(lineDelimiter);
			}
		}
		if (isUseS2Dao(type.getJavaProject()) == false) {
			if (meta.isPrimaryKey()) {
				stb.append('@');
				stb.append(imports.addImport("javax.persistence.Id"));
				stb.append(lineDelimiter);
				stb.append('@');
				stb.append(imports
						.addImport("javax.persistence.GeneratedValue"));
				stb.append(lineDelimiter);
			}
			if (diff) {
				stb.append('@');
				stb.append(imports.addImport("javax.persistence.Column"));
				stb.append("(name=\"");
				stb.append(meta.getSqlColumnName());
				stb.append("\");");
				stb.append(lineDelimiter);
			}
		}
		stb.append("private ");
		stb.append(imports.addImport(meta.getJavaClassName()));
		stb.append(' ');
		stb.append(meta.getJavaFieldName());
		stb.append(';');
		stb.append(lineDelimiter);
		IField result = type.createField(stb.toString(), null, false, monitor);
		if (diff) {
			if (isUseS2Dao(type.getJavaProject())) {
				// カラムアノテーション
				stb = new StringBuffer();
				stb.append("public static final ");
				stb.append(imports.addImport("java.lang.String"));
				stb.append(' ');
				stb.append(meta.getJavaFieldName());
				stb.append("_COLUMN = \"");
				stb.append(meta.getSqlColumnName());
				stb.append("\";");
				stb.append(lineDelimiter);
				type.createField(stb.toString(), null, false, monitor);
			}
		}

		return result;
	}

	protected void createGetter(IType type, ImportsManager imports,
			EntityMappingRow meta, IField field, IProgressMonitor monitor,
			String lineDelimiter) throws CoreException {
		String fieldName = field.getElementName();
		IType parentType = field.getDeclaringType();
		String getterName = NamingConventions.suggestGetterName(type
				.getJavaProject(), fieldName, field.getFlags(), field
				.getTypeSignature().equals(Signature.SIG_BOOLEAN),
				StringUtil.EMPTY_STRINGS);

		String typeName = Signature.toString(field.getTypeSignature());
		String accessorName = NamingConventions
				.removePrefixAndSuffixForFieldName(field.getJavaProject(),
						fieldName, field.getFlags());

		StringBuffer stb = new StringBuffer();
		if (isAddComments()) {
			String comment = CodeGeneration.getGetterComment(field
					.getCompilationUnit(),
					parentType.getTypeQualifiedName('.'), getterName, field
							.getElementName(), typeName, accessorName,
					lineDelimiter);
			if (comment != null) {
				stb.append(comment);
				stb.append(lineDelimiter);
			}
		}

		stb.append(Modifier.toString(meta.getJavaModifiers()));
		stb.append(' ');
		stb.append(imports.addImport(typeName));
		stb.append(' ');
		stb.append(getterName);
		stb.append("() {");
		stb.append(lineDelimiter);

		if (useThisForFieldAccess(field)) {
			fieldName = "this." + fieldName;
		}

		String body = CodeGeneration.getGetterMethodBodyContent(field
				.getCompilationUnit(), parentType.getTypeQualifiedName('.'),
				getterName, fieldName, lineDelimiter);
		if (body != null) {
			stb.append(body);
		}
		stb.append("}");
		stb.append(lineDelimiter);
		type.createMethod(stb.toString(), null, false, monitor);
	}

	/**
	 * @param field
	 * @return
	 */
	private boolean useThisForFieldAccess(IField field) {
		boolean useThis = Boolean.valueOf(
				PreferenceConstants.getPreference(
						PreferenceConstants.CODEGEN_KEYWORD_THIS, field
								.getJavaProject())).booleanValue();
		return useThis;
	}

	protected void createSetter(IType type, ImportsManager imports,
			EntityMappingRow meta, IField field, IProgressMonitor monitor,
			String lineDelimiter) throws CoreException {
		String fieldName = field.getElementName();
		IType parentType = field.getDeclaringType();
		String setterName = NamingConventions.suggestSetterName(type
				.getJavaProject(), fieldName, field.getFlags(), field
				.getTypeSignature().equals(Signature.SIG_BOOLEAN),
				StringUtil.EMPTY_STRINGS);

		String returnSig = field.getTypeSignature();
		String typeName = Signature.toString(returnSig);

		IJavaProject project = field.getJavaProject();

		String accessorName = NamingConventions
				.removePrefixAndSuffixForFieldName(project, fieldName, field
						.getFlags());
		String argname = accessorName;

		StringBuffer stb = new StringBuffer();
		if (isAddComments()) {
			String comment = CodeGeneration.getSetterComment(field
					.getCompilationUnit(),
					parentType.getTypeQualifiedName('.'), setterName, field
							.getElementName(), typeName, argname, accessorName,
					lineDelimiter);
			if (StringUtil.isEmpty(comment) == false) {
				stb.append(comment);
				stb.append(lineDelimiter);
			}
		}

		stb.append(Modifier.toString(meta.getJavaModifiers()));
		stb.append(" void ");
		stb.append(setterName);
		stb.append('(');
		stb.append(imports.addImport(typeName));
		stb.append(' ');
		stb.append(argname);
		stb.append(") {");
		stb.append(lineDelimiter);

		boolean useThis = useThisForFieldAccess(field);
		if (argname.equals(fieldName) || useThis) {
			fieldName = "this." + fieldName;
		}
		String body = CodeGeneration.getSetterMethodBodyContent(field
				.getCompilationUnit(), parentType.getTypeQualifiedName('.'),
				setterName, fieldName, argname, lineDelimiter);
		if (body != null) {
			stb.append(body);
		}
		stb.append("}");
		stb.append(lineDelimiter);
		type.createMethod(stb.toString(), null, false, monitor);
	}

	/**
	 * @param currentSelection
	 *            The currentSelection to set.
	 */
	public void setCurrentSelection(TableNode currentSelection) {
		this.currentSelection = currentSelection;
	}

}
