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
package org.seasar.dolteng.eclipse.operation;

import java.io.BufferedInputStream;
import java.util.HashMap;
import java.util.Map;

import jp.aonir.fuzzyxml.FuzzyXMLAttribute;
import jp.aonir.fuzzyxml.FuzzyXMLDocument;
import jp.aonir.fuzzyxml.FuzzyXMLNode;
import jp.aonir.fuzzyxml.FuzzyXMLParser;
import jp.aonir.fuzzyxml.XPath;
import jp.aonir.fuzzyxml.internal.FuzzyXMLUtil;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.seasar.dolteng.core.teeda.TeedaEmulator;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.dolteng.eclipse.util.DoltengProjectUtil;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.CaseInsensitiveMap;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class PageMarkingJob extends WorkspaceJob {

    private IFile html;

    public PageMarkingJob(IFile html) {
        super(Messages.bind(Messages.PROCESS_MAPPING, html.getName()));
        setPriority(Job.SHORT);
        this.html = html;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.WorkspaceJob#runInWorkspace(org.eclipse.core.runtime.IProgressMonitor)
     */
    public IStatus runInWorkspace(IProgressMonitor monitor)
            throws CoreException {
        monitor.beginTask(Messages.bind(Messages.PROCESS_MAPPING, html
                .getName()), 10);
        try {
            if (html.exists()) {
                html.deleteMarkers(Constants.ID_PAGE_MAPPER, true,
                        IResource.DEPTH_ZERO);
            }
            monitor.worked(1);

            IType actionType = findActionType(html);
            IType pageType = findPageType(html);
            if (actionType == null || actionType.exists() == false) {
                actionType = pageType;
            }
            if (pageType != null) {
                IResource pageJava = pageType.getResource();
                IResource actionJava = actionType.getResource();
                pageJava.deleteMarkers(Constants.ID_HTML_MAPPER, true,
                        IResource.DEPTH_ZERO);
                actionJava.deleteMarkers(Constants.ID_HTML_MAPPER, true,
                        IResource.DEPTH_ZERO);
                monitor.worked(2);
                final CaseInsensitiveMap fieldMap = new CaseInsensitiveMap();
                TypeHierarchyFieldProcessor op = new TypeHierarchyFieldProcessor(
                        pageType,
                        new TypeHierarchyFieldProcessor.FieldHandler() {
                            public void begin() {
                            }

                            public void process(IField field) {
                                fieldMap.put(field.getElementName(), field);
                            }

                            public void done() {
                            }
                        });
                op.run(null);
                monitor.worked(6);

                final CaseInsensitiveMap methodMap = new CaseInsensitiveMap();
                IMethod[] methods = actionType.getMethods();
                for (int i = 0; i < methods.length; i++) {
                    IMethod method = methods[i];
                    methodMap.put(method.getElementName(), method);
                }

                FuzzyXMLParser parser = new FuzzyXMLParser();
                FuzzyXMLDocument doc = parser.parse(new BufferedInputStream(
                        html.getContents()));
                FuzzyXMLNode[] nodes = XPath.selectNodes(doc
                        .getDocumentElement(), "//html//@id");
                for (int i = 0; i < nodes.length; i++) {
                    FuzzyXMLAttribute attr = (FuzzyXMLAttribute) nodes[i];

                    IResource resource = pageJava;
                    IMember mem = (IMember) fieldMap.get(attr.getValue());
                    if (mem == null) {
                        mem = (IMember) methodMap.get(attr.getValue());
                        resource = actionJava;
                    }

                    if (mem != null) {
                        markHtml(attr, mem);
                        markJava(attr, resource, mem);
                    } else if (TeedaEmulator.GO_PREFIX.matcher(attr.getValue())
                            .matches()) {
                        String outcome = StringUtil.decapitalize(attr
                                .getValue().substring(2));
                        IResource goHtml = calcPathFromOutcome(outcome);
                        if (goHtml != null && goHtml.exists()
                                && goHtml.getType() == IResource.FILE) {
                            markHtml(attr);
                        }
                    }
                }
                monitor.worked(1);
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        } finally {
            monitor.done();
        }
        return Status.OK_STATUS;
    }

    /**
     * @return
     * @throws CoreException
     * @see org.seasar.teeda.extension.html.impl.HtmlNavigationHandler#calcPathFromOutcome(java.lang.String,
     *      java.lang.String)
     */
    private IResource calcPathFromOutcome(String outcome) throws CoreException {
        if (outcome == null) {
            return null;
        }

        String[] names = StringUtil.split(outcome, "_");
        if (names.length == 1) {
            return html.getParent().findMember(
                    outcome + "." + html.getFileExtension());
        }

        DoltengProjectPreferences pref = DoltengCore.getPreferences(html
                .getProject());
        if (pref == null) {
            return null;
        }
        NamingConvention nc = pref.getNamingConvention();
        String view = nc.getViewRootPath().substring(1);
        IContainer c = html.getParent();
        while (view.equalsIgnoreCase(c.getName()) == false) {
            c = c.getParent();
            if (c.getType() == IResource.PROJECT) {
                return null;
            }
        }
        IPath path = new Path(names[0]);
        for (int i = 1; i < names.length; i++) {
            path = path.append(StringUtil.decapitalize(names[i]));
        }
        path = path.addFileExtension(html.getFileExtension());
        return c.findMember(path);
    }

    private void markHtml(FuzzyXMLAttribute attr) throws CoreException {
        markHtml(attr, new HashMap());
    }

    private void markHtml(FuzzyXMLAttribute attr, IMember mem)
            throws CoreException {
        Map m = new HashMap();
        m.put(Constants.MARKER_ATTR_MAPPING_TYPE_NAME, mem.getDeclaringType()
                .getFullyQualifiedName());
        m.put(Constants.MARKER_ATTR_MAPPING_FIELD_NAME, mem.getElementName());
        markHtml(attr, m);
    }

    private void markHtml(FuzzyXMLAttribute attr, Map m) throws CoreException {
        m.put(IMarker.CHAR_START, new Integer(attr.getOffset()));
        m.put(IMarker.CHAR_END,
                new Integer(attr.getOffset() + attr.getLength()));
        IMarker marker = html.createMarker(Constants.ID_PAGE_MAPPER);
        marker.setAttributes(m);
    }

    private void markJava(FuzzyXMLAttribute attr, IResource resource,
            IMember mem) throws JavaModelException, CoreException {
        Map m = new HashMap();
        ISourceRange renge = mem.getSourceRange();
        m.put(IMarker.CHAR_START, new Integer(renge.getOffset()));
        m.put(IMarker.CHAR_END, new Integer(renge.getOffset()
                + renge.getLength()));
        m.put(Constants.MARKER_ATTR_MAPPING_HTML_PATH, html.getFullPath()
                .toString());
        m.put(Constants.MARKER_ATTR_MAPPING_HTML_ID, mem.getElementName());
        m.put(Constants.MARKER_ATTR_MAPPING_ELEMENT, FuzzyXMLUtil.escape(attr
                .getParentNode().toXMLString()));
        IMarker marker = resource.createMarker(Constants.ID_HTML_MAPPER);
        marker.setAttributes(m);
    }

    private IType findPageType(IFile html) throws CoreException {
        IProject project = html.getProject();
        DoltengProjectPreferences pref = DoltengCore.getPreferences(project);
        NamingConvention nc = pref.getNamingConvention();
        return findType(html, nc.getPageSuffix());
    }

    private IType findActionType(IFile html) throws CoreException {
        IProject project = html.getProject();
        DoltengProjectPreferences pref = DoltengCore.getPreferences(project);
        NamingConvention nc = pref.getNamingConvention();
        return findType(html, nc.getActionSuffix());
    }

    private IType findType(IFile html, String suffix) throws CoreException {
        IProject project = html.getProject();
        DoltengProjectPreferences pref = DoltengCore.getPreferences(project);
        String pkgName = DoltengProjectUtil.calculatePagePkg(html, pref);
        String fqName = pkgName + "." + getOpenTypeName(html, suffix);
        IJavaProject javap = JavaCore.create(project);
        return javap.findType(fqName);
    }

    private String getOpenTypeName(IFile html, String suffix) {
        String name = html.getName();
        name = name.substring(0, name.lastIndexOf('.'));
        name = StringUtil.capitalize(name) + suffix;
        return name;
    }

}
