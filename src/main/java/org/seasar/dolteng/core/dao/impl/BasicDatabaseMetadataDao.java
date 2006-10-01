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
package org.seasar.dolteng.core.dao.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.seasar.dolteng.core.dao.DatabaseMetaDataDao;
import org.seasar.dolteng.core.entity.ColumnMetaData;
import org.seasar.dolteng.core.entity.TableMetaData;
import org.seasar.dolteng.core.entity.impl.BasicColumnMetaData;
import org.seasar.dolteng.core.entity.impl.BasicTableMetaData;
import org.seasar.extension.jdbc.util.ConnectionUtil;
import org.seasar.extension.jdbc.util.DataSourceUtil;
import org.seasar.framework.exception.SQLRuntimeException;
import org.seasar.framework.util.ResultSetUtil;

/**
 * @author taichi
 * 
 */
public class BasicDatabaseMetadataDao implements DatabaseMetaDataDao {

    private DataSource dataSource;

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.dao.DatabaseMetaDataDao#getSchemas()
     */
    public String[] getSchemas() {
        DatabaseMetaDataHandler handler = new DatabaseMetaDataHandler() {

            public ResultSet getMetaDatas(DatabaseMetaData dmd)
                    throws SQLException {
                return dmd.supportsSchemasInTableDefinitions() ? dmd
                        .getSchemas() : null;
            };

            public Object handleResultSet(ResultSet rs) throws SQLException {
                return rs.getString("TABLE_SCHEM");
            };
        };
        List result = handle(handler);
        return (String[]) result.toArray(new String[result.size()]);
    }

    protected List handle(DatabaseMetaDataHandler handler) {
        Connection con = null;
        ResultSet rs = null;
        try {
            con = DataSourceUtil.getConnection(this.dataSource);
            DatabaseMetaData dmd = con.getMetaData();
            rs = handler.getMetaDatas(dmd);
            List result = new ArrayList();
            while (rs != null && rs.next()) {
                result.add(handler.handleResultSet(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new SQLRuntimeException(e);
        } finally {
            ResultSetUtil.close(rs);
            ConnectionUtil.close(con);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.dao.DatabaseMetaDataDao#getTables(java.lang.String)
     */
    public TableMetaData[] getTables(final String schema, final String[] types) {
        DatabaseMetaDataHandler handler = new DatabaseMetaDataHandler() {
            private int index = 0;

            private boolean supportsSchemas = false;

            public ResultSet getMetaDatas(DatabaseMetaData dmd)
                    throws SQLException {
                supportsSchemas = dmd.supportsSchemasInTableDefinitions();
                return dmd.getTables(null, schema, "%", types);
            }

            public Object handleResultSet(ResultSet rs) throws SQLException {
                BasicTableMetaData meta = new BasicTableMetaData();
                meta.setIndex(index++);
                if (supportsSchemas) {
                    meta.setTableType(rs.getString("TABLE_TYPE"));
                    meta.setSchema(rs.getString("TABLE_SCHEM"));
                }
                meta.setName(rs.getString("TABLE_NAME"));
                meta.setComment(rs.getString("REMARKS"));
                return meta;
            }
        };
        List result = handle(handler);
        return (TableMetaData[]) result
                .toArray(new TableMetaData[result.size()]);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.seasar.dolteng.core.dao.DatabaseMetaDataDao#getColumns(org.seasar.dolteng.core.model.TableMetaData)
     */
    public ColumnMetaData[] getColumns(final TableMetaData table) {
        DatabaseMetaDataHandler columnHandler = new DatabaseMetaDataHandler() {
            private int index = 0;

            public ResultSet getMetaDatas(DatabaseMetaData dmd)
                    throws SQLException {
                return dmd.getColumns(null, table.getSchema(), table.getName(),
                        "%");
            }

            public Object handleResultSet(ResultSet rs) throws SQLException {
                BasicColumnMetaData meta = new BasicColumnMetaData();
                meta.setIndex(index++);
                meta.setName(rs.getString("COLUMN_NAME"));
                meta.setSqlType(rs.getInt("DATA_TYPE"));
                meta.setSqlTypeName(rs.getString("TYPE_NAME"));
                meta.setColumnSize(rs.getInt("COLUMN_SIZE"));
                meta.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));
                meta.setNullable(DatabaseMetaData.typeNullable == rs
                        .getInt("NULLABLE"));
                meta.setComment(rs.getString("REMARKS"));
                return meta;
            }
        };

        DatabaseMetaDataHandler pkHanldler = new DatabaseMetaDataHandler() {
            public ResultSet getMetaDatas(DatabaseMetaData dmd)
                    throws SQLException {
                return dmd.getPrimaryKeys(null, table.getSchema(), table
                        .getName());
            }

            public Object handleResultSet(ResultSet rs) throws SQLException {
                return rs.getString("COLUMN_NAME");
            }
        };

        // DatabaseMetaDataHandler fkHanldler = new DatabaseMetaDataHandler() {
        // public ResultSet getMetaDatas(DatabaseMetaData dmd)
        // throws SQLException {
        // String schema = table.getSchema() == null ? "%" : table.getSchema();
        // return dmd.getImportedKeys(null, schema, table.getName());
        // }
        //
        // public Object handleResultSet(ResultSet rs) throws SQLException {
        // return rs.getString("FKCOLUMN_NAME");
        // }
        // };

        List columns = handle(columnHandler);
        BasicColumnMetaData[] result = (BasicColumnMetaData[]) columns
                .toArray(new BasicColumnMetaData[columns.size()]);
        List pks = handle(pkHanldler);
        // List fks = handle(fkHanldler);
        for (int i = 0; i < result.length; i++) {
            result[i].setPrimaryKey(pks.contains(result[i].getName()));
            // result[i].setForeignKey(fks.contains(result[i].getName()));
        }
        return result;
    }

    public interface DatabaseMetaDataHandler {

        public ResultSet getMetaDatas(DatabaseMetaData dmd) throws SQLException;

        public Object handleResultSet(ResultSet rs) throws SQLException;
    }

    /**
     * @return Returns the dataSource.
     */
    public DataSource getDataSource() {
        return this.dataSource;
    }

    /**
     * @param dataSource
     *            The dataSource to set.
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

}