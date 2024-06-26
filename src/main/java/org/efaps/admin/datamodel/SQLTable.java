/*
 * Copyright © 2003 - 2024 The eFaps Team (-)
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
 */
package org.efaps.admin.datamodel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.efaps.db.Context;
import org.efaps.db.databases.information.TableInformation;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.efaps.util.cache.InfinispanCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * This is the class for the table description. The table description holds
 * information in which table attributes are stored.
 *
 * @author The eFaps Team
 *
 */
public final class SQLTable
    extends AbstractDataModelObject
    implements DBTable
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SQLTable.class);

    /**
     * This is the SQL select statement to select a role from the database by
     * ID.
     */
    private static final String SQL_ID = new SQLSelect()
                    .column("ID")
                    .column("UUID")
                    .column("NAME")
                    .column("SQLTABLE")
                    .column("SQLCOLUMNID")
                    .column("SQLCOLUMNTYPE")
                    .column("DMTABLEMAIN")
                    .from("V_ADMINSQLTABLE", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "ID").addPart(SQLPart.EQUAL).addValuePart("?").toString();

    /**
     * This is the SQL select statement to select a role from the database by
     * Name.
     */
    private static final String SQL_NAME = new SQLSelect()
                    .column("ID")
                    .column("UUID")
                    .column("NAME")
                    .column("SQLTABLE")
                    .column("SQLCOLUMNID")
                    .column("SQLCOLUMNTYPE")
                    .column("DMTABLEMAIN")
                    .from("V_ADMINSQLTABLE", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "NAME").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * This is the SQL select statement to select a role from the database by
     * UUID.
     */
    private static final String SQL_UUID = new SQLSelect()
                    .column("ID")
                    .column("UUID")
                    .column("NAME")
                    .column("SQLTABLE")
                    .column("SQLCOLUMNID")
                    .column("SQLCOLUMNTYPE")
                    .column("DMTABLEMAIN")
                    .from("V_ADMINSQLTABLE", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "UUID").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * Name of the Cache by UUID.
     */
    private static final String UUIDCACHE = SQLTable.class.getName() + ".UUID";

    /**
     * Name of the Cache by ID.
     */
    private static final String IDCACHE = SQLTable.class.getName() + ".ID";

    /**
     * Name of the Cache by Name.
     */
    private static final String NAMECACHE = SQLTable.class.getName() + ".Name";

    /**
     * Instance variable for the name of the SQL table.
     *
     * @see #getSqlTable
     */
    private final String sqlTable;

    /**
     * This instance variable stores the SQL column name of the id of a table.
     *
     * @see #getSqlColId
     */
    private final String sqlColId;

    /**
     * The instance variable stores the SQL column name of the type id.
     *
     * @see #getSqlColType
     */
    private final String sqlColType;

    /**
     * The instance variable stores the main table for this table instance. The
     * main table is the table, which holds the information about the SQL select
     * statement to get a new id. Also the main table must be inserted as first
     * insert (e.g. the id in the table with a main table has a foreign key to
     * the id of the main table).
     *
     * @see #getMainTable()
     */
    private Long mainTableId = null;

    /**
     * The instance variable stores all types which stores information in this
     * table.
     *
     * @see #getTypes()
     */
    private final Set<Long> typeIds = new HashSet<>();

    /**
     * The instance variables is set to <i>true</i> if this table is only a read
     * only SQL table. This means, that no insert and no update on this table is
     * allowed and made.
     *
     * @see #isReadOnly()
     */
    private boolean readOnly = false;

    /**
     * This is the constructor for class {@link Attribute}. Every instance of
     * class {@link Attribute} must have a name (parameter <i>_name</i>) and an
     * identifier (parameter <i>_id</i>).
     *
     * @param _con Connection
     * @param _id eFaps id of the SQL table
     * @param _uuid unique identifier
     * @param _name eFaps name of the SQL table
     * @param _sqlTable name of the SQL Table in the database
     * @param _sqlColId name of column for the id within SQL table
     * @param _sqlColType name of column for the type within SQL table
     * @throws SQLException on error
     */
    protected SQLTable(final long _id,
                       final String _uuid,
                       final String _name,
                       final String _sqlTable,
                       final String _sqlColId,
                       final String _sqlColType)
    {
        super(_id, _uuid, _name);
        this.sqlTable = _sqlTable.trim();
        this.sqlColId = _sqlColId.trim();
        this.sqlColType = _sqlColType != null ? _sqlColType.trim() : null;
    }

    /**
     * The instance method adds a new type to the type list.
     *
     * @param _typeId id of the Type to add
     * @see #types
     */
    protected void addType(final Long _typeId)
    {
        this.typeIds.add(_typeId);
    }

    /**
     * The instance method sets a new property value.
     *
     * @param _name name of the property
     * @param _value value of the property
     * @throws CacheReloadException on error
     */
    @Override
    protected void setProperty(final String _name,
                               final String _value)
        throws CacheReloadException
    {
        if ("ReadOnly".equals(_name)) {
            this.readOnly = "true".equalsIgnoreCase("true");
        }
    }

    /**
     * This is the getter method for instance variable {@link #sqlTable}.
     *
     * @return value of instance variable {@link #sqlTable}
     * @see #sqlTable
     */
    public String getSqlTable()
    {
        return this.sqlTable;
    }

    /**
     * This is the getter method for instance variable {@link #sqlColId}.
     *
     * @return value of instance variable {@link #sqlColId}
     * @see #sqlColId
     */
    public String getSqlColId()
    {
        return this.sqlColId;
    }

    /**
     * This is the getter method for instance variable {@link #sqlColType}.
     *
     * @return value of instance variable {@link #sqlColType}
     * @see #sqlColType
     */
    public String getSqlColType()
    {
        return this.sqlColType;
    }

    /**
     * This is the getter method for instance variable {@link #tableInformation}
     * .
     *
     * @return value of instance variable {@link #tableInformation}
     * @throws SQLException
     * @see #tableInformation
     */
    public TableInformation getTableInformation()
    {
        try {
            return Context.getDbType().getCachedTableInformation(this.sqlTable);
        } catch (final SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This is the getter method for instance variable {@link #mainTable}.
     *
     * @return value of instance variable {@link #mainTable}
     * @see #mainTable
     */
    public SQLTable getMainTable()
    {
        try {
            return this.mainTableId == null ? null : SQLTable.get(this.mainTableId);
        } catch (final CacheReloadException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    /**
     * This is the getter method for instance variable {@link #types}.
     *
     * @return value of instance variable {@link #types}
     * @see #types
     * @throws CacheReloadException on error
     */
    public Set<Type> getTypes()
        throws CacheReloadException
    {
        final Set<Type> ret = new HashSet<>();
        for (final Long id : this.typeIds) {
            ret.add(Type.get(id));
        }
        return Collections.unmodifiableSet(ret);
    }

    /**
     * This is the getter method for instance variable {@link #readOnly}.
     *
     * @return value of instance variable {@link #readOnly}
     * @see #readOnly
     */
    public boolean isReadOnly()
    {
        return this.readOnly;
    }

    protected Long getMainTableId()
    {
        return mainTableId;
    }

    protected void setMainTableId(Long mainTableId)
    {
        this.mainTableId = mainTableId;
    }

    protected Set<Long> getTypeIds()
    {
        return typeIds;
    }

    @Override
    protected void updateCache()
        throws CacheReloadException
    {
        cacheSQLTable(this);
    }

    @Override
    public boolean equals(final Object _obj)
    {
        final boolean ret;
        if (_obj instanceof SQLTable) {
            ret = ((SQLTable) _obj).getId() == getId();
        } else {
            ret = super.equals(_obj);
        }
        return ret;
    }

    @Override
    public int hashCode()
    {
        return Long.valueOf(getId()).intValue();
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     *
     * @param _class Clas that started the initialization
     */
    public static void initialize(final Class<?> _class)
    {
        InfinispanCache.get().<UUID, SQLTable>initCache(SQLTable.UUIDCACHE, SQLTable.LOG);
        InfinispanCache.get().<Long, SQLTable>initCache(SQLTable.IDCACHE, SQLTable.LOG);
        InfinispanCache.get().<String, SQLTable>initCache(SQLTable.NAMECACHE, SQLTable.LOG);
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     */
    public static void initialize()
    {
        SQLTable.initialize(SQLTable.class);
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class
     * {@link SQLTable}.
     *
     * @param _id id to search in the cache
     * @return instance of class {@link SQLTable}
     * @throws CacheReloadException on error
     * @see #getCache
     */
    public static SQLTable get(final long _id)
        throws CacheReloadException
    {
        final var cache = InfinispanCache.get().<Long, SQLTable>getCache(SQLTable.IDCACHE);
        if (!cache.containsKey(_id)) {
            SQLTable.getSQLTableFromDB(SQLTable.SQL_ID, _id);
        }
        return cache.get(_id);
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link SQLTable}.
     *
     * @param _name name to search in the cache
     * @return instance of class {@link SQLTable}
     * @throws CacheReloadException on error
     * @see #getCache
     */
    public static SQLTable get(final String _name)
        throws CacheReloadException
    {
        final var cache = InfinispanCache.get().<String, SQLTable>getCache(SQLTable.NAMECACHE);
        if (!cache.containsKey(_name)) {
            SQLTable.getSQLTableFromDB(SQLTable.SQL_NAME, _name);
        }
        return cache.get(_name);
    }

    /**
     * Returns for given parameter <i>_uuid</i> the instance of class
     * {@link SQLTable}.
     *
     * @param _uuid UUID the tanel is wanted for
     * @return instance of class {@link Type}
     * @throws CacheReloadException on error
     */
    public static SQLTable get(final UUID _uuid)
        throws CacheReloadException
    {
        final var cache = InfinispanCache.get().<UUID, SQLTable>getCache(SQLTable.UUIDCACHE);
        if (!cache.containsKey(_uuid)) {
            SQLTable.getSQLTableFromDB(SQLTable.SQL_UUID, String.valueOf(_uuid));
        }
        return cache.get(_uuid);
    }

    /**
     * @param _sqlTable SQLTable to be cached
     */
    @SuppressFBWarnings("RV_RETURN_VALUE_OF_put_IGNORE")
    private static void cacheSQLTable(final SQLTable _sqlTable)
    {

        final var cache4UUID = InfinispanCache.get().<UUID, SQLTable>getCache(SQLTable.UUIDCACHE);
        cache4UUID.put(_sqlTable.getUUID(), _sqlTable);

        final var nameCache = InfinispanCache.get().<String, SQLTable>getCache(SQLTable.NAMECACHE);
        nameCache.put(_sqlTable.getName(), _sqlTable);

        final var idCache = InfinispanCache.get().<Long, SQLTable>getCache(SQLTable.IDCACHE);
        idCache.put(_sqlTable.getId(), _sqlTable);

    }

    /**
     * @param _sql SQL Statement to be executed
     * @param _criteria filter criteria
     * @return true if successful
     * @throws CacheReloadException on error
     */
    private static boolean getSQLTableFromDB(final String _sql,
                                             final Object _criteria)
        throws CacheReloadException
    {
        LOG.info("Loading SQLTable from db by: {}", _criteria);
        final boolean ret = false;
        Connection con = null;
        try {
            SQLTable table = null;
            long tableMainId = 0;
            con = Context.getConnection();
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement(_sql);
                stmt.setObject(1, _criteria);
                final ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    final long id = rs.getLong(1);
                    final String name = rs.getString(3).trim();
                    table = new SQLTable(id,
                                    rs.getString(2),
                                    name,
                                    rs.getString(4),
                                    rs.getString(5),
                                    rs.getString(6));
                    tableMainId = rs.getLong(7);
                    SQLTable.cacheSQLTable(table);
                    SQLTable.LOG.debug("read SQLTable '{}' (id = {}))", name, id);
                }
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
            con.commit();
            con.close();
            if (table != null) {
                table.readFromDB4Properties();
                if (tableMainId > 0) {
                    table.mainTableId = tableMainId;
                }
                // needed due to cluster serialization that does not update
                // automatically
                SQLTable.cacheSQLTable(table);
            }
        } catch (final SQLException e) {
            throw new CacheReloadException("could not read sql tables", e);
        } catch (final EFapsException e) {
            throw new CacheReloadException("could not read sql tables", e);
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (final SQLException e) {
                throw new CacheReloadException("Cannot read a type for an attribute.", e);
            }
        }
        return ret;
    }
}
