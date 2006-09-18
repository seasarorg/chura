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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.preferences.DoltengProjectPreferences;
import org.seasar.dolteng.eclipse.util.DoltengProjectUtil;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.CaseInsensitiveSet;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class PageMarkingJob extends WorkspaceJob {

    private IFile html;

    public PageMarkingJob(IFile html) {
        super("Mapping ... " + html.getName());
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
        monitor.beginTask("Process Mapping ....", 10);
        try {
            html.deleteMarkers(Constants.ID_PAGE_MAPPER, true,
                    IResource.DEPTH_ZERO);
            monitor.worked(1);

            IType type = findPageType(html);
            final CaseInsensitiveSet set = new CaseInsensitiveSet();
            TypeHierarchyFieldProcessor op = new TypeHierarchyFieldProcessor(
                    type, new TypeHierarchyFieldProcessor.FieldHandler() {
                        public void begin() {
                        }

                        public void process(IField field) {
                            set.add(field.getElementName());
                        }

                        public void done() {
                        }
                    });
            op.run(null);
            FuzzyXMLParser parser = new FuzzyXMLParser();
            FuzzyXMLDocument doc = parser.parse(new BufferedInputStream(html
                    .getContents()));
            FuzzyXMLNode[] nodes = XPath.selectNodes(doc.getDocumentElement(),
                    "//html//@id");
            for (int i = 0; i < nodes.length; i++) {
                FuzzyXMLAttribute attr = (FuzzyXMLAttribute) nodes[i];
                if (set.contains(attr.getValue())) {
                    Map m = new HashMap();
                    m.put(IMarker.CHAR_START, new Integer(attr.getOffset()));
                    m.put(IMarker.CHAR_END, new Integer(attr.getOffset()
                            + attr.getLength()));
                    IMarker marker = html
                            .createMarker(Constants.ID_PAGE_MAPPER);
                    marker.setAttributes(m);
                }
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        } finally {
            monitor.done();
        }
        return Status.OK_STATUS;
    }

    private IType findPageType(IFile html) throws CoreException {
        IProject project = html.getProject();
        DoltengProjectPreferences pref = DoltengCore.getPreferences(project);
        NamingConvention nc = pref.getNamingConvention();
        String pkgName = DoltengProjectUtil.calculatePagePkg(html, pref);
        String fqName = pkgName + "." + getOpenTypeName(html, nc);
        IJavaProject javap = JavaCore.create(project);
        return javap.findType(fqName);
    }

    private String getOpenTypeName(IFile html, NamingConvention nc) {
        String name = html.getName();
        name = name.substring(0, name.lastIndexOf('.'));
        name = StringUtil.capitalize(name) + nc.getPageSuffix();
        return name;
    }

}
