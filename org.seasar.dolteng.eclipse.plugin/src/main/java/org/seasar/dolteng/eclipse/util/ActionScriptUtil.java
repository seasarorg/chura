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
package org.seasar.dolteng.eclipse.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import jp.aonir.fuzzyxml.FuzzyXMLDocument;
import jp.aonir.fuzzyxml.FuzzyXMLElement;
import jp.aonir.fuzzyxml.FuzzyXMLParser;

import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.framework.convention.NamingConvention;
import org.seasar.framework.util.FileOutputStreamUtil;
import org.seasar.framework.util.InputStreamReaderUtil;
import org.seasar.framework.util.InputStreamUtil;
import org.seasar.framework.util.OutputStreamUtil;

import uk.co.badgersinfoil.metaas.ActionScriptFactory;
import uk.co.badgersinfoil.metaas.ActionScriptParser;
import uk.co.badgersinfoil.metaas.ActionScriptWriter;
import uk.co.badgersinfoil.metaas.dom.ASCompilationUnit;

/**
 * @author taichi
 * 
 */
public class ActionScriptUtil {

    public static ASCompilationUnit parse(IFile script) {
        ASCompilationUnit result = null;
        InputStream in = null;
        try {
            in = script.getContents();
            ActionScriptFactory factory = new ActionScriptFactory();
            ActionScriptParser parser = factory.newParser();
            result = parser.parse(InputStreamReaderUtil.create(in, script
                    .getCharset()));
        } catch (Exception e) {
            DoltengCore.log(e);
        } finally {
            InputStreamUtil.close(in);
        }
        return result;
    }

    public static void write(ASCompilationUnit unit, IFile as)
            throws IOException {
        OutputStream out = null;
        try {
            File file = as.getLocation().toFile();
            out = FileOutputStreamUtil.create(file);
            ActionScriptFactory factory = new ActionScriptFactory();
            ActionScriptWriter asWriter = factory.newWriter();
            Writer writer = new BufferedWriter(new OutputStreamWriter(out));
            asWriter.write(writer, unit);
            writer.flush();
        } finally {
            OutputStreamUtil.close(out);
        }
    }

    public static IType findAsPairType(IFile as) {
        IType result = null;
        try {
            if (as != null) {
                IProject project = as.getProject();
                DoltengPreferences pref = DoltengCore.getPreferences(project);
                if (pref != null) {
                    NamingConvention nc = pref.getNamingConvention();

                    if (as.getName().endsWith(nc.getPageSuffix() + ".as")) {
                        ASCompilationUnit unit = parse(as);
                        StringBuffer stb = new StringBuffer();
                        stb.append(unit.getPackageName());
                        stb.append('.');
                        stb.append(nc.getImplementationPackageName());
                        stb.append('.');
                        stb.append(unit.getType().getName().replaceAll(
                                nc.getPageSuffix(), ""));
                        stb.append(nc.getServiceSuffix());
                        stb.append(nc.getImplementationSuffix());

                        IJavaProject javap = JavaCore.create(project);
                        result = javap.findType(stb.toString());
                    }
                }
            }
        } catch (Exception e) {
            DoltengCore.log(e);
        }
        return result;
    }

    public static void modifyMxml(IFile mxml, ITextEditor editor,
            MxmlMdifyHandler handler) {
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
                return;
            }

            FuzzyXMLParser parser = new FuzzyXMLParser();
            FuzzyXMLDocument xmldoc = parser.parse(doc.get());
            FuzzyXMLElement root = xmldoc.getDocumentElement();
            root = FuzzyXMLUtil.getFirstChild(root);
            if (root != null) {
                handler.modify(root, doc);
            }

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
    }

    public interface MxmlMdifyHandler {
        public void modify(FuzzyXMLElement root, IDocument document)
                throws Exception;
    }
}
