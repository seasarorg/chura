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
/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.seasar.dolteng.eclipse.nls;

import org.eclipse.osgi.util.NLS;

public final class CorextMessages extends NLS {

    private static final String BUNDLE_NAME = "org.seasar.dolteng.eclipse.nls.CorextMessages";//$NON-NLS-1$

    private CorextMessages() {
        // Do not instantiate
    }

    public static String Resources_outOfSyncResources;

    public static String Resources_outOfSync;

    public static String Resources_modifiedResources;

    public static String Resources_fileModified;

    public static String JavaDocLocations_migrate_operation;

    public static String JavaDocLocations_error_readXML;

    public static String JavaDocLocations_migratejob_name;

    public static String AllTypesCache_searching;

    public static String AllTypesCache_checking_type_cache;

    public static String TypeInfoHistory_error_serialize;

    public static String TypeInfoHistory_error_read;

    public static String TypeInfoHistory_consistency_check;

    static {
        NLS.initializeMessages(BUNDLE_NAME, CorextMessages.class);
    }
}