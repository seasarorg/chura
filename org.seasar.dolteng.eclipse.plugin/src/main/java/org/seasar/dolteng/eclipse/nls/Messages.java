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
package org.seasar.dolteng.eclipse.nls;

import org.eclipse.osgi.util.NLS;

/**
 * @author taichi
 * 
 */
public class Messages extends NLS {

    static {
        Class clazz = Messages.class;
        NLS.initializeMessages(clazz.getName(), clazz);
    }

    public static String SELECT_PROJECT;

    public static String JDBC_DRIVER_FINDING;

    public static String JDBC_DRIVER_FINDING_CANCELLED;

    public static String CONNECTION_TEST_SUCCEED;

    public static String CONNECTION_TEST_FAILED;

    public static String UNSUPPORTED_ENCODING;

    public static String FILE_NOT_FOUND;

    public static String DRIVER_CLASS_NOT_FOUND;

    public static String PROJECT_NOT_FOUND;

    public static String NAME_IS_EMPTY;

    public static String CONNECTION_URL_EMPTY;

    public static String PLUGIN_INITIALIZING;

    public static String JDBC_DICON_LOADING;

    public static String ImportsStructure_operation_description;

    public static String CHURA_PROJECT_DESCRIPTION;

    public static String CREATING_PROJECT;

    public static String PACKAGE_NAME_IS_EMPTY;

    public static String INVALID_PACKAGE_NAME;

    public static String INVALID_HTML_PATH;

    public static String ACTION_HAS_SERVICE;

    public static String SERVICE_EXISTS;

    public static String PROCESS;

    public static String BEGINING_OF_CREATE;

    public static String CREATE_BASE_PROJECT;

    public static String RELOAD_RESOURCES;

    public static String ADD_NATURE_OF;

    public static String REMOVE_NATURE_OF;

    public static String BUILD_PROJECT;

    public static String RELOAD_DATABASE_VIEW;

    public static String PROCESS_MAPPING;

    public static String GENERATE_SCAFFOLD_CODES;

    public static String GENERATE_CODES;

    public static String UNSUPPORTED_CLASS_IS_LOADED;

    public static String ONLY_USE_VALID_URL;

    public static String REGISTER_MOCKS;

    public static String INVALID_OUTPUT_FILE;

    public static String COPY_FROM_AND_COPY_TO_ARE_SAME;

    public static String SELECT_ACTION_SCRIPT_ROOT;

    public static String ContainerGroup_message;

    public static String ContainerGroup_selectFolder;

    public static String SELECT_FOLDER;

    public static String SELECT_FLEX2_SERVICE;

    public static String SELECT_ACTION_SCRIPT_DTO;

    public static String PROCESS_VALIDATE;

    public static String ILLEGAL_PARAMETER_NAME;

    public static String ILLEGAL_PARAMETER_TYPE;

    public static String ILLEGAL_PARAMETER_COUNT;

    public static String ILLEGAL_KEYWORD_TYPE;

    public static String SQL_NOT_FOUND;

    public static String CREATE_NEW_SQL;

    public static String DOWNLOAD_FROM_MAVEN_REPOS;
}
