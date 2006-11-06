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

import java.util.regex.Pattern;

import jp.aonir.fuzzyxml.FuzzyXMLAttribute;
import jp.aonir.fuzzyxml.FuzzyXMLDocument;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLNode;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.seasar.dolteng.core.teeda.TeedaEmulator;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.dolteng.eclipse.util.DoltengProjectUtil;
import org.seasar.dolteng.eclipse.util.FuzzyXMLUtil;
import org.seasar.dolteng.eclipse.util.TextEditorUtil;
import org.seasar.dolteng.eclipse.util.TypeUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.dolteng.eclipse.wizard.NewPageWizard;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class OpenPagePairAction extends AbstractEditorActionDelegate {

    private static final Pattern propertyPtn = Pattern.compile("(get|set).*");

    protected void processJava(IProject project,
            DoltengProjectPreferences pref, IJavaElement element)
            throws Exception {
        if (element instanceof ICompilationUnit) {
            IFile file = DoltengProjectUtil.findHtmlByJava(project, pref,
                    (ICompilationUnit) element);
            ITextEditor editor = TextEditorUtil.toTextEditor(WorkbenchUtil
                    .openResource(file));
            if (editor != null) {
                IJavaElement e = getSelectionElement();
                String id = "";
                if (e instanceof IField) {
                    IField f = (IField) e;
                    id = f.getElementName();
                } else if (e instanceof IMethod) {
                    IMethod m = (IMethod) e;
                    String en = m.getElementName();
                    if (propertyPtn.matcher(en).matches()) {
                        id = en.replaceAll("^(get|set)", "");
                    }
                }
                if (StringUtil.isEmpty(id) == false) {
                    FuzzyXMLNode[] nodes = FuzzyXMLUtil.selectNodes(file,
                            "//html//*[starts-with(@id,\"" + id + "\")]");
                    for (int i = 0; i < nodes.length; i++) {
                        if (nodes[i] instanceof FuzzyXMLElement) {
                            FuzzyXMLElement elem = (FuzzyXMLElement) nodes[i];
                            if (TeedaEmulator.isNotSkipId(elem, id)) {
                                editor.selectAndReveal(nodes[i].getOffset(),
                                        nodes[i].getLength());
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    protected void processResource(IProject project,
            DoltengProjectPreferences pref, IResource resource) {
        try {
            if (resource instanceof IFile) {
                IFile f = (IFile) resource;
                if (DoltengProjectUtil.isInViewPkg(f)) {
                    NamingConvention nc = pref.getNamingConvention();
                    String memberName = calcSelectionProperty(resource);
                    String[] pkgNames = DoltengProjectUtil.calculatePagePkg(
                            resource, pref);
                    for (int i = 0; i < pkgNames.length; i++) {
                        String fqName = pkgNames[i] + "."
                                + getOpenTypeName(resource, nc);
                        IJavaProject javap = JavaCore.create(project);
                        IType type = javap.findType(fqName);
                        if (type != null && type.exists()) {
                            IMember m = TypeUtil.getMember(type, memberName);
                            IEditorPart part = JavaUI.openInEditor(type);
                            ITextEditor editor = TextEditorUtil
                                    .toTextEditor(part);
                            if (editor != null && m != null) {
                                ISourceRange sr = m.getNameRange();
                                editor.selectAndReveal(sr.getOffset(), sr
                                        .getLength());
                            }
                            return;
                        }
                    }
                    NewPageWizard wiz = new NewPageWizard();
                    wiz.init(f);
                    WorkbenchUtil.startWizard(wiz);
                }
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        }
    }

    private String calcSelectionProperty(IResource resource) throws Exception {
        String result = null;
        if (resource instanceof IFile && this.txtEditor != null) {
            IFile file = (IFile) resource;
            ISelectionProvider provider = this.txtEditor.getSelectionProvider();
            if (provider != null) {
                ISelection selection = provider.getSelection();
                if (selection instanceof ITextSelection) {
                    ITextSelection ts = (ITextSelection) selection;
                    FuzzyXMLDocument doc = FuzzyXMLUtil.parse(file);
                    FuzzyXMLElement elem = doc.getElementByOffset(ts
                            .getOffset());
                    FuzzyXMLAttribute attr = elem.getAttributeNode("id");
                    if (attr != null) {
                        result = TeedaEmulator.calcMappingId(elem, attr
                                .getValue());
                    }
                }
            }
        }
        return result;
    }

    protected String getOpenTypeName(IResource html, NamingConvention nc) {
        String name = html.getName();
        name = name.substring(0, name.lastIndexOf('.'));
        return getOpenTypeName(StringUtil.capitalize(name), nc);
    }

    protected String getOpenTypeName(String baseName, NamingConvention nc) {
        return baseName + nc.getPageSuffix();

    }
}