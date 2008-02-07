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
package org.seasar.dolteng.core.dao.impl;

import org.seasar.dolteng.core.dao.DatabaseMetaDataDao;
import org.seasar.dolteng.core.entity.ColumnMetaData;
import org.seasar.dolteng.core.entity.TableMetaData;
import org.seasar.dolteng.core.entity.impl.BasicTableMetaData;
import org.seasar.dolteng.eclipse.preferences.impl.ConnectionConfigImpl;
import org.seasar.extension.unit.S2TestCase;

/**
 * @author taichi
 * 
 */
public class BasicDatabaseMetadataDaoTest extends S2TestCase {

    private static final String PATH = "BasicDatabaseMetadataDaoTest.dicon";

    protected static String PATH_DS = "jdbc-h2.dicon";

    private DatabaseMetaDataDao target;

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        include(PATH_DS);
        include(PATH);
        getContainer().getDescendant(convertPath(PATH)).include(
                getContainer().getDescendant(PATH_DS));
    }

    /*
     * Test method for
     * 'org.seasar.dolteng.core.dao.impl.BasicDatabaseMetadataDao.getSchemas()'
     */
    public void testGetSchemas() {
        String[] ary = target.getSchemas();
        assertNotNull(ary);
        assertTrue(0 < ary.length);
    }

    /*
     * Test method for
     * 'org.seasar.dolteng.core.dao.impl.BasicDatabaseMetadataDao.getTables(String,
     * String[])'
     */
    public void testGetTables() {
        TableMetaData[] tables = target.getTables("public",
                ConnectionConfigImpl.TABLE_TYPES);
        assertNotNull(tables);
        assertTrue(0 < tables.length);
        for (int i = 0; i < tables.length; i++) {
            TableMetaData meta = tables[i];
            assertEquals(i, meta.getIndex());
            assertNotNull(meta.getName());
            assertTrue(0 < meta.getName().length());
            assertNotNull(meta.getSchema());
            assertTrue(0 < meta.getSchema().length());
        }

    }

    /*
     * Test method for
     * 'org.seasar.dolteng.core.dao.impl.BasicDatabaseMetadataDao.getColumns(TableMetaData)'
     */
    public void testGetColumns() {
        BasicTableMetaData table = new BasicTableMetaData();
        table.setSchema("public");
        table.setName("DEPT");
        ColumnMetaData[] columns = target.getColumns(table);
        assertTrue(0 < columns.length);
        for (int i = 0; i < columns.length; i++) {
            ColumnMetaData meta = columns[i];
            assertEquals(i, meta.getIndex());
            assertNotNull(meta.getName());
        }
        ColumnMetaData column = columns[0];
        assertTrue(column.isPrimaryKey());
        assertFalse(column.isNullable());
        assertEquals(2, column.getColumnSize());
        assertEquals("numeric", column.getSqlTypeName());
        column = columns[1];
        assertTrue(column.isNullable());
        assertEquals(14, column.getColumnSize());
        assertEquals("varchar", column.getSqlTypeName());
    }

    public void testGetResultSetMetadata() throws Exception {
        String sql = "SELECT ID,DEPT_NO,DEPT_NAME AS DN,LOC AS LLL FROM DEPT";
        ColumnMetaData[] columns = target.getColumns(sql);
        assertTrue(0 < columns.length);
        for (int i = 0; i < columns.length; i++) {
            ColumnMetaData meta = columns[i];
            assertEquals(i, meta.getIndex());
            System.out.println(meta.getName());
            assertNotNull(meta.getName());
        }
    }
}
