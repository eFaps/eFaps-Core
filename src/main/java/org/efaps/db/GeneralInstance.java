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
package org.efaps.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.KernelSettings;
import org.efaps.db.store.AbstractStoreResource;
import org.efaps.db.store.JCRStoreResource;
import org.efaps.db.store.JDBCStoreResource;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.SQLDelete.DeleteDefintion;
import org.efaps.db.wrapper.SQLInsert;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.InfinispanCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GeneralInstance
{

    /**
     * Name of the Table.
     */
    public static final String TABLENAME = "T_CMGENINST";

    /**
     * Name of the ID Column.
     */
    public static final String IDCOLUMN = "ID";

    /**
     * Name of the Instance Type ID Column.
     */
    public static final String ISTYPECOLUMN = "INSTTYPEID";

    /**
     * Name of the Instance ID Column.
     */
    public static final String ISIDCOLUMN = "INSTID";

    /**
     * Name of the Exchange ID Column.
     */
    public static final String EXIDCOLUMN = "EXID";

    /**
     * Name of the Exchange System ID Column.
     */
    public static final String EXSYSIDCOLUMN = "EXSYSID";

    /**
     * SQL select statement to select a type from the database by its Name.
     */
    private static final String SQL = new SQLSelect()
                    .column(GeneralInstance.IDCOLUMN)
                    .column(GeneralInstance.EXSYSIDCOLUMN)
                    .column(GeneralInstance.EXIDCOLUMN)
                    .from(GeneralInstance.TABLENAME, 0)
                    .addPart(SQLPart.WHERE)
                    .addColumnPart(0, GeneralInstance.ISIDCOLUMN).addPart(SQLPart.EQUAL).addValuePart("?")
                    .addPart(SQLPart.AND)
                    .addColumnPart(0, GeneralInstance.ISTYPECOLUMN).addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    private static String CACHE = GeneralInstance.class.getName() + ".Cache";

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(GeneralInstance.class);

    /**
     * To make a Singleton.
     */
    private GeneralInstance()
    {
    }

    /**
     * @param instance Instance the GeneralInstance will be created for.
     * @param connection Connection the insert will be executed in
     * @return id of the new instance
     * @throws EFapsException on error
     */
    public static long insert(final Instance instance,
                              final ConnectionResource connection)
        throws EFapsException
    {
        long ret = 0;
        if (instance.isValid() && instance.getType().isGeneralInstance()) {
            try {
                final SQLInsert insert = Context.getDbType().newInsert(GeneralInstance.TABLENAME,
                                GeneralInstance.IDCOLUMN,
                                true);
                insert.column(GeneralInstance.ISTYPECOLUMN, instance.getType().getId());
                insert.column(GeneralInstance.ISIDCOLUMN, instance.getId());
                insert.column(GeneralInstance.EXIDCOLUMN, instance.getExchangeId(false));
                insert.column(GeneralInstance.EXSYSIDCOLUMN, instance.getExchangeSystemId(false));
                ret = insert.execute(connection);
                instance.setGeneralId(ret);
                instance.setGeneralised(true);
            } catch (final SQLException e) {
                GeneralInstance.LOG.error("executeOneStatement", e);
                throw new EFapsException(GeneralInstance.class, "create", e);
            }
        }
        return ret;
    }

    /**
     * @param instance Instance the id of the GeneralInstance will be retrieved
     *            for.
     * @throws EFapsException on error
     */
    protected static void generaliseInstance(final Instance instance)
        throws EFapsException
    {
        if (instance != null && instance.isValid() && instance.getType().isGeneralInstance()) {

            final var cache = InfinispanCache.get().<String, GenInstInfo>getCache(CACHE);

            final var cachedInfo = cache.get(instance.getOid());
            if (cachedInfo == null) {
                final Context context = Context.getThreadContext();
                GeneralInstance.generaliseInstance(instance, context.getConnectionResource());
                if (instance.isGeneralised()) {
                    final var info = new GenInstInfo();
                    info.setGeneralId(instance.getGeneralId());
                    info.setExchangeId(instance.getExchangeId());
                    info.setExchangeSystemId(instance.getExchangeSystemId());
                    int lifespan = 60;
                    if (EFapsSystemConfiguration.get().containsAttributeValue(KernelSettings.GENINSTCACHELIFESPAN)) {
                        lifespan = EFapsSystemConfiguration.get()
                                        .getAttributeValueAsInteger(KernelSettings.GENINSTCACHELIFESPAN);
                    }
                    cache.put(instance.getOid(), info, lifespan, TimeUnit.MINUTES);
                }
            } else {
                instance.setGeneralId(cachedInfo.getGeneralId());
                instance.setExchangeSystemId(cachedInfo.getExchangeSystemId());
                instance.setExchangeId(cachedInfo.getExchangeId());
                instance.setGeneralised(true);
            }
        }
    }

    /**
     * @param instance Instance the id of the GeneralInstance will be retrieved
     *            for.
     * @param connection Connection the query will be executed in
     * @throws EFapsException on error
     */
    private static void generaliseInstance(final Instance instance,
                                           final ConnectionResource connection)
        throws EFapsException
    {
        if (instance.isValid() && instance.getType().isGeneralInstance()) {
            PreparedStatement stmt = null;
            try {
                try {
                    stmt = connection.prepareStatement(GeneralInstance.SQL);
                    stmt.setLong(1, instance.getId());
                    stmt.setLong(2, instance.getType().getId());

                    final ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        instance.setGeneralId(rs.getLong(1));
                        instance.setExchangeSystemId(rs.getLong(2));
                        instance.setExchangeId(rs.getLong(3));
                        instance.setGeneralised(true);
                    }
                    rs.close();

                    if (GeneralInstance.LOG.isDebugEnabled()) {
                        GeneralInstance.LOG.debug(instance.toString());
                    }
                } catch (final SQLException e) {
                    GeneralInstance.LOG.error("executeOneStatement", e);
                    throw new EFapsException(GeneralInstance.class, "create", e);
                } finally {
                    if (stmt != null) {
                        stmt.close();
                    }
                }
            } catch (final SQLException e) {
                throw new EFapsException("generaliseInstance", e);
            }
        }
    }

    /**
     * @param instance instance the DeleteDefintions are wanted for
     * @param connection Connection to be used
     * @return List of DeleteDefintion
     * @throws EFapsException on error
     */
    public static Collection<? extends DeleteDefintion> getDeleteDefintion(final Instance instance,
                                                                           final ConnectionResource connection)
        throws EFapsException
    {
        final List<DeleteDefintion> ret = new ArrayList<>();
        if (instance.isValid() && instance.getType().isGeneralInstance() && !instance.getType().isHistory()) {
            GeneralInstance.generaliseInstance(instance, connection);
            final long id = instance.getGeneralId();
            if (id > 0) {
                if (instance.getType().getStoreId() > 0) {
                    ret.add(new DeleteDefintion(JDBCStoreResource.TABLENAME_STORE, "ID", id));
                    ret.add(new DeleteDefintion(JCRStoreResource.TABLENAME_STORE, "ID", id));
                    ret.add(new DeleteDefintion(AbstractStoreResource.TABLENAME_STORE, "ID", id));
                }
                ret.add(new DeleteDefintion(GeneralInstance.TABLENAME, "ID", id));
            }
        }
        return ret;
    }
}
