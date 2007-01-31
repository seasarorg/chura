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
package org.seasar.dolteng.eclipse.action;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.seasar.dolteng.eclipse.Constants;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.dolteng.eclipse.model.impl.ColumnNode;
import org.seasar.dolteng.eclipse.model.impl.ProjectNode;
import org.seasar.dolteng.eclipse.model.impl.TableNode;
import org.seasar.dolteng.eclipse.nls.Images;
import org.seasar.dolteng.eclipse.nls.Labels;
import org.seasar.dolteng.eclipse.nls.Messages;
import org.seasar.dolteng.eclipse.operation.ScaffoldJob;
import org.seasar.dolteng.eclipse.preferences.DoltengPreferences;
import org.seasar.dolteng.eclipse.template.ScaffoldTemplateHandler;
import org.seasar.dolteng.eclipse.util.SelectionUtil;
import org.seasar.dolteng.eclipse.util.WorkbenchUtil;
import org.seasar.framework.util.CaseInsensitiveMap;
import org.seasar.framework.util.StringUtil;

/**
 * @author taichi
 * 
 */
@SuppressWarnings("unchecked")
public class NewScaffoldAction extends Action {

    public static final String ID = NewScaffoldAction.class.getName();

    private static final Map<String, String> scaffolds = new CaseInsensitiveMap();
    static {
        scaffolds.put(Constants.DAO_TYPE_UUJI, "scaffold");
        scaffolds.put(Constants.DAO_TYPE_S2DAO, "scaffold_s2dao");
        scaffolds.put(Constants.DAO_TYPE_KUINADAO, "scaffold_kuinadao");
    }

    private ISelectionProvider provider;

    /**
     * 
     */
    public NewScaffoldAction(ISelectionProvider provider) {
        super();
        this.provider = provider;
        setId(ID);
        setText(Labels.ACTION_SCAFFOLD_CREATION);
        setImageDescriptor(Images.GENERATE_CODE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    public void run() {
        Object elem = SelectionUtil.getCurrentSelection(this.provider);
        TableNode content = null;
        if (elem instanceof TableNode) {
            content = (TableNode) elem;
            content.findChildren();
        } else if (elem instanceof ColumnNode) {
            ColumnNode cn = (ColumnNode) elem;
            content = (TableNode) cn.getParent();
        }

        if (content != null
                && MessageDialog.openQuestion(WorkbenchUtil.getShell(),
                        Labels.PLUGIN_NAME, Messages.GENERATE_SCAFFOLD_CODES)) {
            IProject project = ((ProjectNode) content.getRoot())
                    .getJavaProject().getProject();
            DoltengPreferences pref = DoltengCore
                    .getPreferences(project);
            if (pref != null) {
                String type = (String) scaffolds.get(pref.getDaoType());
                if (StringUtil.isEmpty(type) == false) {
                    ScaffoldTemplateHandler handler = new ScaffoldTemplateHandler(
                            type, project, content);
                    ScaffoldJob job = new ScaffoldJob(handler);
                    job.schedule();
                }
            }
        }
    }
}
