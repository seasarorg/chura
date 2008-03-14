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
package org.seasar.dolteng.projects;

import static org.seasar.dolteng.eclipse.Constants.CTX_LIB_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_LIB_SRC_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_MAIN_JAVA_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_MAIN_OUT_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_MAIN_RESOURCE_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_TEST_JAVA_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_TEST_LIB_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_TEST_LIB_SRC_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_TEST_OUT_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_TEST_RESOURCE_PATH;
import static org.seasar.dolteng.eclipse.Constants.CTX_WEBAPP_ROOT;

import java.util.HashMap;
import java.util.Map;

/**
 * @author taichi
 * 
 */
public class Constants {

    public static final String ID_PLUGIN = "org.seasar.dolteng.projects";

    /* ------------------------------------------------------------------ */

    public static final String EXTENSION_POINT_RESOURCE_HANDLER = "resourceHandler";

    public static final String EXTENSION_POINT_RESOURCE_LOADER = "resourceLoader";

    public static final String EXTENSION_POINT_NEW_PROJECT = "newProject";

    /* ------------------------------------------------------------------ */

    public static final String TAG_FACET = "facet";

    public static final String ATTR_FACET_ROOT = "root";

    public static final String ATTR_FACET_EXTENDS = "extends";

    public static final String TAG_CONTEXT_PROPERTY = "contextProperty";

    public static final String ATTR_CTXPROP_NAME = "name";

    public static final String ATTR_CTXPROP_VALUE = "value";

    public static final String TAG_IF = "if";

    public static final String ATTR_IF_JRE = "jre";

    public static final String TAG_LOADER = "loader";

    public static final String ATTR_LOADER_TYPE = "type";

    public static final String ATTR_LOADER_CLASS = "class";

    public static final String TAG_HANDLER = "handler";

    public static final String ATTR_HAND_TYPE = "type";

    public static final String ATTR_HAND_LOADER = "resourceLoader";

    public static final String ATTR_HAND_CLASS = "class";

    public static final String TAG_ENTRY = "entry";

    /* ------------------------------------------------------------------ */
    
    public static final String TAG_ARG = "arg";
    
    public static final String TAG_INCLUDE = "include";

    public static final String ATTR_INCLUDE_PATH = "path";

    public static final String TAG_COMPONENT = "component";

    public static final String ATTR_COMPONENT_NAME = "name";

    public static final String ATTR_COMPONENT_CLASS = "class";

    public static final String TAG_INIT_METHOD = "initMethod";

    public static final String TAG_INIT_METHOD_X = "initMethodXXX";

    public static final String ATTR_INIT_NAME = "name";

    public static final String TAG_PROPERTY = "property";
    
    public static final String ATTR_PROPERTY_NAME = "name";
    
    /* ------------------------------------------------------------------ */

    public static final String TAG_CATEGORY = "category";

    public static final String ATTR_CATEGORY_KEY = "key";

    public static final String ATTR_CATEGORY_ID = "id";

    public static final String ATTR_CATEGORY_NAME = "name";

    public static final String TAG_APP_TYPE = "applicationtype";

    public static final String ATTR_APP_TYPE_ID = "id";

    public static final String ATTR_APP_TYPE_NAME = "name";
    
    public static final String ATTR_APP_TYPE_PACKAGING = "packaging";

    public static final String TAG_DEFAULT = "default";

    public static final String ATTR_DEFAULT_FACET = "facet";

    public static final String TAG_DISABLE = "disable";

    public static final String ATTR_DISABLE_CATEGORY = "category";

    public static final String ATTR_DISABLE_FACET = "facet";

    public static final String TAG_FIRST = "first";

    public static final String ATTR_FIRST_FACET = "facet";

    public static final String TAG_LAST = "last";

    public static final String ATTR_LAST_FACET = "facet";

    /* ------------------------------------------------------------------ */

    private static final String DEFAULT_LIB_PATH = "src/main/webapp/WEB-INF/lib";

    private static final String DEFAULT_LIB_SRC_PATH = "src/main/webapp/WEB-INF/lib/sources";

    private static final String DEFAULT_TEST_LIB_PATH = "lib";

    private static final String DEFAULT_TEST_LIB_SRC_PATH = "lib/sources";

    private static final String DEFAULT_MAIN_JAVA_PATH = "src/main/java";

    private static final String DEFAULT_MAIN_RESOURCE_PATH = "src/main/resources";

    private static final String DEFAULT_MAIN_OUT_PATH = "src/main/webapp/WEB-INF/classes";

    private static final String DEFAULT_WEBAPP_ROOT = "src/main/webapp";

    private static final String DEFAULT_TEST_JAVA_PATH = "src/test/java";

    private static final String DEFAULT_TEST_RESOURCE_PATH = "src/test/resources";

    private static final String DEFAULT_TEST_OUT_PATH = "target/test-classes";

    public static final Map<String, String> DEFAULT_CONFIGURE_CONTEXT = new HashMap<String, String>() {
        private static final long serialVersionUID = 1L;
        {
            put(CTX_LIB_PATH, DEFAULT_LIB_PATH);
            put(CTX_LIB_SRC_PATH, DEFAULT_LIB_SRC_PATH);
            put(CTX_TEST_LIB_PATH, DEFAULT_TEST_LIB_PATH);
            put(CTX_TEST_LIB_SRC_PATH, DEFAULT_TEST_LIB_SRC_PATH);
            put(CTX_MAIN_JAVA_PATH, DEFAULT_MAIN_JAVA_PATH);
            put(CTX_MAIN_RESOURCE_PATH, DEFAULT_MAIN_RESOURCE_PATH);
            put(CTX_MAIN_OUT_PATH, DEFAULT_MAIN_OUT_PATH);
            put(CTX_WEBAPP_ROOT, DEFAULT_WEBAPP_ROOT);
            put(CTX_TEST_JAVA_PATH, DEFAULT_TEST_JAVA_PATH);
            put(CTX_TEST_RESOURCE_PATH, DEFAULT_TEST_RESOURCE_PATH);
            put(CTX_TEST_OUT_PATH, DEFAULT_TEST_OUT_PATH);
        }
    };
}
