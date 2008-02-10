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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.NamingConventions;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.ui.CodeGeneration;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.seasar.dolteng.core.entity.MethodMetaData;
import org.seasar.dolteng.core.teeda.TeedaEmulator;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.PageMappingRow;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.operation.AddArrayPropertyOperation;
import org.seasar.dolteng.eclipse.operation.AddPropertyOperation;
import org.seasar.dolteng.eclipse.operation.TypeHierarchyMethodProcessor;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.CaseInsensitiveMap;
import org.seasar.framework.util.ClassUtil;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class NewPageWizardPage extends NewClassWizardPage {

    private static final String NAME = "NewPageWizardPage";

    private static final String CONFIG_SEPARATE_ACTION = "separateAction";

    private static final String CONFIG_CREATE_BASE_CLASS = "createBaseClass";

    private PageMappingPage mappingPage;

    private NewClassWizardPage invisiblePage;

    private boolean separateAction = false;

    private boolean createBaseClass = false;

    private NewActionWizardPage actionPage;

    private DoltengPreferences preferences;

    private Button pageActionSeparate;

    public NewPageWizardPage(PageMappingPage mappingPage) {
        this.mappingPage = mappingPage;
        this.invisiblePage = new NewClassWizardPage();
    }

    @Override
    public void init(IStructuredSelection selection) {
        super.init(selection);
        IDialogSettings section = getDialogSettings().getSection(NAME);
        if (section != null) {
            this.separateAction = section.getBoolean(CONFIG_SEPARATE_ACTION);
            this.createBaseClass = section.getBoolean(CONFIG_CREATE_BASE_CLASS);
        }
        this.invisiblePage.setWizard(getWizard());
        this.invisiblePage.init(selection);
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        Composite composite = (Composite) getControl();

        createPartOfPageActionSeparate(composite);
        createPartOfAbstractPageClass(composite);
    }

    /**
     * @param composite
     */
    private void createPartOfPageActionSeparate(Composite composite) {
        Label label = new Label(composite, SWT.NONE);
        label.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false,
                false, 4, 1));
        label.setFont(composite.getFont());
        label.setText(Labels.WIZARD_PAGE_SEPARATE_DESCRIPTION);

        createEmptySpace(composite, 1);
        pageActionSeparate = new Button(composite, SWT.CHECK);
        pageActionSeparate.setFont(composite.getFont());
        pageActionSeparate.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button btn = (Button) e.widget;
                separateAction = btn.getSelection();
            }
        });
        pageActionSeparate.setSelection(this.separateAction);
        pageActionSeparate.setText(Labels.WIZARD_PAGE_SEPARATE);
        GridData data = new GridData();
        data.horizontalSpan = 3;
        data.horizontalAlignment = GridData.HORIZONTAL_ALIGN_FILL;
        pageActionSeparate.setLayoutData(data);
    }

    private void createPartOfAbstractPageClass(Composite composite) {
        Label label = new Label(composite, SWT.NONE);
        label.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false,
                false, 4, 1));
        label.setFont(composite.getFont());
        label.setText(Labels.WIZARD_BASE_PAGE_DESCRIPTION);

        createEmptySpace(composite, 1);
        Button b = new Button(composite, SWT.CHECK);
        b.setFont(composite.getFont());
        b.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Button btn = (Button) e.widget;
                createBaseClass = btn.getSelection();
                if (createBaseClass) {
                    setSuperClass(calculateBaseClass(), true);
                } else {
                    setSuperClass("java.lang.Object", true);
                }
            }
        });
        b.setSelection(this.createBaseClass);
        if (this.createBaseClass) {
            setSuperClass(calculateBaseClass(), true);
        }
        b.setText(Labels.WIZARD_BASE_PAGE);
        GridData data = new GridData();
        data.horizontalSpan = 3;
        data.horizontalAlignment = GridData.HORIZONTAL_ALIGN_FILL;
        b.setLayoutData(data);
    }

    private String calculateBaseClass() {
        DoltengPreferences pref = DoltengCore
                .getPreferences(getPackageFragment().getJavaProject());
        if (pref != null) {
            NamingConvention nc = pref.getNamingConvention();
            String webpkg = ClassUtil.concatName(pref
                    .getDefaultRootPackageName(), nc
                    .getSubApplicationRootPackageName());
            String pkg = getPackageText();
            String name = pkg.replaceAll(webpkg + "|\\.", "");
            if (StringUtil.isEmpty(name) == false) {
                return pkg + ".Abstract" + StringUtil.capitalize(name) + "Page";
            }
        }
        return "java.lang.Object";
    }

    public static Control createEmptySpace(Composite parent, int span) {
        Label label = new Label(parent, SWT.LEFT);
        GridData gd = new GridData();
        gd.horizontalAlignment = GridData.BEGINNING;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = span;
        gd.horizontalIndent = 0;
        gd.widthHint = 0;
        gd.heightHint = 0;
        label.setLayoutData(gd);
        return label;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.ui.wizards.NewClassWizardPage#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible && this.preferences != null) {
            if (Constants.DAO_TYPE_UUJI.equals(this.preferences.getDaoType())) {
                this.separateAction = false;
                this.pageActionSeparate.setSelection(false);
                this.pageActionSeparate.setEnabled(false);
            }
        }
    }

    @Override
    protected void createTypeMembers(IType type, ImportsManager imports,
            IProgressMonitor monitor) throws CoreException {

        IType superType = null;
        if (this.createBaseClass) {
            superType = createSuperTypeIfNeeded(monitor);
        }

        String lineDelimiter = ProjectUtil.getProjectLineDelimiter(type
                .getJavaProject());
        List<PageMappingRow> mappingRows = mappingPage.getMappingRows();
        List<PageMappingRow> itemsRows = new ArrayList<PageMappingRow>();

        for (PageMappingRow meta : mappingRows) {
            if (meta.isThisGenerate()) {
                IField field = createField(type, imports, meta, mappingPage
                        .getUsePublicField(),
                        new SubProgressMonitor(monitor, 1), lineDelimiter);
                if (!mappingPage.getUsePublicField()) {
                    createGetter(type, imports, meta, field,
                            new SubProgressMonitor(monitor, 1), lineDelimiter);
                    createSetter(type, imports, meta, field,
                            new SubProgressMonitor(monitor, 1), lineDelimiter);
                }
            } else if (meta.isSuperGenerate() && this.createBaseClass) {
                String name = meta.getPageClassName();
                IWorkspaceRunnable op = null;
                boolean isArray = 0 < name.indexOf('[');
                if (isArray) {
                    name = name.substring(0, name.indexOf('['));
                }
                // FIXME : イマイチ…
                IType fieldType = superType.getJavaProject().findType(name);
                if (fieldType != null) {
                    if (isArray) {
                        op = new AddArrayPropertyOperation(superType
                                .getCompilationUnit(), fieldType, meta
                                .getPageFieldName(), mappingPage
                                .getUsePublicField());
                    } else {
                        op = new AddPropertyOperation(superType
                                .getCompilationUnit(), fieldType, meta
                                .getPageFieldName(), mappingPage
                                .getUsePublicField());
                    }
                }

                if (PrimitiveType.toCode(name) != null) {
                    if (isArray) {
                        op = new AddArrayPropertyOperation(superType
                                .getCompilationUnit(), name, meta
                                .getPageFieldName(), mappingPage
                                .getUsePublicField());
                    } else {
                        op = new AddPropertyOperation(superType
                                .getCompilationUnit(), name, meta
                                .getPageFieldName(), mappingPage
                                .getUsePublicField());
                    }
                }
                if (op != null) {
                    op.run(new SubProgressMonitor(monitor, 1));
                }
            }
            if (TeedaEmulator.MAPPING_MULTI_ITEM.matcher(
                    meta.getPageFieldName()).matches()) {
                itemsRows.add(meta);
            }
        }

        if (this.separateAction == false) {
            actionPage.createActionMethod(type, imports,
                    new SubProgressMonitor(monitor, 1), lineDelimiter);
        }

        createInitialize(type, monitor, lineDelimiter);
        DoltengPreferences pref = DoltengCore.getPreferences(type
                .getJavaProject());
        createPrerender(type, itemsRows, pref, imports, monitor, lineDelimiter,
                mappingPage.getUsePublicField());

        createConditionMethod(type, imports,
                new SubProgressMonitor(monitor, 1), lineDelimiter);

        super.createTypeMembers(type, imports, monitor);

        if (superType != null) {
            JavaUI.openInEditor(superType);
        }

        IDialogSettings section = getDialogSettings().getSection(NAME);
        if (section == null) {
            section = getDialogSettings().addNewSection(NAME);
        }
        section.put(CONFIG_SEPARATE_ACTION, this.separateAction);
        section.put(CONFIG_CREATE_BASE_CLASS, this.createBaseClass);
    }

    private IType createSuperTypeIfNeeded(IProgressMonitor monitor)
            throws CoreException {
        try {
            String baseClass = calculateBaseClass();
            IJavaProject project = getPackageFragment().getJavaProject();
            IType type = project.findType(baseClass);
            if (type == null || type.exists() == false) {
                this.invisiblePage.setPackageFragmentRoot(
                        getPackageFragmentRoot(), true);
                this.invisiblePage.setPackageFragment(getPackageFragment(),
                        true);
                this.invisiblePage.setModifiers(F_PUBLIC | F_ABSTRACT, true);
                this.invisiblePage.setTypeName(baseClass.substring(baseClass
                        .lastIndexOf('.') + 1), true);
                this.invisiblePage.setAddComments(true, true);
                this.invisiblePage
                        .createType(new SubProgressMonitor(monitor, 1));
                type = this.invisiblePage.getCreatedType();
            }
            return type;
        } catch (CoreException e) {
            DoltengCore.log(e);
            throw e;
        } catch (Exception e) {
            DoltengCore.log(e);
            throw new IllegalStateException();
        }
    }

    protected IField createField(IType type, ImportsManager imports,
            PageMappingRow meta, boolean usePublicField,
            IProgressMonitor monitor, String lineDelimiter)
            throws CoreException {

        String pageClassName = meta.getPageClassName();

        StringBuffer stb = new StringBuffer();
        if (isAddComments()) {
            String comment = CodeGeneration.getFieldComment(type
                    .getCompilationUnit(), pageClassName, meta
                    .getPageFieldName(), lineDelimiter);
            if (StringUtil.isEmpty(comment) == false) {
                stb.append(comment);
                stb.append(lineDelimiter);
            }
        }
        stb.append(usePublicField ? "public " : "private ");
        stb.append(imports.addImport(pageClassName));
        stb.append(' ');
        stb.append(meta.getPageFieldName());
        stb.append(';');
        stb.append(lineDelimiter);

        return type.createField(stb.toString(), null, false, monitor);
    }

    protected void createGetter(IType type, ImportsManager imports,
            PageMappingRow meta, IField field, IProgressMonitor monitor,
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

        stb.append(Modifier.toString(meta.getPageModifiers()));
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
        stb.append(lineDelimiter);
        stb.append("}");
        stb.append(lineDelimiter);
        type.createMethod(stb.toString(), null, false, monitor);
    }

    private static boolean useThisForFieldAccess(IField field) {
        boolean useThis = Boolean.valueOf(
                PreferenceConstants.getPreference(
                        PreferenceConstants.CODEGEN_KEYWORD_THIS, field
                                .getJavaProject())).booleanValue();
        return useThis;
    }

    protected void createSetter(IType type, ImportsManager imports,
            PageMappingRow meta, IField field, IProgressMonitor monitor,
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

        stb.append(Modifier.toString(meta.getPageModifiers()));
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
        stb.append(lineDelimiter);
        stb.append("}");
        stb.append(lineDelimiter);
        type.createMethod(stb.toString(), null, false, monitor);
    }

    protected void createInitialize(IType type, IProgressMonitor monitor,
            String lineDelimiter) throws CoreException {
        String name = "initialize";
        StringBuffer stb = new StringBuffer();
        if (isAddComments()) {
            String comment = CodeGeneration.getMethodComment(type
                    .getCompilationUnit(), type.getTypeQualifiedName('.'),
                    name, StringUtil.EMPTY_STRINGS, StringUtil.EMPTY_STRINGS,
                    "QClass;", null, lineDelimiter);
            if (StringUtil.isEmpty(comment) == false) {
                stb.append(comment);
                stb.append(lineDelimiter);
            }
        }

        stb.append("public Class ").append(name).append("() {");
        stb.append(lineDelimiter);
        stb.append("return null;");
        stb.append(lineDelimiter);
        stb.append('}');

        type.createMethod(stb.toString(), null, false, monitor);
    }

    protected void createPrerender(IType type,
            List<PageMappingRow> multiItemsRows, DoltengPreferences pref,
            ImportsManager imports, IProgressMonitor monitor,
            String lineDelimiter, boolean usePublicField) throws CoreException {
        List<String> tables = new ArrayList<String>(multiItemsRows.size());
        NamingConvention nc = pref.getNamingConvention();
        IJavaProject project = type.getJavaProject();
        for (PageMappingRow row : multiItemsRows) {
            String table = row.getPageFieldName();
            table = StringUtil.capitalize(table.replaceAll("Items", ""));
            String dao = table + nc.getDaoSuffix();
            String[] pkgs = nc.getRootPackageNames();
            for (String pkg : pkgs) {
                IType t = project.findType(pkg + "." + nc.getDaoPackageName()
                        + "." + dao);
                if (t != null && t.exists()) {
                    imports.addImport(t.getFullyQualifiedName());
                    AddPropertyOperation op = new AddPropertyOperation(type
                            .getCompilationUnit(), t, usePublicField);
                    op.run(null);
                    tables.add(table);
                    break;
                }
            }
        }

        String name = "prerender";
        StringBuffer stb = new StringBuffer();
        if (isAddComments()) {
            String comment = CodeGeneration.getMethodComment(type
                    .getCompilationUnit(), type.getTypeQualifiedName('.'),
                    name, StringUtil.EMPTY_STRINGS, StringUtil.EMPTY_STRINGS,
                    "QClass;", null, lineDelimiter);
            if (StringUtil.isEmpty(comment) == false) {
                stb.append(comment);
                stb.append(lineDelimiter);
            }
        }

        stb.append("public Class ").append(name).append("() {");
        stb.append(lineDelimiter);

        String search = "find";
        if (Constants.DAO_TYPE_S2DAO.equals(pref.getDaoType())) {
            search = "select";
        }
        for (Iterator i = tables.iterator(); i.hasNext();) {
            String table = (String) i.next();
            stb.append("this.");
            if (usePublicField) {
                table = StringUtil.decapitalize(table);
                stb.append(table);
                stb.append("Items");
                stb.append(" = ");
                stb.append("this.");
                stb.append(table);
                stb.append("Dao.");
                stb.append(search);
                stb.append("All()");
            } else {
                stb.append("set");
                stb.append(table);
                stb.append("Items(");
                stb.append("get");
                stb.append(table);
                stb.append("Dao().");
                stb.append(search);
                stb.append("All())");
            }
            stb.append(';');
            stb.append(lineDelimiter);
        }
        stb.append("return null;");
        stb.append(lineDelimiter);
        stb.append('}');

        type.createMethod(stb.toString(), null, false, monitor);

    }

    protected void createConditionMethod(IType type, ImportsManager imports,
            IProgressMonitor monitor, String lineDelimiter)
            throws CoreException {
        Map methods = getSuperTypeMethods(type);
        for (MethodMetaData meta : this.mappingPage.getConditionMethods()) {
            if (methods.containsKey(meta.getName())) {
                continue;
            }

            StringBuffer stb = new StringBuffer();
            if (isAddComments()) {
                String comment = CodeGeneration.getMethodComment(type
                        .getCompilationUnit(), type.getTypeQualifiedName('.'),
                        meta.getName(), StringUtil.EMPTY_STRINGS,
                        StringUtil.EMPTY_STRINGS, Signature.SIG_BOOLEAN, null,
                        lineDelimiter);
                if (StringUtil.isEmpty(comment) == false) {
                    stb.append(comment);
                    stb.append(lineDelimiter);
                }
            }

            stb.append(Modifier.toString(meta.getModifiers()));
            stb.append(" boolean ");
            stb.append(meta.getName());
            stb.append("() {");
            stb.append(lineDelimiter);
            stb.append("return false;");
            stb.append(lineDelimiter);
            stb.append('}');

            type.createMethod(stb.toString(), null, false, monitor);
        }
    }

    @SuppressWarnings("unchecked")
    protected Map getSuperTypeMethods(IType type) {
        final Map<String, IMethod> result = new CaseInsensitiveMap();
        IRunnableWithProgress runnable = new TypeHierarchyMethodProcessor(type,
                new TypeHierarchyMethodProcessor.MethodHandler() {
                    public void begin() {
                    }

                    public void process(IMethod method) {
                        result.put(method.getElementName(), method);
                    }

                    public void done() {
                    }
                });
        try {
            getContainer().run(false, false, runnable);
        } catch (Exception e) {
            DoltengCore.log(e);
        }
        return result;
    }

    /**
     * @return Returns the separateAction.
     */
    public boolean isSeparateAction() {
        return separateAction;
    }

    /**
     * @param separateAction
     *            The separateAction to set.
     */
    public void setSeparateAction(boolean separateAction) {
        this.separateAction = separateAction;
    }

    /**
     * @return Returns the createBaseClass.
     */
    public boolean isCreateBaseClass() {
        return createBaseClass;
    }

    /**
     * @param createBaseClass
     *            The createBaseClass to set.
     */
    public void setCreateBaseClass(boolean createBaseClass) {
        this.createBaseClass = createBaseClass;
    }

    /**
     * @param actionPage
     *            The actionPage to set.
     */
    public void setActionPage(NewActionWizardPage actionPage) {
        this.actionPage = actionPage;
    }

    /**
     * @return Returns the preferences.
     */
    public DoltengPreferences getPreferences() {
        return preferences;
    }

    /**
     * @param preferences
     *            The preferences to set.
     */
    public void setPreferences(DoltengPreferences preferences) {
        this.preferences = preferences;
    }

}
