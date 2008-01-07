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
package org.seasar.dolteng.eclipse.wigets;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.seasar.dolteng.eclipse.ast.JPAAssociationElements;

/**
 * @author taichi
 * 
 */
public class JPAAssociationDialogMain {

    /**
     * 
     */
    public JPAAssociationDialogMain() {
        super();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        Shell shell = new Shell(Display.getDefault());
        try {
            JPAAssociationDialog dialog = new JPAAssociationDialog(shell);
            final JPAAssociationElements ae = new JPAAssociationElements();
            ae.setName("javax.persistence.ManyToOne");
            ae.setTargetEntity("java.util.List");
            ae.getCascade().add("ALL");
            ae.getCascade().add("REMOVE");
            ae.setFetch("LAZY");
            ae.setOptional(true);
            ae.setMappedBy("hogehoge");
            dialog.setElements(ae);
            dialog.open();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            shell.close();
        }
    }

}
