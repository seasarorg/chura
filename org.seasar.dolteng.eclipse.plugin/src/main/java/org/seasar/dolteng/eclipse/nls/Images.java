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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.seasar.eclipse.common.util.StaticImageLoader;

/**
 * @author taichi
 * 
 */
public class Images {

    public static Image PLUGIN_IMAGE;

    public static ImageDescriptor SEASAR;

    public static ImageDescriptor CONNECTION_WIZARD;

    public static Image CONNECTION;

    public static Image SCHEMA;

    public static Image TABLE;

    public static Image VIEW;

    public static Image COLUMNS;

    public static Image PRIMARY_KEY;

    public static ImageDescriptor ADD;

    public static ImageDescriptor DELETE;

    public static ImageDescriptor REFRESH;

    public static Image DOTS;

    public static ImageDescriptor ENTITY_WIZARD;

    public static ImageDescriptor GENERATE_CODE;

    public static Image PUBLIC_CO;

    public static Image PROTECTED_CO;

    public static Image DEFAULT_CO;

    public static Image PRIVATE_CO;

    public static Image JAVA_PROJECT;

    public static Image CHECKED;

    public static Image UNCHECKED;

    public static Image CHECK;

    public static Image TYPE;

    public static Image SYNCED;

    public static Image PACKAGE;

    public static Image INTERFACE;

    public static Image CLASS;

    public static Image RENAME;

    static {
        StaticImageLoader.loadResources(Images.class, Images.class.getName());
    }
}
