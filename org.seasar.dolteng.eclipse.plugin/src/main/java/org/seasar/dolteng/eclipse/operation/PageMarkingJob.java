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
package org.seasar.dolteng.eclipse.operation;

import java.io.BufferedInputStream;
import java.io.IOException;
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
import org.eclipse.core.resources.ResourcesPlugin;
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
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
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
        this.html = html;
        setPriority(Job.SHORT);
        setRule(this.html);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.jobs.Job#belongsTo(java.lang.Object)
     */
    @Override
    public boolean belongsTo(Object family) {
        return family == ResourcesPlugin.FAMILY_AUTO_BUILD
                || family == ResourcesPlugin.FAMILY_MANUAL_BUILD;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.resources.WorkspaceJob#runInWorkspace(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IStatus runInWorkspace(IProgressMonitor monitor) {
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
            if (pageType != null && actionType != null) {
                processMapping(monitor, actionType, pageType);
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        } finally {
            monitor.done();
        }
        return Status.OK_STATUS;
    }

    private void processMapping(IProgressMonitor monitor, IType actionType,
            IType pageType) throws IOException, CoreException,
            JavaModelException {
        final CaseInsensitiveMap fieldMap = new CaseInsensitiveMap();
        parseFields(pageType, fieldMap);
        ProgressMonitorUtil.isCanceled(monitor, 3);

        // TODO workaround ...
        // 一度型階層を作ると、JavaModelの状態が最新になる為、以前にexists == trueでも、
        // この時点で、falseになる可能性がある為。
        if (pageType.exists() && actionType.exists()) {
            final CaseInsensitiveMap methodMap = new CaseInsensitiveMap();
            parseMethods(pageType, methodMap);
            ProgressMonitorUtil.isCanceled(monitor, 3);
            parseMethods(actionType, methodMap);
            ProgressMonitorUtil.isCanceled(monitor, 3);

            FuzzyXMLParser parser = new FuzzyXMLParser();
            FuzzyXMLDocument doc = parser.parse(new BufferedInputStream(html
                    .getContents()));
            FuzzyXMLNode[] nodes = XPath.selectNodes(doc.getDocumentElement(),
                    "//@id");

            ProgressMonitorUtil.isCanceled(monitor, 1);

            for (FuzzyXMLNode node : nodes) {
                FuzzyXMLAttribute attr = (FuzzyXMLAttribute) node;
                String mappingKey = TeedaEmulator
                        .calcMappingId((FuzzyXMLElement) attr.getParentNode(),
                                attr.getValue());

                IMember mem = findMember(fieldMap, mappingKey);
                if (mem == null) {
                    mem = findMember(methodMap, mappingKey);
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
        }
        ProgressMonitorUtil.isCanceled(monitor, 1);
    }

    private IMember findMember(Map members, String mappingKey) {
        Object result = members.get(mappingKey);
        if (result == null) {
            IJavaProject javap = JavaCore.create(this.html.getProject());
            if (javap.exists()) {
                String prefix = split(javap.getOption(
                        JavaCore.CODEASSIST_FIELD_PREFIXES, true));
                String suffix = split(javap.getOption(
                        JavaCore.CODEASSIST_FIELD_SUFFIXES, true));
                if (StringUtil.isEmpty(prefix) == false) {
                    result = members.get(prefix + mappingKey);
                }
                if (result == null && StringUtil.isEmpty(suffix) == false) {
                    result = members.get(mappingKey + suffix);
                }
                if (result == null && StringUtil.isEmpty(prefix) == false
                        && StringUtil.isEmpty(suffix) == false) {
                    result = members.get(prefix + mappingKey + suffix);
                }
            }
        }
        return (IMember) result;
    }

    private String split(String s) {
        String result = null;
        if (StringUtil.isEmpty(s) == false) {
            String[] ary = s.split("[ ]*,[ ]*");
            if (0 < ary.length && ary[0] != null && 0 < ary[0].length()) {
                result = ary[0];
            }
        }
        return result;
    }

    private void parseFields(IType pageType, final CaseInsensitiveMap fieldMap) {
        TypeHierarchyFieldProcessor op = new TypeHierarchyFieldProcessor(
                pageType, new TypeHierarchyFieldProcessor.FieldHandler() {
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
    }

    private void parseMethods(IType type, final CaseInsensitiveMap methodMap) {
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

    private IResource calcPathFromOutcome(String outcome) {
        if (outcome == null) {
            return null;
        }

        String[] names = StringUtil.split(outcome, "_");
        if (names.length == 1) {
            return html.getParent().findMember(
                    outcome + "." + html.getFileExtension());
        }

        DoltengPreferences pref = DoltengCore.getPreferences(html.getProject());
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

    @SuppressWarnings("unchecked")
    private void markHtml(FuzzyXMLAttribute attr, IMember mem)
            throws CoreException {
        Map m = new HashMap();
        m.put(Constants.MARKER_ATTR_MAPPING_TYPE_NAME, mem.getDeclaringType()
                .getFullyQualifiedName());
        m.put(Constants.MARKER_ATTR_MAPPING_FIELD_NAME, mem.getElementName());
        markHtml(attr, m);
    }

    @SuppressWarnings("unchecked")
    private void markHtml(FuzzyXMLAttribute attr, Map m) throws CoreException {
        m.put(IMarker.CHAR_START, new Integer(attr.getOffset()));
        m.put(IMarker.CHAR_END,
                new Integer(attr.getOffset() + attr.getLength()));
        IMarker marker = html.createMarker(Constants.ID_PAGE_MAPPER);
        marker.setAttributes(m);
    }

    @SuppressWarnings("unchecked")
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
        DoltengPreferences pref = DoltengCore.getPreferences(project);
        NamingConvention nc = pref.getNamingConvention();
        return findType(html, nc.getPageSuffix());
    }

    private IType findActionType(IFile html) throws CoreException {
        IProject project = html.getProject();
        DoltengPreferences pref = DoltengCore.getPreferences(project);
        NamingConvention nc = pref.getNamingConvention();
        return findType(html, nc.getActionSuffix());
    }

    private IType findType(IFile html, String suffix) throws CoreException {
        IProject project = html.getProject();
        DoltengPreferences pref = DoltengCore.getPreferences(project);
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
