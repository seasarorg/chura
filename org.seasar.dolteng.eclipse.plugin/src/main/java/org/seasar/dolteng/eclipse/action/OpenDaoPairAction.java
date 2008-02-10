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

import java.util.regex.Pattern;

import jp.aonir.fuzzyxml.FuzzyXMLAttribute;
import jp.aonir.fuzzyxml.FuzzyXMLDocument;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLNode;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
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
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.util.FuzzyXMLUtil;
import org.seasar.dolteng.eclipse.util.ResourcesUtil;
import org.seasar.dolteng.eclipse.util.TextEditorUtil;
import org.seasar.dolteng.eclipse.util.TypeUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.dolteng.eclipse.util.ResourcesUtil.FindingHandler;
import org.seasar.dolteng.eclipse.wizard.NewOrmXmlWizard;
import org.seasar.dolteng.eclipse.wizard.NewSqlWizard;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class OpenDaoPairAction extends AbstractWorkbenchWindowActionDelegate {

    private static final Pattern suffix = Pattern.compile(
            ".*((Orm\\.xml)|\\.sql)", Pattern.CASE_INSENSITIVE);

    /**
     * 
     */
    public OpenDaoPairAction() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.action.AbstractEditorActionDelegate#processJava(org.eclipse.core.resources.IProject,
     *      org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences,
     *      org.eclipse.jdt.core.IJavaElement)
     */
    @Override
    protected void processJava(IProject project, DoltengPreferences pref,
            IJavaElement element) throws Exception {
        if (element instanceof ICompilationUnit) {
            ICompilationUnit unit = (ICompilationUnit) element;
            IType type = unit.findPrimaryType();
            String name = type.getFullyQualifiedName();
            NamingConvention nc = pref.getNamingConvention();
            if (nc.isTargetClassName(name)) {
                boolean willbeOpen = false;
                String en = type.getElementName();
                String pkg = type.getPackageFragment().getElementName();
                String entityName = "";
                willbeOpen = nc.isTargetClassName(name, nc.getDaoSuffix());
                if (willbeOpen) {
                    entityName = en.substring(0, en.length() - 3);
                } else {
                    willbeOpen = pkg.endsWith(nc.getEntityPackageName());
                    if (willbeOpen) {
                        entityName = en;
                    }
                }
                if (willbeOpen) {
                    IPath resourcePath = new Path(pkg.replace('.', '/'));
                    IMethod method = null;
                    IJavaElement elem = getSelectionElement();
                    if (elem instanceof IMethod) {
                        method = (IMethod) elem;
                    }
                    if (method != null) {
                        String sql = type.getElementName() + "_"
                                + method.getElementName();
                        Pattern sqlPtn = Pattern.compile(sql + ".*\\.sql",
                                Pattern.CASE_INSENSITIVE);
                        if (ResourcesUtil.findDir(project, resourcePath,
                                sqlPtn, new DefaultFindingHandler()) == false) {
                            if (Constants.DAO_TYPE_KUINADAO.equals(pref
                                    .getDaoType()) == false) {
                                NewSqlWizard wiz = new NewSqlWizard();
                                wiz.setContainerFullPath(pref
                                        .getDefaultResourcePath().append(
                                                resourcePath));
                                wiz.setFileName(sql + ".sql");
                                WorkbenchUtil.startWizard(wiz);
                                return;
                            }
                        }
                    }

                    if (Constants.DAO_TYPE_KUINADAO.equals(pref.getDaoType())) {
                        Pattern ormXml = Pattern.compile(entityName
                                + "Orm\\.xml", Pattern.CASE_INSENSITIVE);
                        FindingHandler handler = new XmlFindingHandler(
                                entityName, method);
                        if (ResourcesUtil.findDir(project,
                                new Path("META-INF"), ormXml, handler) == false) {
                            if (ResourcesUtil.findDir(project, resourcePath,
                                    ormXml, handler) == false) {
                                NewOrmXmlWizard wiz = new NewOrmXmlWizard();
                                wiz.setContainerFullPath(pref
                                        .getOrmXmlOutputPath());
                                wiz.setEntityName(entityName);
                                WorkbenchUtil.startWizard(wiz);
                            }
                        }
                    }
                }
            }
        }
    }

    private static class DefaultFindingHandler implements FindingHandler {
        public void handle(IFile file) {
            WorkbenchUtil.openResource(file);
        }
    }

    private static class XmlFindingHandler implements FindingHandler {
        private String entityName;

        private IMethod method;

        public XmlFindingHandler(String entityName, IMethod method) {
            this.entityName = entityName;
            this.method = method;
        }

        public void handle(IFile file) {
            if (method == null) {
                return;
            }

            IEditorPart ep = WorkbenchUtil.openResource(file);
            ITextEditor editor = TextEditorUtil.toTextEditor(ep);
            if (editor == null) {
                return;
            }

            try {
                String query = "//named-query[@name=\"" + entityName + "."
                        + method.getElementName() + "\"]";
                FuzzyXMLNode[] list = FuzzyXMLUtil.selectNodes(file, query);
                if (list != null && 0 < list.length) {
                    editor.selectAndReveal(list[0].getOffset(), 0);
                }
            } catch (Exception e) {
                DoltengCore.log(e);
            }

        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.eclipse.action.AbstractEditorActionDelegate#processResource(org.eclipse.core.resources.IProject,
     *      org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences,
     *      org.eclipse.core.resources.IResource)
     */
    @Override
    protected void processResource(IProject project, DoltengPreferences pref,
            IResource resource) throws Exception {
        if (suffix.matcher(resource.getName()).matches()) {
            NamingConvention nc = pref.getNamingConvention();
            IJavaProject javap = JavaCore.create(project);
            String name = resource.getName();
            name = name.replaceAll("((Orm\\.xml)|([dD][aA][oO])?_.*\\.sql)$",
                    "");
            name = StringUtil.capitalize(name);
            String methodName = calcSelectionMethod(resource);
            for (String pkgName : nc.getRootPackageNames()) {
                String typeName = getOpenTypeName(pkgName, name, nc);
                IType type = javap.findType(typeName);
                if (type != null && type.exists()) {
                    IMember m = TypeUtil.getMember(type, methodName);
                    IEditorPart editor = JavaUI.openInEditor(type);
                    ITextEditor te = TextEditorUtil.toTextEditor(editor);
                    if (te != null && m != null) {
                        ISourceRange sr = m.getNameRange();
                        te.selectAndReveal(sr.getOffset(), sr.getLength());
                    }
                    break;
                }
            }
        }
    }

    private String calcSelectionMethod(IResource resource) throws Exception {
        String name = resource.getName();
        int index = name.indexOf('_');
        if (-1 < index) {
            return name.substring(index + 1, name.lastIndexOf('.'));
        }
        ITextEditor txtEditor = TextEditorUtil.toTextEditor(WorkbenchUtil
                .getActiveEditor());
        if (resource instanceof IFile && txtEditor != null) {
            IFile file = (IFile) resource;
            ISelectionProvider provider = txtEditor.getSelectionProvider();
            if (provider != null) {
                ISelection selection = provider.getSelection();
                if (selection instanceof ITextSelection) {
                    ITextSelection ts = (ITextSelection) selection;
                    FuzzyXMLDocument doc = FuzzyXMLUtil.parse(file);
                    FuzzyXMLElement elem = doc.getElementByOffset(ts
                            .getOffset());
                    FuzzyXMLNode current = elem;
                    while (true) {
                        String elemName = elem.getName();
                        if (elemName.equalsIgnoreCase("named-query")) {
                            FuzzyXMLAttribute attr = elem
                                    .getAttributeNode("name");
                            if (attr != null) {
                                String value = attr.getValue().replaceAll("\"",
                                        "");
                                int i = value.indexOf('.');
                                if (-1 < i) {
                                    return value.substring(i + 1);
                                }
                            }
                        }
                        if (elemName.equalsIgnoreCase("entity-mappings")) {
                            break;
                        }
                        FuzzyXMLNode node = current.getParentNode();
                        if (node == null) {
                            break;
                        } else if (node instanceof FuzzyXMLElement) {
                            elem = (FuzzyXMLElement) node;
                        } else {
                            current = node;
                        }
                    }
                }
            }
        }
        return null;
    }

    protected String getOpenTypeName(String root, String entityName,
            NamingConvention nc) {
        String result = root + "." + nc.getDaoPackageName() + "." + entityName
                + nc.getDaoSuffix();
        return result;
    }
}