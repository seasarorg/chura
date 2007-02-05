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
package org.seasar.dolteng.eclipse.action;

import java.lang.reflect.InvocationTargetException;

import jp.aonir.fuzzyxml.FuzzyXMLAttribute;
import jp.aonir.fuzzyxml.FuzzyXMLDocument;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLParser;

import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
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
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.seasar.dolteng.core.template.TemplateExecutor;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.template.ASPageTemplateHandler;
import org.seasar.dolteng.eclipse.util.ActionScriptUtil;
import org.seasar.dolteng.eclipse.util.FuzzyXMLUtil;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.TextFileBufferUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.dolteng.eclipse.wigets.ResourceTreeSelectionDialog;
import org.seasar.framework.util.ClassUtil;

import uk.co.badgersinfoil.metaas.dom.ASClassType;
import uk.co.badgersinfoil.metaas.dom.ASCompilationUnit;

/**
 * @author taichi
 * 
 */
public class NewASPageAction extends AbstractEditorActionDelegate {

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
    protected void processResource(IProject project, DoltengPreferences pref,
            final IResource resource) throws Exception {
        if (resource.getType() != IResource.FILE) {
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

        WorkbenchUtil.getWorkbenchWindow().run(false, false,
                new IRunnableWithProgress() {
                    public void run(IProgressMonitor monitor)
                            throws InvocationTargetException,
                            InterruptedException {
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
                            addPageDefine(mxml, page);
                            WorkbenchUtil.openResource(page);
                        } catch (Exception e) {
                            DoltengCore.log(e);
                            throw new InvocationTargetException(e);
                        }
                    }
                });

    }

    // FIXME : AddServiceActionとのコードの重複を何とかする。
    private void addPageDefine(IFile mxml, IFile page) {
        ITextFileBuffer buffer = null;
        IDocument doc = null;

        try {
            IDocumentProvider provider = this.txtEditor.getDocumentProvider();
            if (provider != null) {
                doc = provider.getDocument(this.txtEditor.getEditorInput());
            }

            if (doc == null) {
                buffer = TextFileBufferUtil.acquire(mxml);
                doc = buffer.getDocument();
            }

            if (doc == null) {
                return;
            }

            MultiTextEdit edits = new MultiTextEdit();

            FuzzyXMLParser parser = new FuzzyXMLParser();
            FuzzyXMLDocument xmldoc = parser.parse(doc.get());
            FuzzyXMLElement root = xmldoc.getDocumentElement();
            root = FuzzyXMLUtil.getFirstChild(root);
            if (root == null) {
                return;
            }
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
                    edits.addChild(new InsertEdit(
                            a.getOffset() + a.getLength(), stb.toString()));
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
            pagedefine.append(ProjectUtil.getLineDelimiterPreference(mxml
                    .getProject()));

            edits.addChild(new InsertEdit(calcInsertOffset(doc, root),
                    pagedefine.toString()));

            edits.apply(doc);

            if (buffer != null) {
                buffer.commit(new NullProgressMonitor(), true);
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        } finally {
            if (buffer != null) {
                TextFileBufferUtil.release(mxml);
            }
        }
    }

    private int calcInsertOffset(IDocument doc, FuzzyXMLElement root)
            throws Exception {
        int result = 0;
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

}
