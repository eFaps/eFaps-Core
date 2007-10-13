/*
 * Copyright 2003-2007 The eFaps Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.db.databases;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public class DerbyDatabase extends AbstractDatabase  {

  //////////////////////////////////////////////////////////////////////////////
  // static variables

  /**
   * Logging instance used in this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(DerbyDatabase.class);

  /**
   * SQL Select statement for all foreign keys and constraints.
   *
   * @see #deleteAll
   */
  private static final String SELECT_ALL_KEYS
          = "select t.TABLENAME, c.CONSTRAINTNAME "
              + "from SYS.SYSSCHEMAS s, SYS.SYSTABLES t, SYS.SYSCONSTRAINTS c "
              + "where s.AUTHORIZATIONID<>'DBA' "
                    + "and s.SCHEMAID=t.SCHEMAID "
                    + "and t.TABLEID=c.TABLEID "
                    + "and c.TYPE='F'";

  /**
   * SQL Select statement for all views.
   *
   * @see #deleteAll
   */
  private static final String SELECT_ALL_VIEWS
          = "select t.TABLENAME "
              + "from SYS.SYSSCHEMAS s, SYS.SYSTABLES t "
              + "where s.AUTHORIZATIONID<>'DBA' "
                    + "and s.SCHEMAID=t.SCHEMAID "
                    + "and t.TABLETYPE='V'";

  /**
   * SQL Select statement for all tables.
   *
   * @see #deleteAll
   */
  private static final String SELECT_ALL_TABLES
          = "select t.TABLENAME "
              + "from SYS.SYSSCHEMAS s, SYS.SYSTABLES t "
              + "where s.AUTHORIZATIONID<>'DBA' "
                    +"and s.SCHEMAID=t.SCHEMAID "
                    + "and t.TABLETYPE='T'";


  //////////////////////////////////////////////////////////////////////////////
  // constructors

  /**
   * @todo specificy real column type
   */
  public DerbyDatabase()  {
    this.columnMap.put(ColumnType.INTEGER,      "bigint");
//    this.columnMap.put(ColumnType.REAL,         "real");
    this.columnMap.put(ColumnType.STRING_SHORT, "char");
    this.columnMap.put(ColumnType.STRING_LONG,  "varchar");
    this.columnMap.put(ColumnType.DATETIME,     "timestamp");
    this.columnMap.put(ColumnType.BLOB,         "blob(2G)");
    this.columnMap.put(ColumnType.CLOB,         "clob(2G)");
    this.columnMap.put(ColumnType.BOOLEAN,      "smallint");
  }

  public String getCurrentTimeStamp()  {
    return "current_timestamp";
  }

  //////////////////////////////////////////////////////////////////////////////
  // instance methods

  /**
   * This is the Derby specific implementation of an all deletion. Following
   * order is used to remove all eFaps specific information:
   * <ul>
   * <li>remove all foreign keys of the user</li>
   * <li>remove all views of the user</li>
   * <li>remove all tables of the user</li>
   * </ul>
   * Attention! If application specific tables, views or contraints are defined,
   * this database objects are also removed!
   *
   * @param _con  sql connection
   * @throws SQLException
   */
  public void deleteAll(final Connection _con) throws SQLException  {

    Statement stmtSel = _con.createStatement();
    Statement stmtExec = _con.createStatement();

    try  {
    // remove all foreign keys
      if (LOG.isInfoEnabled())  {
        LOG.info("Remove all Foreign Keys");
      }
      ResultSet rs = stmtSel.executeQuery(SELECT_ALL_KEYS);
      while (rs.next())  {
        String tableName = rs.getString(1);
        String constrName = rs.getString(2);
        if (LOG.isDebugEnabled())  {
          LOG.debug("  - Table '" + tableName + "' Constraint '" + constrName + "'");
        }
        stmtExec.execute("alter table " + tableName + " drop constraint " + constrName);
      }
      rs.close();

      // remove all views
      if (LOG.isInfoEnabled())  {
        LOG.info("Remove all Views");
      }
      rs = stmtSel.executeQuery(SELECT_ALL_VIEWS);
      while (rs.next())  {
        String viewName = rs.getString(1);
        if (LOG.isDebugEnabled())  {
          LOG.debug("  - View '" + viewName + "'");
        }
        stmtExec.execute("drop view " + viewName);
      }
      rs.close();

      // remove all tables
      if (LOG.isInfoEnabled())  {
        LOG.info("Remove all Tables");
      }
      rs = stmtSel.executeQuery(SELECT_ALL_TABLES);
      while (rs.next())  {
        String tableName = rs.getString(1);
        if (LOG.isDebugEnabled())  {
          LOG.debug("  - Table '" + tableName + "'");
        }
        stmtExec.execute("drop table " + tableName);
      }
      rs.close();
    } finally  {
      stmtSel.close();
      stmtExec.close();
    }
  }

  /**
   * For the derby database, an eFaps sql table is created in this steps:
   * <ul>
   * <li>sql table itself with column <code>ID</code> and unique key on the
   *     column is created</li>
   * <li>if the table is an autoincrement table (parent table is
   *     <code>null</code>, the column <code>ID</code> is set as autoincrement
   *     column</li>
   * <li>if no parent table is defined, the foreign key to the parent table is
   *     automatically set</li>
   * </ul>
   *
   * @throws SQLException if the table could not be created
   */
  public void createTable(final Connection _con, final String _table,
          final String _parentTable) throws SQLException  {

    Statement stmt = _con.createStatement();

    try  {

      // create table itself
      StringBuilder cmd = new StringBuilder();
      cmd.append("create table ").append(_table).append(" (")
         .append("  ID bigint not null");

      // autoincrement
      if (_parentTable == null)  {
        cmd.append(" generated always as identity (start with 1, increment by 1)");
      }

      cmd.append(",")
         .append("  constraint ").append(_table).append("_UK_ID unique(ID)");

      // foreign key to parent sql table
      if (_parentTable != null)  {
        cmd.append(",")
           .append("constraint ").append(_table).append("_FK_ID ")
           .append("  foreign key(ID) ")
           .append("  references ").append(_parentTable).append("(ID)");
      }

      cmd.append(")");
      stmt.executeUpdate(cmd.toString());

    } finally  {
      stmt.close();
    }
  }

  /**
   * @return always <i>true</i> because supported by Derby database
   */
  public boolean supportsGetGeneratedKeys()  {
    return true;
  }

  /**
   * @return always <i>true</i> because supported by PostgreSQL database
   */
  public boolean supportsBinaryInputStream()  {
    return false;
  }
}