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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.seasar.framework.container.factory.XmlS2ContainerBuilder;
import org.seasar.framework.util.ResourceUtil;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author taichi
 * 
 */
public class S2ContainerEntityResolver implements EntityResolver {

    private Map dtdPaths = new HashMap();

    public S2ContainerEntityResolver() {
        super();
        this.registerDtdPath(XmlS2ContainerBuilder.PUBLIC_ID,
                XmlS2ContainerBuilder.DTD_PATH);
        this.registerDtdPath(XmlS2ContainerBuilder.PUBLIC_ID21,
                XmlS2ContainerBuilder.DTD_PATH21);
        this.registerDtdPath(XmlS2ContainerBuilder.PUBLIC_ID23,
                XmlS2ContainerBuilder.DTD_PATH23);
        this.registerDtdPath(XmlS2ContainerBuilder.PUBLIC_ID24,
                XmlS2ContainerBuilder.DTD_PATH24);
    }

    public void registerDtdPath(String publicId, String dtdPath) {
        dtdPaths.put(publicId, dtdPath);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String,
     *      java.lang.String)
     */
    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException {
        String dtdPath = null;
        if (publicId != null) {
            dtdPath = (String) dtdPaths.get(publicId);
        }
        if (dtdPath == null) {
            return null;
        }
        return new InputSource(ResourceUtil.getResourceAsStream(dtdPath));
    }

}
