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
package org.seasar.dolteng.eclipse.action;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import jp.aonir.fuzzyxml.FuzzyXMLAttribute;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLNode;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.ui.texteditor.ITextEditor;
import org.seasar.dolteng.core.template.TemplateExecutor;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.template.ASPageTemplateHandler;
import org.seasar.dolteng.eclipse.util.ActionScriptUtil;
import org.seasar.dolteng.eclipse.util.FuzzyXMLUtil;
import org.seasar.dolteng.eclipse.util.NamingConventionUtil;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.TextEditorUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.dolteng.eclipse.wigets.ResourceTreeSelectionDialog;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.ClassUtil;

import uk.co.badgersinfoil.metaas.dom.ASArg;
import uk.co.badgersinfoil.metaas.dom.ASClassType;
import uk.co.badgersinfoil.metaas.dom.ASCompilationUnit;
import uk.co.badgersinfoil.metaas.dom.ASField;
import uk.co.badgersinfoil.metaas.dom.ASMethod;
import uk.co.badgersinfoil.metaas.dom.ASType;
import uk.co.badgersinfoil.metaas.dom.Visibility;

/**
 * @author taichi
 * 
 */
public class NewASPageAction extends AbstractWorkbenchWindowActionDelegate {

    /**
     * 
     */
    public NewASPageAction() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.action.AbstractEditorActionDelegate#processResource(org.eclipse.core.resources.IProject,
     *      org.seasar.dolteng.eclipse.preferences.DoltengPreferences,
     *      org.eclipse.core.resources.IResource)
     */
    @Override
    protected void processResource(IProject project,
            final DoltengPreferences pref, final IResource resource)
            throws Exception {
        final IJavaProject javap = JavaCore.create(project);
        if (resource.getType() != IResource.FILE && javap.exists()) {
            return;
        }
        // DTO の選択ダイアログ
        ResourceTreeSelectionDialog dialog = new ResourceTreeSelectionDialog(
                WorkbenchUtil.getShell(), ProjectUtil.getWorkspaceRoot(),
                IResource.PROJECT | IResource.FOLDER | IResource.FILE);
        dialog.setTitle(Messages.SELECT_ACTION_SCRIPT_DTO);
        dialog.setAllowMultiple(false);
        dialog.setInitialSelection(resource.getParent());
        dialog.addFilter(new ViewerFilter() {
            @Override
            public boolean select(Viewer viewer, Object parentElement,
                    Object element) {
                if (element instanceof IFile) {
                    IFile file = (IFile) element;
                    return file.getFileExtension().endsWith("as");
                }
                return true;
            }
        });
        if (dialog.open() != Window.OK) {
            return;
        }
        Object[] selected = dialog.getResult();
        if (selected == null || selected.length < 1) {
            return;
        }
        if ((selected[0] instanceof IFile) == false) {
            return;
        }
        final IFile asdto = (IFile) selected[0];
        final IFile mxml = (IFile) resource;
        final ITextEditor txtEditor = TextEditorUtil.toTextEditor(WorkbenchUtil
                .getActiveEditor());
        WorkbenchUtil.getWorkbenchWindow().run(false, false,
                new IRunnableWithProgress() {
                    public void run(IProgressMonitor monitor)
                            throws InvocationTargetException {
                        try {
                            // TemplateHandlerの生成
                            ASPageTemplateHandler handler = new ASPageTemplateHandler(
                                    mxml, asdto, monitor);
                            // TemplateExecutorの実行
                            TemplateExecutor executor = DoltengCore
                                    .getTemplateExecutor();
                            executor.proceed(handler);
                            IFile page = handler.getGenarated();
                            // 生成されたリソースへのPersistantProperty設定。(mxmlにBindingタグを埋めるのに使う。)
                            mxml.setPersistentProperty(
                                    Constants.PROP_FLEX_PAGE_DTO_PATH, asdto
                                            .getFullPath().toString());
                            addPageDefine(mxml, page, txtEditor);

                            // Javaのサービス呼出しのコードを追加する。
                            addSeviceMethod(javap, pref, mxml, page, asdto,
                                    txtEditor);

                            WorkbenchUtil.openResource(page);
                        } catch (Exception e) {
                            DoltengCore.log(e);
                            throw new InvocationTargetException(e);
                        }
                    }
                });

    }

    private void addPageDefine(IFile mxml, final IFile page, ITextEditor editor) {
        ActionScriptUtil.modifyMxml(mxml, editor,
                new ActionScriptUtil.MxmlMdifyHandler() {
                    public void modify(FuzzyXMLElement root, IDocument document)
                            throws Exception {
                        addPageDefine(page, document, root);
                    }
                });
    }

    private void addPageDefine(IFile page, IDocument doc, FuzzyXMLElement root)
            throws Exception {
        MultiTextEdit edits = new MultiTextEdit();

        ASCompilationUnit unit = ActionScriptUtil.parse(page);
        String pkgName = unit.getPackageName();
        String pkgLast = ClassUtil.getShortClassName(pkgName);
        String xmlns = "xmlns:" + pkgLast;

        if (root.hasAttribute(xmlns) == false) {
            FuzzyXMLAttribute[] attrs = root.getAttributes();
            if (attrs != null && 0 < attrs.length) {
                FuzzyXMLAttribute a = attrs[attrs.length - 1];
                StringBuffer stb = new StringBuffer();
                stb.append(" ");
                stb.append(xmlns);
                stb.append("=\"");
                stb.append(pkgName);
                stb.append(".*");
                stb.append("\"");
                edits.addChild(new InsertEdit(a.getOffset() + a.getLength(),
                        stb.toString()));
            }
        }

        StringBuffer pagedefine = new StringBuffer();
        pagedefine.append("<");
        pagedefine.append(pkgLast);
        pagedefine.append(':');
        ASClassType clazz = (ASClassType) unit.getType();
        pagedefine.append(clazz.getName());
        pagedefine.append(" id=\"page\"");
        pagedefine.append("/>");
        pagedefine.append(ProjectUtil.getLineDelimiterPreference(page
                .getProject()));

        edits.addChild(new InsertEdit(calcInsertOffset(doc, root), pagedefine
                .toString()));

        edits.apply(doc);
    }

    private int calcInsertOffset(IDocument doc, FuzzyXMLElement root)
            throws Exception {
        int result = 0;
        ITextEditor txtEditor = TextEditorUtil.toTextEditor(WorkbenchUtil
                .getActiveEditor());
        if (txtEditor != null) {
            ISelectionProvider sp = txtEditor.getSelectionProvider();
            if (sp != null) {
                ISelection s = sp.getSelection();
                if (s instanceof ITextSelection) {
                    ITextSelection ts = (ITextSelection) s;
                    return ts.getOffset();
                }
            }
        }

        FuzzyXMLElement kid = FuzzyXMLUtil.getFirstChild(root);
        if (kid != null) {
            int line = doc.getLineOfOffset(kid.getOffset());
            return doc.getLineOffset(line);
        }
        return result;
    }

    private void addSeviceMethod(final IJavaProject project,
            final DoltengPreferences pref, IFile mxml, final IFile page,
            final IFile asdto, ITextEditor editor) {
        ActionScriptUtil.modifyMxml(mxml, editor,
                new ActionScriptUtil.MxmlMdifyHandler() {
                    public void modify(FuzzyXMLElement root, IDocument document)
                            throws Exception {
                        addSeviceMethod(project, pref, page, asdto, root);
                    }
                });
    }

    private void addSeviceMethod(IJavaProject project, DoltengPreferences pref,
            IFile page, IFile asdto, FuzzyXMLElement root) throws Exception {
        ASCompilationUnit asUnit = ActionScriptUtil.parse(page);
        ASCompilationUnit asDto = ActionScriptUtil.parse(asdto);

        FuzzyXMLElement e = selectService(root);
        if (e != null) {
            String attr = FuzzyXMLUtil.getAttribute(e, "destination");
            NamingConvention nc = pref.getNamingConvention();
            IType service = NamingConventionUtil.fromComponentNameToType(nc,
                    attr, project);
            if (service != null) {
                IMethod[] methods = service.getMethods();
                ASClassType type = (ASClassType) asUnit.getType();
                ASClassType dto = (ASClassType) asDto.getType();
                for (int i = 0; methods != null && i < methods.length; i++) {
                    IMethod m = methods[i];
                    // ActionScriptはオーバーロード出来ない為。
                    if (type.getMethod(m.getElementName()) == null) {
                        addServiceMethod(m, type, dto);
                    }
                }
            }
        }
        ActionScriptUtil.write(asUnit, page);
    }

    private FuzzyXMLElement selectService(FuzzyXMLElement current) {
        if (current != null) {
            for (FuzzyXMLNode node : current.getChildren()) {
                if (node instanceof FuzzyXMLElement) {
                    FuzzyXMLElement e = (FuzzyXMLElement) node;
                    String attr = FuzzyXMLUtil.getAttribute(e, "id");
                    if ("seasar:S2Flex2Service".equalsIgnoreCase(e.getName())
                            && "service".equalsIgnoreCase(attr)) {
                        return e;
                    }
                    selectService(e);
                }
            }
        }
        return null;
    }

    private void addServiceMethod(IMethod m, ASClassType type, ASClassType dto)
            throws Exception {
        String function = m.getElementName();
        String success = function + "OnSuccess";
        String fault = function + "OnFault";
        List<String> args = Arrays.asList(m.getParameterNames());

        ASMethod main = type.newMethod(function, Visibility.PUBLIC, "void");
        for (String name : args) {
            StringBuffer var = new StringBuffer();
            var.append("var ");
            var.append(name);
            ASField f = dto.getField(name);
            if (f == null) {
                var.append(" = ");
                var.append("null");
            } else {
                var.append(" : ");
                var.append(f.getType());
                var.append(" = ");
                var.append("model.");
                var.append(name);
            }
            var.append(";");
            main.addStmt(var.toString());
        }
        StringBuffer remoteCall = new StringBuffer(100);
        remoteCall.append("remoteCall(service.");
        remoteCall.append(function);
        remoteCall.append('(');
        for (Iterator<String> i = args.iterator(); i.hasNext();) {
            remoteCall.append(i.next());
            if (i.hasNext()) {
                remoteCall.append(", ");
            }
        }
        remoteCall.append("), ");
        remoteCall.append(success);
        remoteCall.append(", ");
        remoteCall.append(fault);
        remoteCall.append(");");

        main.addStmt(remoteCall.toString());

        addEventFunction(type, success, "ResultEvent");
        ASMethod asm = addEventFunction(type, fault, "FaultEvent");
        asm.addStmt("Alert.show(\"" + function + " is fault\");");
    }

    private ASMethod addEventFunction(ASType type, String name, String eventType) {
        ASMethod m = type.newMethod(name, Visibility.PUBLIC, "void");
        m.addParam("e", eventType);
        ASArg arg = m.addParam("token", "Object");
        arg.setDefault("null");
        return m;
    }
}
