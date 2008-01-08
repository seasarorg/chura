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
package org.seasar.dolteng.eclipse.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JPAAssociationElements {

    public static final Set<String> ASSOCIATE_ANNOTATIONS = new HashSet<String>();

    private static final Map<String, String> DEFAULT_FETCH = new HashMap<String, String>();

    static {
        ASSOCIATE_ANNOTATIONS.add("javax.persistence.ManyToOne");
        ASSOCIATE_ANNOTATIONS.add("ManyToOne");
        ASSOCIATE_ANNOTATIONS.add("javax.persistence.OneToOne");
        ASSOCIATE_ANNOTATIONS.add("OneToOne");
        ASSOCIATE_ANNOTATIONS.add("javax.persistence.OneToMany");
        ASSOCIATE_ANNOTATIONS.add("OneToMany");
        ASSOCIATE_ANNOTATIONS.add("javax.persistence.ManyToMany");
        ASSOCIATE_ANNOTATIONS.add("ManyToMany");

        DEFAULT_FETCH.put("javax.persistence.ManyToOne", "EAGER");
        DEFAULT_FETCH.put("ManyToOne", "EAGER");
        DEFAULT_FETCH.put("javax.persistence.OneToOne", "EAGER");
        DEFAULT_FETCH.put("OneToOne", "EAGER");
        DEFAULT_FETCH.put("javax.persistence.OneToMany", "LAZY");
        DEFAULT_FETCH.put("OneToMany", "LAZY");
        DEFAULT_FETCH.put("javax.persistence.ManyToMany", "LAZY");
        DEFAULT_FETCH.put("ManyToMany", "LAZY");

    }

    private boolean exists = false;

    private String name = "";

    private String targetEntity = "";

    private List<Object> cascade = new ArrayList<Object>();

    private String fetch = "";

    private boolean optional = true;

    private String mappedBy = "";

    public boolean isDefaultFetch() {
        return DEFAULT_FETCH.get(name).equals(this.fetch);
    }

    /**
     * @return Returns the cascade.
     */
    public List<Object> getCascade() {
        return cascade;
    }

    /**
     * @return Returns the exists.
     */
    public boolean isExists() {
        return exists;
    }

    /**
     * @param exists
     *            The exists to set.
     */
    public void setExists(boolean exists) {
        this.exists = exists;
    }

    /**
     * @return Returns the fetch.
     */
    public String getFetch() {
        return fetch;
    }

    /**
     * @param fetch
     *            The fetch to set.
     */
    public void setFetch(String fetch) {
        this.fetch = fetch;
    }

    /**
     * @return Returns the mappedBy.
     */
    public String getMappedBy() {
        return mappedBy;
    }

    /**
     * @param mappedBy
     *            The mappedBy to set.
     */
    public void setMappedBy(String mappedBy) {
        this.mappedBy = mappedBy;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the optional.
     */
    public boolean isOptional() {
        return optional;
    }

    /**
     * @param optional
     *            The optional to set.
     */
    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    /**
     * @return Returns the targetEntity.
     */
    public String getTargetEntity() {
        return targetEntity;
    }

    /**
     * @param targetEntity
     *            The targetEntity to set.
     */
    public void setTargetEntity(String targetEntity) {
        this.targetEntity = targetEntity;
    }

}