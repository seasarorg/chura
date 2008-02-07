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
package org.seasar.dolteng.eclipse.wizard;

import java.util.List;

import jp.aonir.fuzzyxml.FuzzyXMLDocument;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLParser;

import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.MxBindingMappingRow;
import org.seasar.dolteng.eclipse.util.FuzzyXMLUtil;
import org.seasar.dolteng.eclipse.util.ProjectUtil;
import org.seasar.dolteng.eclipse.util.TextFileBufferUtil;

/**
 * @author taichi
 * 
 */
public class AddBindingWizard extends Wizard {

    private IFile mxml = null;

    private ITextEditor editor = null;

    private AddBindingWizardPage mainPage;

    public AddBindingWizard() {
    }

    public void initialize(IFile mxml, ITextEditor editor) {
        this.mxml = mxml;
        this.editor = editor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        mainPage = new AddBindingWizardPage();
        mainPage.setMxml(mxml);
        addPage(mainPage);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        ITextFileBuffer buffer = null;
        IDocument doc = null;

        try {
            IDocumentProvider provider = editor.getDocumentProvider();
            if (provider != null) {
                doc = provider.getDocument(editor.getEditorInput());
            }
            if (doc == null) {
                buffer = TextFileBufferUtil.acquire(mxml);
                doc = buffer.getDocument();
            }
            if (doc == null) {
                return false;
            }

            FuzzyXMLParser parser = new FuzzyXMLParser();
            FuzzyXMLDocument xmldoc = parser.parse(doc.get());
            FuzzyXMLElement root = xmldoc.getDocumentElement();
            root = FuzzyXMLUtil.getFirstChild(root);
            if (root == null) {
                return false;
            }

            MultiTextEdit edits = new MultiTextEdit();

            // タグのインサート
            List<MxBindingMappingRow> rows = mainPage.getMappingRows();
            String delim = ProjectUtil.getLineDelimiterPreference(mxml
                    .getProject());

            for (MxBindingMappingRow row : rows) {
                if (row.isGenerate()) {
                    edits.addChild(new InsertEdit(calcInsertOffset(doc, root),
                            row.toXml() + delim));
                }
            }

            edits.apply(doc);
            if (buffer != null) {
                buffer.commit(new NullProgressMonitor(), true);
            }
            return true;
        } catch (Exception e) {
            DoltengCore.log(e);
        } finally {
            if (buffer != null) {
                TextFileBufferUtil.release(mxml);
            }
        }
        return false;
    }

    private int calcInsertOffset(IDocument doc, FuzzyXMLElement root)
            throws Exception {
        int result = 0;
        if (editor != null) {
            ISelectionProvider sp = editor.getSelectionProvider();
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
