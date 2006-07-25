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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import org.seasar.dolteng.eclipse.DoltengCore;
import org.seasar.framework.beans.BeanDesc;
import org.seasar.framework.beans.factory.BeanDescFactory;
import org.seasar.framework.util.FieldUtil;
import org.seasar.framework.util.ResourceBundleUtil;

/**
 * @author taichi
 * 
 */
public class StaticImageLoader {

    public static void loadResources(Class holder, String name) {
        loadResources(JFaceResources.getImageRegistry(), holder, name);
    }

    public static void loadResources(ImageRegistry registry, Class holder,
            String name) {
        ResourceBundle bundle = getBundle(name, holder.getClassLoader());
        if (bundle == null) {
            return;
        }
        BeanDesc holderBd = BeanDescFactory.getBeanDesc(holder);
        Map pathMap = ResourceBundleUtil.convertMap(bundle);
        for (int i = 0; i < holderBd.getFieldSize(); i++) {
            Field field = holderBd.getField(i);
            String key = field.getName();
            if (validateMask(field)) {
                continue;
            }

            if (pathMap.containsKey(key) == false) {
                log(key + " not found in " + name);
                continue;
            }

            ImageDescriptor id = ImageDescriptor.createFromFile(holder, pathMap
                    .get(key).toString());
            registry.put(key, id);

            if (isAssignableFrom(ImageDescriptor.class, field)) {
                FieldUtil.set(field, null, id);
            } else if (isAssignableFrom(Image.class, field)) {
                FieldUtil.set(field, null, registry.get(key));
            }
        }
    }

    private static boolean validateMask(Field f) {
        final int MOD_EXPECTED = Modifier.PUBLIC | Modifier.STATIC;
        final int MOD_MASK = MOD_EXPECTED | Modifier.FINAL;
        return (f.getModifiers() & MOD_MASK) != MOD_EXPECTED;
    }

    private static void log(String msg) {
        DoltengCore.log(msg);
    }

    private static ResourceBundle getBundle(String name, ClassLoader loader) {
        try {
            return ResourceBundle.getBundle(name, Locale.getDefault(), loader);
        } catch (MissingResourceException e) {
            return null;
        }
    }

    private static boolean isAssignableFrom(final Class clazz,
            final Field target) {
        return clazz.isAssignableFrom(target.getType());
    }
}
