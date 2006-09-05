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
package org.seasar.dolteng.eclipse.util;

import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.seasar.dolteng.core.entity.impl.BasicFieldMetaData;
import org.seasar.dolteng.core.entity.impl.BasicMethodMetaData;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.impl.PageClassColumn;
import org.seasar.framework.util.InputStreamUtil;
import org.seasar.framework.util.StringUtil;
import org.seasar.teeda.core.JsfConstants;
import org.seasar.teeda.extension.ExtensionConstants;
import org.seasar.teeda.extension.html.DocumentNode;
import org.seasar.teeda.extension.html.ElementNode;
import org.seasar.teeda.extension.html.HtmlNode;
import org.seasar.teeda.extension.html.HtmlParser;
import org.seasar.teeda.extension.html.impl.HtmlParserImpl;

/**
 * @author taichi
 * 
 */
public class HtmlNodeAnalyzer {

    private static final Pattern skipIds = Pattern.compile(
            JsfConstants.MESSAGES + "|" + ".*" + ExtensionConstants.FORM_SUFFIX
                    + "|" + ".*" + ExtensionConstants.MESSAGE_SUFFIX + "|"
                    + "|" + ExtensionConstants.GO_PREFIX + ".*" + "|"
                    + ExtensionConstants.MESSAGE_SUFFIX + ".*",
            Pattern.CASE_INSENSITIVE);

    private IFile htmlfile;

    private List actionMethods = new ArrayList();

    private Map pageFields = new HashMap();

    public HtmlNodeAnalyzer(IFile htmlfile) {
        super();
        this.htmlfile = htmlfile;
    }

    public void analyze() {
        InputStream in = null;
        try {
            HtmlParser parser = new HtmlParserImpl();
            parser.setEncoding(this.htmlfile.getCharset());
            in = htmlfile.getContents();
            HtmlNode node = parser.parse(in);
            proceed(node);
        } catch (Exception e) {
            DoltengCore.log(e);
        } finally {
            InputStreamUtil.close(in);
        }
    }

    private void proceed(HtmlNode node) {
        if (node instanceof DocumentNode) {
            proceed((DocumentNode) node);
        } else if (node instanceof ElementNode) {
            proceed((ElementNode) node);
        }
    }

    private void proceed(DocumentNode node) {
        for (int i = 0; i < node.getChildSize(); i++) {
            HtmlNode child = node.getChild(i);
            proceed(child);
        }
    }

    private void proceed(ElementNode node) {
        for (int i = 0; i < node.getChildSize(); i++) {
            HtmlNode child = node.getChild(i);
            proceed(child);
        }
        String id = node.getId();
        if (StringUtil.isEmpty(id)) {
            id = node.getProperty(JsfConstants.CLASS_ATTR);
        }
        if (StringUtil.isEmpty(id)) {
            return;
        }

        if (0 == id.indexOf(ExtensionConstants.DO_PREFIX)) {
            BasicMethodMetaData meta = new BasicMethodMetaData();
            meta.setModifiers(Modifier.PUBLIC);
            meta.setName(id);
            this.actionMethods.add(meta);
        } else if (skipIds.matcher(id).matches() == false) {
            // TODO ElementProcessorFactoryを使う様にする。
            BasicFieldMetaData meta = new BasicFieldMetaData();
            meta.setModifiers(Modifier.PUBLIC);
            if (PageClassColumn.multiItemRegx.matcher(id).matches()) {
                meta.setDeclaringClassName("java.util.List");
            } else {
                meta.setDeclaringClassName("java.lang.String");
            }
            meta.setName(id);
            this.pageFields.put(id, meta);
        }
    }

    public List getActionMethods() {
        return this.actionMethods;
    }

    public Map getPageFields() {
        return this.pageFields;
    }
}
