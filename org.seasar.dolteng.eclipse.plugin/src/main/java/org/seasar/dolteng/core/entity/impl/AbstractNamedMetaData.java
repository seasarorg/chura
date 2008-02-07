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

import org.seasar.dolteng.core.entity.NamedMetaData;

/**
 * @author taichi
 * 
 */
public abstract class AbstractNamedMetaData implements NamedMetaData {

    private int index = 0;

    private String name = "";

    private String comment = "";

    /**
     * @return Returns the comment.
     */
    public String getComment() {
        return this.comment;
    }

    /**
     * @param comment
     *            The comment to set.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * @return Returns the index.
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * @param index
     *            The index to set.
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof AbstractNamedMetaData ? equals((AbstractNamedMetaData) other)
                : false;
    }

    public boolean equals(AbstractNamedMetaData other) {
        return other != null && this.getIndex() == other.getIndex()
                && this.getName().equals(other.getName());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer stb = new StringBuffer();
        stb.append("index : [").append(getIndex()).append(']');
        stb.append("name : [").append(this.getName()).append(']');
        return stb.toString();
    }

    public int compareTo(NamedMetaData other) {
        if (other instanceof AbstractNamedMetaData) {
            AbstractNamedMetaData anm = (AbstractNamedMetaData) other;
            if (this.getIndex() == anm.getIndex()) {
                return this.getName().compareTo(anm.getName());
            }
            return this.getIndex() - anm.getIndex();
        }
        return -1;
    }

}
