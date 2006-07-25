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
package org.seasar.dolteng.eclipse.nls;

import org.eclipse.osgi.util.NLS;

/**
 * @author taichi
 * 
 */
public class Labels extends NLS {

    public static String PLUGIN_NAME;

    public static String CONNECTION_DIALOG_TITLE;

    public static String CONNECTION_DIALOG_NAME;

    public static String CONNECTION_DIALOG_DEPENDENT_PROJECT;

    public static String CONNECTION_DIALOG_DEPENDENT_PROJECT_BROWSE;

    public static String CONNECTION_DIALOG_SELECT_PROJECT;

    public static String CONNECTION_DIALOG_DRIVER_PATH;

    public static String CONNECTION_DIALOG_DRIVER_PATH_BROWSE;

    public static String CONNECTION_DIALOG_DRIVER_CLASS;

    public static String CONNECTION_DIALOG_DRIVER_CLASS_FIND;

    public static String CONNECTION_DIALOG_CONNECTION_URL;

    public static String CONNECTION_DIALOG_USER;

    public static String CONNECTION_DIALOG_PASS;

    public static String CONNECTION_DIALOG_CHARSET;

    public static String CONNECTION_DIALOG_TEST;

    public static String ACTION_CONNECTION_CONFIG_ADD;

    public static String ACTION_CONNECTION_CONFIG_DELETE;

    public static String ACTION_FIND_CHILDREN;

    public static String NODE_FINDING;

    public static String WIZARD_ENTITY_CREATION_TITLE;

    public static String WIZARD_ENTITY_CREATION_DESCRIPTION;

    public static String WIZARD_PAGE_ENTITY_FIELD_SELECTION;

    public static String WIZARD_PAGE_ENTITY_TREE_LABEL;

    public static String WIZARD_PAGE_ENTITY_CONVERSION_METHOD;

    public static String WIZARD_PAGE_ENTITY_CONVERSION_METHOD_NONE;

    public static String WIZARD_PAGE_ENTITY_CONVERSION_METHOD_BASIC;

    public static String ACTION_ENTITY_CREATION;

    public static String COLUMN_SQL_TYPE;

    public static String COLUMN_COLUMN_NAME;

    public static String COLUMN_MODIFIER;

    public static String COLUMN_JAVA_CLASS;

    public static String COLUMN_FIELD_NAME;

    public static String PREFERENCE_USE_DOLTENG;

    public static String PREFERENCE_USE_S2DAO;

    public static String PREFERENCE_DEFAULT_ENTITY_PKG;

    public static String PREFERENCE_DEFAULT_DAO_PKG;

    public static String BROWSE;

    public static String PACKAGE_SELECTION;

    public static String PACKAGE_SELECTION_DESC;

    public static String PACKAGE_SELECTION_EMPTY;

    static {
        Class clazz = Labels.class;
        NLS.initializeMessages(clazz.getName(), clazz);
    }
}
