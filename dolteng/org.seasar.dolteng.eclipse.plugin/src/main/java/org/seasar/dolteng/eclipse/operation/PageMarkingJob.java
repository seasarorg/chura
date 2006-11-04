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
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import jp.aonir.fuzzyxml.FuzzyXMLAttribute;
import jp.aonir.fuzzyxml.FuzzyXMLDocument;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
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
import org.seasar.dolteng.eclipse.util.ProgressMonitorUtil;
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
                .getName()), 13);
        try {
            if (html.exists()) {
                html.deleteMarkers(Constants.ID_PAGE_MAPPER, true,
                        IResource.DEPTH_ZERO);
            }
            ProgressMonitorUtil.isCanceled(monitor, 1);

            IType actionType = findActionType(html);
            IType pageType = findPageType(html);
            if (actionType == null || actionType.exists() == false) {
                actionType = pageType;
            }
            if (pageType != null) {
                final CaseInsensitiveMap fieldMap = new CaseInsensitiveMap();
                TypeHierarchyFieldProcessor op = new TypeHierarchyFieldProcessor(
                        pageType,
                        new TypeHierarchyFieldProcessor.FieldHandler() {
                            public void begin() {
                            }

                            public void process(IField field) {
                                try {
                                    removeMarkers(field.getResource());
                                    fieldMap.put(field.getElementName(), field);
                                } catch (CoreException e) {
                                    DoltengCore.log(e);
                                }
                            }

                            public void done() {
                            }
                        });
                op.run(null);
                ProgressMonitorUtil.isCanceled(monitor, 3);

                final CaseInsensitiveMap methodMap = new CaseInsensitiveMap();
                parseMethods(pageType, methodMap);
                ProgressMonitorUtil.isCanceled(monitor, 3);
                parseMethods(actionType, methodMap);
                ProgressMonitorUtil.isCanceled(monitor, 3);

                FuzzyXMLParser parser = new FuzzyXMLParser();
                FuzzyXMLDocument doc = parser.parse(new BufferedInputStream(
                        html.getContents()));
                FuzzyXMLNode[] nodes = XPath.selectNodes(doc
                        .getDocumentElement(), "//html//@id");

                ProgressMonitorUtil.isCanceled(monitor, 1);

                for (int i = 0; i < nodes.length; i++) {
                    FuzzyXMLAttribute attr = (FuzzyXMLAttribute) nodes[i];
                    String mappingKey = TeedaEmulator.calcMappingId(
                            (FuzzyXMLElement) attr.getParentNode(), attr
                                    .getValue());

                    IMember mem = (IMember) fieldMap.get(mappingKey);
                    if (mem == null) {
                        mem = (IMember) methodMap.get(mappingKey);
                    }

                    if (mem != null) {
                        markHtml(attr, mem);
                        markJava(attr, mem);
                    } else if (TeedaEmulator.EXIST_TO_FILE_PREFIX.matcher(
                            attr.getValue()).matches()) {
                        String outcome = TeedaEmulator
                                .toOutComeFileName(mappingKey);
                        IResource goHtml = calcPathFromOutcome(outcome);
                        if (goHtml != null && goHtml.exists()
                                && goHtml.getType() == IResource.FILE) {
                            markHtml(attr);
                        }
                    }
                }
                ProgressMonitorUtil.isCanceled(monitor, 1);
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        } finally {
            monitor.done();
        }
        return Status.OK_STATUS;
    }

    /**
     * @param type
     * @param methodMap
     * @throws InvocationTargetException
     * @throws InterruptedException
     */
    private void parseMethods(IType type, final CaseInsensitiveMap methodMap)
            throws InvocationTargetException, InterruptedException {
        TypeHierarchyMethodProcessor methodOp = new TypeHierarchyMethodProcessor(
                type, new TypeHierarchyMethodProcessor.MethodHandler() {
                    public void begin() {
                    }

                    public void process(IMethod method) {
                        try {
                            removeMarkers(method.getResource());
                            methodMap.put(method.getElementName(), method);
                        } catch (CoreException e) {
                            DoltengCore.log(e);
                        }
                    }

                    public void done() {
                    }
                });
        methodOp.run(null);
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

    private void markJava(FuzzyXMLAttribute attr, IMember mem)
            throws JavaModelException, CoreException {
        Map m = new HashMap();
        ISourceRange renge = mem.getNameRange();
        m.put(IMarker.CHAR_START, new Integer(renge.getOffset()));
        m.put(IMarker.CHAR_END, new Integer(renge.getOffset()
                + renge.getLength()));
        m.put(Constants.MARKER_ATTR_MAPPING_HTML_PATH, html.getFullPath()
                .toString());
        m.put(Constants.MARKER_ATTR_MAPPING_HTML_ID, mem.getElementName());
        m.put(Constants.MARKER_ATTR_MAPPING_ELEMENT, FuzzyXMLUtil.escape(attr
                .getParentNode().toXMLString()));
        IMarker marker = mem.getCompilationUnit().getResource().createMarker(
                Constants.ID_HTML_MAPPER);
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
        String[] pkgNames = DoltengProjectUtil.calculatePagePkg(html, pref);
        for (int i = 0; i < pkgNames.length; i++) {
            String fqName = pkgNames[i] + "." + getOpenTypeName(html, suffix);
            IJavaProject javap = JavaCore.create(project);
            IType type = javap.findType(fqName);
            if (type != null && type.exists()) {
                return type;
            }
        }
        return null;
    }

    private String getOpenTypeName(IFile html, String suffix) {
        String name = html.getName();
        name = name.substring(0, name.lastIndexOf('.'));
        name = StringUtil.capitalize(name) + suffix;
        return name;
    }

    private void removeMarkers(IResource r) throws CoreException {
        IMarker[] markers = r.findMarkers(Constants.ID_HTML_MAPPER, true,
                IResource.DEPTH_ZERO);
        String path = html.getFullPath().toString();
        for (int i = 0; i < markers.length; i++) {
            IMarker marker = markers[i];
            String p = marker.getAttribute(
                    Constants.MARKER_ATTR_MAPPING_HTML_PATH, "");
            if (path.equals(p)) {
                marker.delete();
            }
        }
    }

}
