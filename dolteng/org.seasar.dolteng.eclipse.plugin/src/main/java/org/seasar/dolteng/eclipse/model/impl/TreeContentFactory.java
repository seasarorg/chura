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
package org.seasar.dolteng.eclipse.model.impl;

import org.seasar.dolteng.core.dao.DatabaseMetaDataDao;
import org.seasar.dolteng.core.dao.impl.BasicDatabaseMetadataDao;
import org.seasar.dolteng.core.entity.ColumnMetaData;
import org.seasar.dolteng.core.entity.TableMetaData;
import org.seasar.dolteng.eclipse.model.TreeContent;
import org.seasar.dolteng.eclipse.preferences.ConnectionConfig;

/**
 * @author taichi
 * 
 */
public class TreeContentFactory {

    private ConnectionConfig config;

    private DatabaseMetaDataDao metaDataDao;

    public TreeContentFactory(ConnectionConfig config) {
        super();
        BasicDatabaseMetadataDao dao = new BasicDatabaseMetadataDao();
        dao.setDataSource(config);
        metaDataDao = dao;
        this.config = config;
    }

    TreeContent[] createNode(ConnectionNode parent) {
        String[] schemas = metaDataDao.getSchemas();
        TreeContent[] results = null;
        if (0 < schemas.length) {
            results = new TreeContent[schemas.length];
            for (int i = 0; i < schemas.length; i++) {
                results[i] = new SchemaNode(this, schemas[i]);
            }
        } else { // スキーマの取れないDBなら、テーブルを直接取りにいく。
            results = createNode("%");
        }
        return results;
    }

    TreeContent[] createNode(SchemaNode parent) {
        return createNode(parent.getText());
    }

    private TreeContent[] createNode(String name) {
        TableMetaData[] metas = metaDataDao.getTables(name, config
                .getTableTypes());
        TreeContent[] results = new TreeContent[metas.length];
        for (int i = 0; i < metas.length; i++) {
            results[i] = new TableNode(this, metas[i]);
        }
        return results;
    }

    TreeContent[] createNode(TableNode parent) {
        ColumnMetaData[] metas = metaDataDao.getColumns(parent.getMetaData());
        TreeContent[] results = new TreeContent[metas.length];
        for (int i = 0; i < metas.length; i++) {
            results[i] = new ColumnNode(metas[i]);
        }
        return results;
    }

}
