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
package org.seasar.dolteng.eclipse.wizard;

import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.util.TextFileBufferUtil;

/**
 * @author taichi
 * 
 */
public class AddBindingWizard extends Wizard {

    private IFile mxml = null;

    private ITextEditor editor = null;

    public AddBindingWizard() {
    }

    public void initialize(IFile mxml, ITextEditor editor) {
        this.mxml = mxml;
        this.editor = editor;
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

            MultiTextEdit edits = new MultiTextEdit();

            // mxmlのパース。id属性一覧を抜き出す。

            // DTOの選択。デフォルト値は、Pageクラスを生成した時の、PersistantProp

            // マッピング一覧の表示。項目は、「チェック」、「mxmlのid属性」、「DTOの変数宣言」

            // チェックされたペアのタグ生成。
            // <mx:Binding source="tiAaa.text" destination="hoge"/>
            // タグのインサート

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
        return false;
    }

}
