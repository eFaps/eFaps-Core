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
package org.efaps.db.stmt.runner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.efaps.admin.access.user.AccessCache;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeType;
import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.index.Queue;
import org.efaps.db.Context;
import org.efaps.db.GeneralInstance;
import org.efaps.db.ICacheDefinition;
import org.efaps.db.Instance;
import org.efaps.db.QueryCache;
import org.efaps.db.QueryKey;
import org.efaps.db.QueryValue;
import org.efaps.db.stmt.delete.AbstractDelete;
import org.efaps.db.stmt.filter.AbstractCriterion;
import org.efaps.db.stmt.filter.AssociationCriterion;
import org.efaps.db.stmt.filter.CompanyCriterion;
import org.efaps.db.stmt.filter.Filter;
import org.efaps.db.stmt.filter.TypeCriterion;
import org.efaps.db.stmt.print.AbstractPrint;
import org.efaps.db.stmt.print.CountQuery;
import org.efaps.db.stmt.print.ListPrint;
import org.efaps.db.stmt.print.ObjectPrint;
import org.efaps.db.stmt.print.QueryPrint;
import org.efaps.db.stmt.selection.ISelectionProvider;
import org.efaps.db.stmt.selection.Select;
import org.efaps.db.stmt.selection.elements.AbstractDataElement;
import org.efaps.db.stmt.selection.elements.AbstractElement;
import org.efaps.db.stmt.selection.elements.ICriterion;
import org.efaps.db.stmt.selection.elements.IOrderable;
import org.efaps.db.stmt.update.AbstractObjectUpdate;
import org.efaps.db.stmt.update.AbstractUpdate;
import org.efaps.db.stmt.update.Insert;
import org.efaps.db.stmt.update.ListUpdate;
import org.efaps.db.stmt.update.ObjectUpdate;
import org.efaps.db.store.Resource;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.db.wrapper.AbstractSQLInsertUpdate;
import org.efaps.db.wrapper.SQLDelete;
import org.efaps.db.wrapper.SQLDelete.DeleteDefintion;
import org.efaps.db.wrapper.SQLInsert;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.db.wrapper.SQLSelect.SQLSelectPart;
import org.efaps.db.wrapper.SQLUpdate;
import org.efaps.db.wrapper.SQLWhere;
import org.efaps.db.wrapper.TableIndexer.TableIdx;
import org.efaps.eql.builder.Converter;
import org.efaps.eql2.Comparison;
import org.efaps.eql2.Connection;
import org.efaps.eql2.ILimit;
import org.efaps.eql2.IOffset;
import org.efaps.eql2.IOrder;
import org.efaps.eql2.IOrderElement;
import org.efaps.eql2.IPageable;
import org.efaps.eql2.IPrintQueryStatement;
import org.efaps.eql2.IStatement;
import org.efaps.eql2.IUpdateElement;
import org.efaps.eql2.IUpdateElementsStmt;
import org.efaps.eql2.StmtFlag;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class SQLRunner.
 *
 * @author The eFaps Team
 */
