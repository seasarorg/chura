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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.framework.exception.IORuntimeException;

/**
 * @author taichi
 * 
 */
public class NewOrmXmlWizardPage extends WizardNewFileCreationPage {

    /**
     * @param selection
     */
    public NewOrmXmlWizardPage(IStructuredSelection selection) {
        super("NewOrmXmlWizardPage", selection);
    }

    /**
     * @param pageName
     * @param selection
     */
    public NewOrmXmlWizardPage(String pageName, IStructuredSelection selection) {
        super(pageName, selection);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.dialogs.WizardNewFileCreationPage#getInitialContents()
     */
    @Override
    protected InputStream getInitialContents() {
        InputStream result = null;
        try {
            URL url = FileLocator.find(DoltengCore.getDefault().getBundle(),
                    new Path("template").append("Orm.xml"), null);
            if (url != null) {
                result = url.openStream();
            }
            return result;
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }

}
