package org.seasar.dolteng.eclipse.action;

import jp.aonir.fuzzyxml.FuzzyXMLAttribute;
import jp.aonir.fuzzyxml.FuzzyXMLDocument;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLNode;
import jp.aonir.fuzzyxml.FuzzyXMLParser;

import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.TextFileBufferUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.framework.convention.NamingConvention;

public class AddServiceAction extends AbstractEditorActionDelegate {

    public AddServiceAction() {
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
        if (resource.getType() != IResource.FILE) {
            return;
        }

        ITextFileBuffer buffer = null;
        IDocument doc = null;

        try {
            IDocumentProvider provider = this.txtEditor.getDocumentProvider();
            if (provider != null) {
                doc = provider.getDocument(this.txtEditor.getEditorInput());
            }

            if (doc == null) {
                buffer = TextFileBufferUtil.acquire(resource);
                doc = buffer.getDocument();
            }

            if (doc == null) {
                return;
            }

            MultiTextEdit edits = new MultiTextEdit();

            FuzzyXMLParser parser = new FuzzyXMLParser();
            FuzzyXMLDocument xmldoc = parser.parse(doc.get());
            FuzzyXMLElement root = xmldoc.getDocumentElement();
            root = getFirstChild(root);
            if (root == null) {
                return;
            }

            SelectionDialog dialog = JavaUI
                    .createTypeDialog(
                            Display.getCurrent().getActiveShell(),
                            WorkbenchUtil.getWorkbenchWindow(),
                            resource.getProject(),
                            IJavaElementSearchConstants.CONSIDER_CLASSES_AND_INTERFACES,
                            false);
            dialog.setTitle(Messages.SELECT_FLEX2_SERVICE);
            if (dialog.open() != Window.OK) {
                return;
            }
            Object[] results = dialog.getResult();
            if (results == null || results.length < 1) {
                return;
            }
            IType selected = (IType) results[0];
            String fqn = selected.getFullyQualifiedName();
            NamingConvention nc = pref.getNamingConvention();
            String componentName = nc.fromClassNameToComponentName(fqn);

            if (root.hasAttribute("xmlns:seasar") == false) {
                FuzzyXMLAttribute[] attrs = root.getAttributes();
                if (attrs != null && 0 < attrs.length) {
                    FuzzyXMLAttribute a = attrs[attrs.length - 1];
                    edits
                            .addChild(new InsertEdit(a.getOffset()
                                    + a.getLength(),
                                    " xmlns:seasar=\"http://www.seasar.org/s2flex2/mxml\""));
                }
            }

            StringBuffer remoting = new StringBuffer();
            remoting.append("<seasar:S2Flex2Service id=\"service\"");
            remoting.append(" destination=\"");
            remoting.append(componentName);
            remoting.append("\" showBusyCursor=\"true\"");
            remoting.append("/>");
            remoting.append(ProjectUtil.getLineDelimiterPreference(resource
                    .getProject()));

            edits.addChild(new InsertEdit(calcInsertOffset(doc, root), remoting
                    .toString()));

            edits.apply(doc);

            if (buffer != null) {
                buffer.commit(new NullProgressMonitor(), true);
            }
        } finally {
            if (buffer != null) {
                TextFileBufferUtil.release(resource);
            }
        }
    }

    private FuzzyXMLElement getFirstChild(FuzzyXMLElement element) {
        FuzzyXMLNode[] kids = element.getChildren();
        for (int i = 0; i < kids.length; i++) {
            FuzzyXMLNode n = kids[i];
            if (n instanceof FuzzyXMLElement) {
                return (FuzzyXMLElement) n;
            }
        }
        return null;
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

        FuzzyXMLElement kid = getFirstChild(root);
        if (kid != null) {
            int line = doc.getLineOfOffset(kid.getOffset());
            return doc.getLineOffset(line);
        }
        return result;
    }
}