public class SQLRunner
    implements IEQLRunner
{

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(SQLRunner.class);

    /** The print. */
    private IRunnable runnable;

    /** The sql select. */
    private SQLSelect sqlSelect;

    /** The updatemap. */
    private final Map<SQLTable, AbstractSQLInsertUpdate<?>> updatemap = new LinkedHashMap<>();

    @Override
    public void prepare(final IRunnable _runnable)
        throws EFapsException
    {
        LOG.trace("Preparing: {}", this);
        runnable = _runnable;
        sqlSelect = new SQLSelect();
        if (isCount()) {
            prepareCount((CountQuery) _runnable);
        } else if (isPrint()) {
            preparePrint((AbstractPrint) _runnable);
        } else if (isInsert()) {
            prepareInsert();
        } else if (isDelete()) {
            prepareDelete();
        } else {
            prepareUpdate();
        }
    }

    private void prepareDelete()
        throws EFapsException
    {
        // Nothing to do yet
    }

    private void prepareUpdate()
        throws EFapsException
    {
        if (runnable instanceof final ObjectUpdate update) {
            prepareUpdate(update.getInstance().getType(), update.getInstance());
        } else if (runnable instanceof final ListUpdate update) {
            final Map<Type, List<Instance>> types = update.getInstances().stream()
                            .collect(Collectors.groupingBy(Instance::getType));
            for (final Entry<Type, List<Instance>> entry : types.entrySet()) {
                prepareUpdate(entry.getKey(), entry.getValue().stream().toArray(Instance[]::new));
            }
        }
    }

    private void prepareInsert()
        throws EFapsException
    {
        final Insert insert = (Insert) runnable;
        final Type type = insert.getType();
        final SQLTable mainTable = type.getMainTable();
        getSQLInsert(mainTable);
        prepareUpdate(type);
    }

    private void prepareUpdate(final Type type,
                               final Instance... _instances)
        throws EFapsException
    {
        final Iterator<?> iter = type.getAttributes().entrySet().iterator();
        while (iter.hasNext()) {
            final Map.Entry<?, ?> entry = (Map.Entry<?, ?>) iter.next();
            final Attribute attr = (Attribute) entry.getValue();
            final AttributeType attrType = attr.getAttributeType();
            if (_instances.length == 0 && attrType.isCreateUpdate() || attrType.isAlwaysUpdate()) {
                try {
                    final SQLTable sqlTable = attr.getTable();
                    if (_instances.length == 0) {
                        final SQLInsert sqlInsert = getSQLInsert(sqlTable);
                        attr.prepareDBInsert(sqlInsert);
                    } else {
                        final SQLUpdate sqlUpdate = getSQLUpdate(type, sqlTable);
                        attr.prepareDBUpdate(sqlUpdate);
                    }
                } catch (final SQLException e) {
                    throw new EFapsException(SQLRunner.class, "prepareInsert", e);
                }
            }
            // if the attribute is in another table it must be added
            if (_instances.length == 0 && !updatemap.containsKey(attr.getTable())) {
                getSQLInsert(attr.getTable());
            }
        }

        final IUpdateElementsStmt<?> eqlStmt = ((AbstractUpdate) runnable).getEqlStmt();
        for (final IUpdateElement element : eqlStmt.getUpdateElements()) {
            final Attribute attr = type.getAttribute(element.getAttribute());
            if (attr == null) {
                LOG.error("Could not get Attribute for element attr {} and type {}", element.getAttribute(), type);
            }
            final SQLTable sqlTable = attr.getTable();
            try {
                if (_instances.length == 0) {
                    attr.getAttributeType().getDbAttrType().validate4Insert(attr, Instance.get(type, 0),
                                    new Object[] { element.getValue() });
                    final SQLInsert sqlInsert = getSQLInsert(sqlTable);
                    attr.prepareDBInsert(sqlInsert, Converter.convertEql(element.getValue()));
                } else {
                    for (final Instance instance : _instances) {
                        attr.getAttributeType().getDbAttrType().validate4Update(attr, instance,
                                        new Object[] { Converter.convertEql(element.getValue()) });
                    }
                    final SQLUpdate sqlUpdate = getSQLUpdate(type, sqlTable);
                    attr.prepareDBUpdate(sqlUpdate, Converter.convertEql(element.getValue()));
                }
            } catch (final SQLException e) {
                throw new EFapsException(SQLRunner.class, "prepareUpdate", e);
            }
        }
    }

    private SQLInsert getSQLInsert(final SQLTable _sqlTable)
    {
        SQLInsert ret;
        if (updatemap.containsKey(_sqlTable)) {
            ret = (SQLInsert) updatemap.get(_sqlTable);
        } else {
            ret = Context.getDbType().newInsert(_sqlTable.getSqlTable(), _sqlTable.getSqlColId(), updatemap
                            .isEmpty());
            updatemap.put(_sqlTable, ret);
        }
        return ret;
    }

    private SQLUpdate getSQLUpdate(final Type _type,
                                   final SQLTable _sqlTable)
    {
        SQLUpdate ret;
        if (updatemap.containsKey(_sqlTable)) {
            ret = (SQLUpdate) updatemap.get(_sqlTable);
        } else {
            final AbstractUpdate update = (AbstractUpdate) runnable;
            final Long[] ids;
            if (update instanceof AbstractObjectUpdate) {
                ids = new Long[] { ((AbstractObjectUpdate) update).getInstance().getId() };
            } else if (update instanceof ListUpdate) {
                ids = ((ListUpdate) update).getInstances().stream()
                                .filter(instance -> instance.getType().equals(_type))
                                .map(Instance::getId)
                                .toArray(Long[]::new);
            } else {
                ids = new Long[] { 0L };
            }
            ret = Context.getDbType().newUpdate(_sqlTable.getSqlTable(), _sqlTable.getSqlColId(), ids);
            updatemap.put(_sqlTable, ret);
        }
        return ret;
    }

    private void prepareCount(final CountQuery count)
        throws EFapsException
    {
        count.getStmt();
        final Set<AbstractCriterion> criteria = new HashSet<>();
        for (final Select select : count.getSelection().getAllSelects()) {
            for (final AbstractElement<?> element : select.getElements()) {
                if (element instanceof AbstractDataElement) {
                    ((AbstractDataElement<?>) element).append2SQLSelect(sqlSelect);
                }
                if (element instanceof final ICriterion criterionElement) {
                    criterionElement.add2Criteria(sqlSelect, criteria);
                }
            }
        }
        addBaseTypeCriteria(count, criteria);
        addBaseCompanyCriteria(count, criteria);
        addBaseAssociationCriteria(count, criteria);
        addWhere4QueryPrint(count, criteria);
    }

    /**
     * Prepare print.
     *
     * @param _print the print
     * @throws EFapsException the e faps exception
     */
    private void preparePrint(final AbstractPrint _print)
        throws EFapsException
    {
        final IStatement<?> stmt = _print.getStmt();
        IOrder order = null;
        ILimit limit = null;
        IOffset offset = null;
        if (stmt instanceof IPrintQueryStatement) {
            order = ((IPrintQueryStatement) stmt).getOrder();
        }
        if (stmt instanceof IPageable) {
            limit = ((IPageable<?>) stmt).getQuery().getLimit();
            offset = ((IPageable<?>) stmt).getQuery().getOffset();
        }

        final Set<AbstractCriterion> criteria = new HashSet<>();
        int idx = 1;
        boolean isSquash = false;
        for (final Select select : _print.getSelection().getAllSelects()) {
            if (!isSquash) {
                isSquash = select.isSquash();
            }
            for (final AbstractElement<?> element : select.getElements()) {
                if (element instanceof AbstractDataElement) {
                    ((AbstractDataElement<?>) element).append2SQLSelect(sqlSelect);
                }
                if (element instanceof final ICriterion criterionElement) {
                    criterionElement.add2Criteria(sqlSelect, criteria);
                }
            }

            if (order != null) {
                int orderIdx = 0;
                for (final IOrderElement orderElement : order.getElementsList()) {
                    if (orderElement.getKey().equals(select.getAlias())
                                    || orderElement.getKey().equals(String.valueOf(idx))) {
                        final List<AbstractElement<?>> orderables = select.getElements().stream()
                                        .filter(IOrderable.class::isInstance)
                                        .collect(Collectors.toList());
                        if (orderables.isEmpty()) {
                            LOG.warn("Cannot add order for Key: {}", orderElement);
                        } else {
                            ((IOrderable) orderables.get(orderables.size() - 1)).append2SQLOrder(orderIdx,
                                            sqlSelect.getOrder(),
                                            orderElement.isDesc());
                        }
                        break;
                    }
                    orderIdx++;
                }
            }
            if (limit != null) {
                sqlSelect.limit(Integer.valueOf(limit.getValue()));
            }
            if (offset != null) {
                sqlSelect.offset(Integer.valueOf(offset.getValue()));
            }
            idx++;
        }
        if (order != null && !sqlSelect.hasOrder()) {
            LOG.warn("Could not add order for: '{}'", order.eqlStmt());
        }

        if (sqlSelect.getColumns().size() > 0) {
            addBaseCompanyCriteria(_print, criteria);
            addBaseAssociationCriteria(_print, criteria);
            if (_print instanceof ObjectPrint) {
                addWhere4ObjectPrint((ObjectPrint) _print);
                if (!criteria.isEmpty()) {
                    Filter.get(_print.getFlags(), null).addCriteria(sqlSelect, criteria);
                }
            } else if (_print instanceof ListPrint) {
                addWhere4ListPrint((ListPrint) _print);
                if (!criteria.isEmpty()) {
                    Filter.get(_print.getFlags(), null).addCriteria(sqlSelect, criteria);
                }
            } else {
                addBaseTypeCriteria(_print, criteria);
                addWhere4QueryPrint((QueryPrint) _print, criteria);
            }
        }
        sqlSelect.setSquash(isSquash);
    }

    /**
     * Checks if is prints the.
     *
     * @return true, if is prints the
     */
    private boolean isPrint()
    {
        return runnable instanceof AbstractPrint;
    }

    private boolean isInsert()
    {
        return runnable instanceof Insert;
    }

    private boolean isDelete()
    {
        return runnable instanceof AbstractDelete;
    }

    private boolean isCount()
    {
        return runnable instanceof CountQuery;
    }

    /**
     * Adds the company criteria.
     *
     * @param _print the print
     * @throws EFapsException the e faps exception
     */
    private void addBaseCompanyCriteria(final AbstractPrint print,
                                        final Set<AbstractCriterion> criteria)
        throws EFapsException
    {
        final List<Type> types = print.getTypes().stream().sorted(Comparator.comparing(Type::getId))
                        .collect(Collectors.toList());
        for (final Type type : types) {
            if (type.isCompanyDependent()) {
                final TableIdx tableIdx = evalTableIdx(type.getCompanyAttribute());
                criteria.addAll(CompanyCriterion.eval(tableIdx, type.getCompanyAttribute(),
                                print.has(StmtFlag.COMPANYINDEPENDENT)));
            }
        }

    }

    /**
     * Adds the company criteria.
     *
     * @param _print the print
     * @throws EFapsException the e faps exception
     */
    private void addBaseAssociationCriteria(final AbstractPrint print, final Set<AbstractCriterion> criteria)
        throws EFapsException
    {
        final List<Type> types = print.getTypes().stream().sorted(Comparator.comparing(Type::getId))
                        .collect(Collectors.toList());
        for (final Type type : types) {
            if (type.hasAssociation()) {
                final TableIdx tableIdx = evalTableIdx(type.getAssociationAttribute());
                criteria.addAll(AssociationCriterion.eval(tableIdx, type.getAssociationAttribute(), print.has(StmtFlag.COMPANYINDEPENDENT)));
            }
        }
    }

    private TableIdx evalTableIdx(final Attribute _attribute)
    {
        final String table = _attribute.getTable().getSqlTable();
        final TableIdx tableIdx = sqlSelect.getIndexer().getTableIdx(table);
        TableIdx mainTableIdx = null;
        if (_attribute.getTable().getMainTable() != null) {
            final String mainTable = _attribute.getTable().getMainTable().getSqlTable();
            mainTableIdx = sqlSelect.getIndexer().getTableIdx(mainTable);
            if (mainTableIdx.isCreated()) {
                sqlSelect.from(mainTableIdx.getTable(), mainTableIdx.getIdx());
            }
        }
        if (tableIdx.isCreated()) {
            if (mainTableIdx == null) {
                sqlSelect.from(tableIdx.getTable(), tableIdx.getIdx());
            } else {
                sqlSelect.leftJoin(tableIdx.getTable(), tableIdx.getIdx(), "ID", mainTableIdx.getIdx(), "ID");
            }
        }
        return tableIdx;
    }

    /**
     * Adds the base type criteria.
     *
     * @param _print the print
     * @param typeCriteria2
     */
    private void addBaseTypeCriteria(final AbstractPrint print,
                                     final Set<AbstractCriterion> criteria)
    {
        final List<Type> types = print.getTypes().stream().collect(Collectors.toList());
        for (final Type type : types) {
            final String tableName = type.getMainTable().getSqlTable();
            final TableIdx tableIdx = sqlSelect.getIndexer().getTableIdx(tableName);
            if (tableIdx.isCreated()) {
                sqlSelect.from(tableIdx.getTable(), tableIdx.getIdx());
            }
            if (type.getMainTable().getSqlColType() != null) {
                criteria.add(TypeCriterion.of(tableIdx, type.getMainTable().getSqlColType(), type.getId()));
            }
        }
    }

    /**
     * Adds the where.
     *
     * @throws CacheReloadException on error
     */
    private void addWhere4QueryPrint(final IFiltered filtered,
                                     final Set<AbstractCriterion> Criteria)
        throws EFapsException
    {
        final Filter filter = filtered.getFilter();
        filter.append2SQLSelect(sqlSelect, Criteria);
    }

    /**
     * Adds the where.
     */
    private void addWhere4ObjectPrint(final ObjectPrint _print)
    {
        final SQLWhere where = sqlSelect.getWhere();
        where.addCriteria(0, "ID", Comparison.EQUAL, String.valueOf(_print.getInstance().getId()), Connection.AND);
    }

    /**
     * Adds the where.
     */
    private void addWhere4ListPrint(final ListPrint _print)
    {
        final SQLSelectPart currentPart = sqlSelect.getCurrentPart();
        if (currentPart == null) {
            sqlSelect.addPart(SQLPart.WHERE);
        } else {
            sqlSelect.addPart(SQLPart.AND);
        }
        sqlSelect.addColumnPart(0, "ID")
                        .addPart(SQLPart.IN)
                        .addPart(SQLPart.PARENTHESIS_OPEN)
                        .addValuePart(_print.getInstances().stream()
                                        .map(instance -> String.valueOf(instance.getId()))
                                        .collect(Collectors.joining(SQLPart.COMMA.getDefaultValue())))
                        .addPart(SQLPart.PARENTHESIS_CLOSE);
    }

    @Override
    public void execute()
        throws EFapsException
    {
        LOG.trace("Executing: {}", this);
        if (isPrint()) {
            executeSQLStmt((ISelectionProvider) runnable, sqlSelect.getSQL());
        } else if (isInsert()) {
            executeInserts();
        } else if (isDelete()) {
            executeDeletes();
        } else {
            executeUpdates();
        }
    }

    /**
     * Execute the inserts.
     *
     * @throws EFapsException the e faps exception
     */
    private void executeDeletes()
        throws EFapsException
    {
        final AbstractDelete delete = (AbstractDelete) runnable;
        for (final Instance instance : delete.getInstances()) {
            final Context context = Context.getThreadContext();
            final ConnectionResource con = context.getConnectionResource();
            // first remove the storeresource, because the information needed
            // from the general
            // instance to actually delete will be removed in the second step
            Resource storeRsrc = null;
            try {
                if (instance.getType().hasStore()) {
                    storeRsrc = context.getStoreResource(instance, Resource.StoreEvent.DELETE);
                    storeRsrc.delete();
                }
            } finally {
                if (storeRsrc != null && storeRsrc.isOpened()) {
                }
            }
            try {
                delete.triggerListeners();

                final List<DeleteDefintion> defs = new ArrayList<>();
                defs.addAll(GeneralInstance.getDeleteDefintion(instance, con));
                final SQLTable mainTable = instance.getType().getMainTable();
                for (final SQLTable curTable : instance.getType().getTables()) {
                    if (!curTable.equals(mainTable) && !curTable.isReadOnly()) {
                        defs.add(new DeleteDefintion(curTable.getSqlTable(), curTable.getSqlColId(), instance.getId()));
                    }
                }
                defs.add(new DeleteDefintion(mainTable.getSqlTable(), mainTable.getSqlColId(), instance.getId()));
                final SQLDelete sqlDelete = Context.getDbType().newDelete(defs.toArray(new DeleteDefintion[defs
                                .size()]));
                sqlDelete.execute(con);
                AccessCache.registerUpdate(instance);
                Queue.registerUpdate(instance);
            } catch (final SQLException e) {
                throw new EFapsException(getClass(), "executeWithoutAccessCheck.SQLException", e, instance);
            }
        }
    }

    /**
     * Execute the update.
     *
     * @throws EFapsException the e faps exception
     */
    private void executeUpdates()
        throws EFapsException
    {
        ConnectionResource con = null;
        try {

            con = Context.getThreadContext().getConnectionResource();
            for (final Entry<SQLTable, AbstractSQLInsertUpdate<?>> entry : updatemap.entrySet()) {
                ((SQLUpdate) entry.getValue()).execute(con);
            }
            if (runnable instanceof final ObjectUpdate update) {
                update.triggerListeners();
                Queue.registerUpdate(update.getInstance());
            } else if (runnable instanceof final ListUpdate listUpdate) {
                listUpdate.triggerListeners();
                listUpdate.getInstances().forEach(instance -> {
                    Queue.registerUpdate(instance);
                });
            }
        } catch (final SQLException e) {
            throw new EFapsException(SQLRunner.class, "executeOneCompleteStmt", e);
        }
    }

    /**
     * Execute the inserts.
     *
     * @throws EFapsException the e faps exception
     */
    private void executeInserts()
        throws EFapsException
    {
        ConnectionResource con = null;
        try {
            final Insert insert = (Insert) runnable;
            con = Context.getThreadContext().getConnectionResource();
            long id = 0;
            for (final Entry<SQLTable, AbstractSQLInsertUpdate<?>> entry : updatemap.entrySet()) {
                if (id != 0) {
                    entry.getValue().column(entry.getKey().getSqlColId(), id);
                }
                if (entry.getKey().getSqlColType() != null) {
                    entry.getValue().column(entry.getKey().getSqlColType(), insert.getType().getId());
                }
                final Long created = ((SQLInsert) entry.getValue()).execute(con);
                if (created != null) {
                    id = created;
                    insert.evaluateInstance(created);
                }
            }
            GeneralInstance.insert(insert.getInstance(), con);
            insert.triggerListeners();
            Queue.registerUpdate(insert.getInstance());
        } catch (final SQLException e) {
            throw new EFapsException(SQLRunner.class, "executeOneCompleteStmt", e);
        }
    }

    /**
     * Execute SQL stmt.
     *
     * @param _sqlProvider the sql provider
     * @param _complStmt the compl stmt
     * @return true, if successful
     * @throws EFapsException the e faps exception
     */
    @SuppressWarnings("unchecked")
    protected boolean executeSQLStmt(final ISelectionProvider _sqlProvider,
                                     final String _complStmt)
        throws EFapsException
    {
        SQLRunner.LOG.debug("SQL-Statement: {}", _complStmt);

        boolean ret = false;
        List<Object[]> rows = new ArrayList<>();

        boolean cached = false;
        if (runnable.has(StmtFlag.REQCACHED)) {
            final QueryKey querykey = QueryKey.get(Context.getThreadContext().getRequestId(), _complStmt);
            final var cache = QueryCache.get();
            if (cache.containsKey(querykey)) {
                final var queryValue = cache.get(querykey);
                if (queryValue.getContent() instanceof List) {
                    rows = (List<Object[]>) queryValue.getContent();
                }
                cached = true;
            }
        }
        if (!cached) {
            ConnectionResource con = null;
            try {
                con = Context.getThreadContext().getConnectionResource();
                final Statement stmt = con.createStatement();
                final ResultSet rs = stmt.executeQuery(_complStmt);
                final ArrayListHandler handler = new ArrayListHandler(Context.getDbType().getRowProcessor());
                rows = handler.handle(rs);
                rs.close();
                stmt.close();
            } catch (final SQLException e) {
                LOG.error("SQL Statment threw error: {}", _complStmt);
                throw new EFapsException(SQLRunner.class, "executeOneCompleteStmt", e);
            }
            if (runnable.has(StmtFlag.REQCACHED)) {
                final ICacheDefinition cacheDefinition = new ICacheDefinition()
                {

                    @Override
                    public long getLifespan()
                    {
                        return 5;
                    }

                    @Override
                    public TimeUnit getLifespanUnit()
                    {
                        return TimeUnit.MINUTES;
                    }
                };
                QueryCache.put(cacheDefinition, QueryKey.get(Context.getThreadContext().getRequestId(), _complStmt),
                                QueryValue.get(Context.getThreadContext().getRequestId(), rows));
            }
        }
        for (final Object[] row : rows) {
            for (final Select select : _sqlProvider.getSelection().getAllSelects()) {
                select.addObject(row);
            }
            ret = true;
        }
        return ret;
    }
}
