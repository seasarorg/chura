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
package org.seasar.dolteng.core.entity.impl;

import org.seasar.dolteng.core.entity.MemberMetaData;

/**
 * @author taichi
 * 
 */
public abstract class AbstractMemberMetaData extends AbstractNamedMetaData
        implements MemberMetaData {

    private String declaringClassName;

    private int modifiers;

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.entity.MemberMetaData#getDeclaringClassName()
     */
    public String getDeclaringClassName() {
        return this.declaringClassName;
    }

    /**
     * @param declaringClassName
     *            The declaringClassName to set.
     */
    public void setDeclaringClassName(String declaringClassName) {
        this.declaringClassName = declaringClassName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.entity.MemberMetaData#getModifiers()
     */
    public int getModifiers() {
        return this.modifiers;
    }

    /**
     * @param modifiers
     *            The modifiers to set.
     */
    public void setModifiers(int modifiers) {
        this.modifiers = modifiers;
    }

}
