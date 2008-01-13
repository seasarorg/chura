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
package org.seasar.dolteng.eclipse.viewer;

import java.util.Map;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.TreeContent;
import org.seasar.dolteng.eclipse.model.impl.BasicNode;
import org.seasar.dolteng.eclipse.nls.Images;
import org.seasar.dolteng.eclipse.operation.TypeHierarchyFieldProcessor;
import org.seasar.dolteng.eclipse.util.TreeContentAcceptor;
import org.seasar.dolteng.eclipse.util.TreeContentAcceptor.TreeContentVisitor;
import org.seasar.framework.util.CaseInsensitiveMap;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
public class AsServiceMethodTreeContentProvider implements ITreeContentProvider {

    // FIXME : 未完成

    private TreeContent invisible;

    public AsServiceMethodTreeContentProvider(IType service) {
        this.invisible = new BasicNode("", null);
    }

    protected void initialize(IType service) {
        try {
            CaseInsensitiveMap fieldMap = parseFields(service);
            IMethod[] methods = service.getMethods();
            for (IMethod method : methods) {
                int flags = method.getFlags();
                if (Flags.isPublic(flags) && method.isConstructor() == false
                        && Flags.isStatic(flags) == false) {
                    String base = method.getElementName();
                    String fieldName = base.replaceFirst("[sg]et", "");
                    IMember member = findMember(service.getJavaProject(),
                            fieldMap, fieldName);
                    if (member == null || member.exists() == false) {
                        BasicNode node = new BasicNode(base, Images.PUBLIC_CO);
                        invisible.addChild(node);
                        BasicNode success = new BasicNode(base + "OnSuccess",
                                Images.PUBLIC_CO);
                        node.addChild(success);
                        BasicNode fault = new BasicNode(base + "OnFault",
                                Images.PUBLIC_CO);
                        node.addChild(fault);
                    }
                }
            }
        } catch (JavaModelException e) {
            DoltengCore.log(e);
        }
    }

    private CaseInsensitiveMap parseFields(IType service) {
        final CaseInsensitiveMap fieldMap = new CaseInsensitiveMap();
        TypeHierarchyFieldProcessor op = new TypeHierarchyFieldProcessor(
                service, new TypeHierarchyFieldProcessor.FieldHandler() {
                    public void begin() {
                    }

                    public void process(IField field) {
                        fieldMap.put(field.getElementName(), field);
                    }

                    public void done() {
                    }
                });
        try {
            op.run(null);
        } catch (Exception e) {
            DoltengCore.log(e);
        }
        return fieldMap;
    }

    private IMember findMember(IJavaProject javap, Map members,
            String mappingKey) {
        Object result = members.get(mappingKey);
        if (result == null) {
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

    public void walk(TreeContentVisitor visitor) {
        TreeContentAcceptor.accept(invisible, visitor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof TreeContent) {
            TreeContent tc = (TreeContent) parentElement;
            return tc.getChildren();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    public Object getParent(Object element) {
        if (element instanceof TreeContent) {
            TreeContent tc = (TreeContent) element;
            return tc.getParent();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    public boolean hasChildren(Object element) {
        if (element instanceof TreeContent) {
            TreeContent tc = (TreeContent) element;
            return tc.hasChildren();
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement) {
        if (inputElement instanceof TreeContent) {
            TreeContent tc = (TreeContent) inputElement;
            return tc.getChildren();
        }
        return invisible.getChildren();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {
        this.invisible.dispose();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput instanceof IType) {
            IType type = (IType) newInput;
            initialize(type);
        }
    }

}
