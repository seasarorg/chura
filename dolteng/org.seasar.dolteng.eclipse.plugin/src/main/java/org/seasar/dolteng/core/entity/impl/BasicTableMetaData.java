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

import org.seasar.dolteng.core.entity.TableMetaData;

/**
 * @author taichi
 * 
 */
public class BasicTableMetaData extends AbstractNamedMetaData implements
        TableMetaData {

    private String tableType = "";

    private String schema = null;

    /**
     * @return Returns the schema.
     */
    public String getSchema() {
        return this.schema;
    }

    /**
     * @param schema
     *            The schema to set.
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * @return Returns the tableType.
     */
    public String getTableType() {
        return this.tableType;
    }

    /**
     * @param tableType
     *            The tableType to set.
     */
    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

}
