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
package org.seasar.dolteng.eclipse;

import org.eclipse.core.runtime.QualifiedName;

/**
 * @author taichi
 * 
 */
public final class Constants {

    public static final String ID_PLUGIN = "org.seasar.dolteng.eclipse";

    public static final String ID_NATURE = ID_PLUGIN + ".nature";

    public static final String ID_NATURE_FLEX = ID_PLUGIN + ".nature.flex";

    public static final String ID_BUILDER = ID_PLUGIN + ".builder";

    public static final String ID_DATABASE_VIEW = ID_PLUGIN + ".databaseView";

    public static final String ID_PAGE_MAPPER = ID_PLUGIN + ".pageMapper";

    public static final String ID_HTML_MAPPER = ID_PLUGIN + ".htmlMapper";

    public static final String ID_DI_MAPPER = ID_PLUGIN + ".diMapper";

    public static final String ID_TOMCAT_PLUGIN = "com.sysdeo.eclipse.tomcat";

    public static final String ID_TOMCAT_NATURE = ID_TOMCAT_PLUGIN
            + ".tomcatnature";

    public static final String ID_DIIGU_PLUGIN = "org.seasar.diigu.eclipse";

    public static final String ID_DIIGU_NATURE = ID_DIIGU_PLUGIN
            + ".diiguNature";

    public static final String ID_DB_LAUNCHER_PLUGIN = "org.seasar.dblauncher";

    public static final String ID_DB_LAUNCHER_NATURE = ID_DB_LAUNCHER_PLUGIN
            + ".nature";

    public static final String ID_DB_LAUNCHER_START_SERVER_CMD = "org.seasar.dblauncher.command.start";

    public static final String ID_DB_LAUNCHER_EXECUTE_QUERY_CMD = "org.seasar.dblauncher.command.stop";

    /* ------------------------------------------------------------------ */

    public static final String ID_FLEX_BUILDER_PLUGIN = "com.adobe.flexbuilder.project";

    public static final String ID_FLEX_BUILDER_FLEXNATURE = ID_FLEX_BUILDER_PLUGIN
            + ".flexnature";

    public static final String ID_FLEX_BUILDER_ACTIONSCRIPTNATURE = ID_FLEX_BUILDER_PLUGIN
            + ".actionscriptnature";

    /* ------------------------------------------------------------------ */

    public static final String PREF_WEBCONTENTS_ROOT = "WebContentsRoot";

    public static final String PREF_SERVLET_PATH = "ServletPath";

    public static final String PREF_WEB_SERVER = "WebServer";

    public static final String PREF_NECESSARYDICONS = "NecessaryDicons";

    public static final String PREF_VIEW_TYPE = "ViewType";

    public static final String PREF_DAO_TYPE = "DaoType";

    public static final String PREF_DEFAULT_ENTITY_PACKAGE = "DefaultEntityPackage";

    public static final String PREF_DEFAULT_DTO_PACKAGE = "DefaultDtoPackage";

    public static final String PREF_DEFAULT_DAO_PACKAGE = "DefaultDaoPackage";

    public static final String PREF_DEFAULT_WEB_PACKAGE = "DefaultWebPackage";

    public static final String PREF_USE_PAGE_MARKER = "UsePageMarker";

    public static final String PREF_USE_DI_MARKER = "UseDIMarker";

    public static final String PREF_ORM_XML_OUTPUT_PATH = "OrmXmlOutputPath";

    public static final String PREF_DEFAULT_SRC_PATH = "DefaultSrcPath";

    public static final String PREF_DEFAULT_RESOURCE_PATH = "DefaultResourcePath";

    public static final String PREF_FLEX_SRC_PATH = "flexSrcPath";

    public static final QualifiedName PROP_USE_DI_MARKER = new QualifiedName(
            ID_PLUGIN, PREF_USE_DI_MARKER);

    public static final QualifiedName PROP_FLEX_PAGE_DTO_PATH = new QualifiedName(
            ID_PLUGIN, "flexPageDto");

    /* ------------------------------------------------------------------ */

    public static final String PREF_CONNECTION_NAME = "ConnectionName";

    public static final String PREF_DRIVER_PATH = "DriverPath";

    public static final String PREF_DRIVER_CLASS = "DriverClass";

    public static final String PREF_CONNECTION_URL = "ConnectionUrl";

    public static final String PREF_USER = "User";

    public static final String PREF_PASS = "Pass";

    public static final String PREF_CHARSET = "Charset";

    /* ------------------------------------------------------------------ */

    public static final String MARKER_ATTR_MAPPING_TYPE_NAME = ID_PAGE_MAPPER
            + ".mappingType";

    public static final String MARKER_ATTR_MAPPING_FIELD_NAME = ID_PAGE_MAPPER
            + ".mappingField";

    public static final String MARKER_ATTR_MAPPING_HTML_PATH = ID_HTML_MAPPER
            + ".htmlpath";

    public static final String MARKER_ATTR_MAPPING_HTML_ID = ID_HTML_MAPPER
            + ".id";

    public static final String MARKER_ATTR_MAPPING_ELEMENT = ID_HTML_MAPPER
            + ".mappingElem";

    /* ------------------------------------------------------------------ */

    public static final String VIEW_TYPE_TEEDA = "Teeda";

    public static final String VIEW_TYPE_FLEX2 = "Flex2";

    public static final String[] VIEW_TYPES = { VIEW_TYPE_TEEDA,
            VIEW_TYPE_FLEX2 };

    public static final String DAO_TYPE_KUINADAO = "Kuina-Dao";

    public static final String DAO_TYPE_S2DAO = "S2Dao";

    public static final String DAO_TYPE_UUJI = "Uuji";

    public static final String[] DAO_TYPES = { DAO_TYPE_KUINADAO,
            DAO_TYPE_S2DAO, DAO_TYPE_UUJI };

    /* ------------------------------------------------------------------ */
    public static final String CTX_PROJECT_NAME = "projectName";

    public static final String CTX_PACKAGE_NAME = "packageName";

    public static final String CTX_PACKAGE_PATH = "packagePath";

    public static final String CTX_JRE_CONTAINER = "jreContainer";

    public static final String CTX_LIB_PATH = "libPath";

    public static final String CTX_LIB_SRC_PATH = "libSrcPath";

    public static final String CTX_TEST_LIB_PATH = "testLibPath";

    public static final String CTX_TEST_LIB_SRC_PATH = "testLibSrcPath";

    public static final String CTX_MAIN_JAVA_PATH = "mainJavaPath";

    public static final String CTX_MAIN_RESOURCE_PATH = "mainResourcePath";

    public static final String CTX_MAIN_OUT_PATH = "mainOutPath";

    public static final String CTX_WEBAPP_ROOT = "webAppRoot";

    public static final String CTX_TEST_JAVA_PATH = "testJavaPath";

    public static final String CTX_TEST_RESOURCE_PATH = "testResourcePath";

    public static final String CTX_TEST_OUT_PATH = "testOutPath";
}
