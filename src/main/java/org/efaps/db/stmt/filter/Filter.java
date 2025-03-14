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
package org.efaps.db.stmt.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.comparators.ComparatorChain;
import org.apache.commons.lang3.StringUtils;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Status;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.attributetype.IAttributeType;
import org.efaps.admin.datamodel.attributetype.LinkType;
import org.efaps.admin.datamodel.attributetype.LongType;
import org.efaps.admin.datamodel.attributetype.StatusType;
import org.efaps.db.Instance;
import org.efaps.db.stmt.selection.elements.LinktoElement;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.db.wrapper.SQLSelect.FromTableLeftJoin;
import org.efaps.db.wrapper.SQLWhere;
import org.efaps.db.wrapper.SQLWhere.Criteria;
import org.efaps.db.wrapper.SQLWhere.Group;
import org.efaps.db.wrapper.SQLWhere.Section;
import org.efaps.db.wrapper.TableIndexer.TableIdx;
import org.efaps.eql2.Comparison;
import org.efaps.eql2.Connection;
import org.efaps.eql2.IAttributeSelectElement;
import org.efaps.eql2.IBaseSelectElement;
import org.efaps.eql2.IClassSelectElement;
import org.efaps.eql2.ILinktoSelectElement;
import org.efaps.eql2.ISelectElement;
import org.efaps.eql2.IWhere;
import org.efaps.eql2.IWhereElement;
import org.efaps.eql2.IWhereElementTerm;
import org.efaps.eql2.IWhereGroupTerm;
import org.efaps.eql2.IWhereSelect;
import org.efaps.eql2.IWhereTerm;
import org.efaps.util.EFapsException;
import org.efaps.util.UUIDUtil;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Filter.
 */
public class Filter
{

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(Filter.class);

    /** The i where. */
    private IWhere iWhere;

    /** The types. */
    private List<Type> types;

    private Map<Type, TableIdx> type2tableIdx;

    /**
     * Analyze.
     *
     * @param _where the where
     * @param _types the types
     * @return the filter
     */
    private Filter analyze(final IWhere _where,
                           final List<Type> _types)
    {
        iWhere = _where;
        types = _types;
        return this;
    }

    public void append2SQLSelect(final SQLSelect _sqlSelect,
                                 final Map<Type, TableIdx> _type2tableIdx)
        throws EFapsException
    {
        type2tableIdx = _type2tableIdx;
        append2SQLSelect(_sqlSelect, Collections.emptySet());
    }

    /**
     * Append two SQL select.
     *
     * @param _sqlSelect the sql select
     * @throws EFapsException
     */
    public void append2SQLSelect(final SQLSelect sqlSelect,
                                 final Set<TypeCriterion> typeCriteria)
        throws EFapsException
    {
        if (iWhere != null) {
            analyzeTerms(sqlSelect, iWhere.getTerms(), sqlSelect.getWhere().getSections());
        }
        addTypeCriteria(sqlSelect, typeCriteria);
    }

    protected void analyzeTerms(final SQLSelect sqlSelect,
                                final IWhereTerm<?>[] whereTerms,
                                final List<Section> sections)
        throws EFapsException
    {
        for (final IWhereTerm<?> term : whereTerms) {
            if (term instanceof IWhereElementTerm) {
                final IWhereElement element = ((IWhereElementTerm) term).getElement();
                if (element.getNestedQuery() != null) {
                    final NestedQuery nestedQuery = new NestedQuery(element);
                    sections.addAll(nestedQuery.append2SQLSelect(types, sqlSelect, term));
                } else if (element.getAttribute() != null) {
                    sections.add(attribute(sqlSelect, term.getConnection(), element, null, null));
                } else if (element.getSelect() != null) {
                    final IWhereSelect select = element.getSelect();
                    final List<ILinktoSelectElement> linktoElements = new ArrayList<>();
                    if (select.getElements(0) instanceof IClassSelectElement) {
                        sections.add(classSelect(sqlSelect, term.getConnection(), element));
                    } else {
                        for (final ISelectElement ele : select.getElements()) {
                            if (ele instanceof IBaseSelectElement) {
                                switch (((IBaseSelectElement) ele).getElement()) {
                                    case STATUS:
                                        sections.add(status(sqlSelect, term.getConnection(), element));
                                        break;
                                    default:
                                        break;
                                }
                                linktoElements.clear();
                            } else if (ele instanceof IAttributeSelectElement) {
                                sections.add(attribute(sqlSelect, term.getConnection(), element, ele, linktoElements));
                                linktoElements.clear();
                            } else if (ele instanceof ILinktoSelectElement) {
                                linktoElements.add((ILinktoSelectElement) ele);
                            }
                        }
                    }
                }
            } else if (term instanceof IWhereGroupTerm) {
                final var subTerms = ((IWhereGroupTerm) term).getTerms();
                final var group = new Group().setConnection(term.getConnection());
                sections.add(group);
                analyzeTerms(sqlSelect, subTerms, group);
            }
        }
    }

    protected Section classSelect(final SQLSelect mainSqlSelect,
                                  final Connection connection,
                                  final IWhereElement element)
        throws EFapsException
    {
        LOG.debug("Evaluation classSelectElement for where: {}", element);
        final IClassSelectElement classSelectElement = (IClassSelectElement) element.getSelect().getElements(0);
        Classification classification;
        if (UUIDUtil.isUUID(classSelectElement.getName())) {
            classification = Classification.get(UUID.fromString(classSelectElement.getName()));
        } else {
            classification = Classification.get(classSelectElement.getName());
        }
        final var type = classification.getClassifiesType();

        final SQLTable table = type.getMainTable();
        final String tableName = table.getSqlTable();
        final TableIdx tableidx = mainSqlSelect.getIndexer().getTableIdx(tableName);
        // if it was not already added
        if (tableidx.isCreated()) {
            mainSqlSelect.addTablePart(tableName, tableidx.getIdx());
            mainSqlSelect.column(tableidx.getIdx(), "ID");
        }

        // the inner classification part
        final var sqlSelect = new SQLSelect("C");
        final var classTable = classification.getMainTable();
        final var classTableName = classTable.getSqlTable();
        final var classTableIdx = sqlSelect.getIndexer().getTableIdx(classTableName);
        sqlSelect.addTablePart(classTableName, tableidx.getIdx());
        final var colName = classification.getAttribute(classification.getLinkAttributeName()).getSqlColNames().get(0);
        sqlSelect.column(classTableIdx.getIdx(), colName);

        final var subFilter = Filter.get(null, classification);
        final List<Section> subSections = new ArrayList<>();
        for (int i = 1; i < element.getSelect().getElements().length; i++) {
            final var ele = element.getSelect().getElements()[i];
            if (ele instanceof IBaseSelectElement) {
                switch (((IBaseSelectElement) ele).getElement()) {
                    default:
                        break;
                }
            } else if (ele instanceof IAttributeSelectElement) {
                subSections.add(subFilter.attribute(sqlSelect, Connection.AND, element, ele, new ArrayList<>()));
            }
        }
        sqlSelect.getWhere().getSections().addAll(subSections);

        return new Criteria()
                        .tableIndex(tableidx.getIdx())
                        .colNames(Collections.singletonList("ID"))
                        .comparison(Comparison.IN)
                        .values(Set.of(sqlSelect.toString()))
                        .escape(false)
                        .connection(connection);
    }

    protected Section attribute(final SQLSelect _sqlSelect,
                                final Connection connection,
                                final IWhereElement _element,
                                final ISelectElement _selectElement,
                                final List<ILinktoSelectElement> linktoElementStmts)
        throws EFapsException
    {
        Section ret = null;
        final String attrName;
        if (_selectElement != null && _selectElement instanceof IAttributeSelectElement) {
            attrName = ((IAttributeSelectElement) _selectElement).getName();
        } else {
            attrName = _element.getAttribute();
        }
        if (CollectionUtils.isNotEmpty(linktoElementStmts)) {
            final var linktoElements = new ArrayList<LinktoElement>();
            for (final var linktoElementStmt : linktoElementStmts) {
                final var linktoElement = new LinktoElement();
                if (linktoElements.isEmpty()) {
                    for (final Type type : types) {
                        final Attribute linktoAttr = type.getAttribute(linktoElementStmt.getName());
                        if (linktoAttr != null) {
                            linktoElement.setAttribute(linktoAttr);
                        }
                    }
                    linktoElements.add(linktoElement);
                } else {

                }
            }

            for (final var linktoElement : linktoElements) {
                linktoElement.append2SQLSelect(_sqlSelect);
            }
            final var last = linktoElements.get(linktoElements.size() - 1);
            final Type currentType = last.getAttribute().getLink();
            final var tableIdx = last.getJoinTableIdx(_sqlSelect);
            final Attribute attr = currentType.getAttribute(attrName);
            ret = attribute(_sqlSelect, attr, connection, _element, tableIdx, false);
        } else if (types.isEmpty() && type2tableIdx != null) {
            for (final var entry : type2tableIdx.entrySet()) {
                final Attribute attr = entry.getKey().getAttribute(attrName);
                if (attr != null) {
                    ret = attribute(_sqlSelect, attr, connection, _element, entry.getValue(), false);
                    break;
                }
            }
        } else {
            for (final Type type : types) {
                final Attribute attr = type.getAttribute(attrName);
                if (attr != null) {
                    ret = attribute(_sqlSelect, attr, connection, _element);
                    break;
                }
            }
        }
        return ret;
    }

    protected Section status(final SQLSelect _sqlSelect,
                             final Connection connection,
                             final IWhereElement _element)
    {

        Section ret = null;
        if (types.isEmpty() && type2tableIdx != null) {
            for (final var entry : type2tableIdx.entrySet()) {
                final Attribute attr = entry.getKey().getStatusAttribute();
                if (attr != null) {
                    ret = attribute(_sqlSelect, attr, connection, _element, entry.getValue(), true);
                    break;
                }
            }
        } else {
            for (final Type type : types) {
                final Attribute attr = type.getStatusAttribute();
                if (attr != null) {
                    ret = attribute(_sqlSelect, attr, connection, _element);
                    break;
                }
            }
        }
        return ret;
    }

    protected TableIdx tableIndex4Attr(final SQLSelect _sqlSelect,
                                       final Attribute _attr)
    {
        final SQLTable table = _attr.getTable();
        final TableIdx tableIdx;
        if (table.getMainTable() != null) {
            final var mainTableIdx = _sqlSelect.getIndexer().getTableIdx(table.getMainTable().getSqlTable());
            if (mainTableIdx.isCreated()) {
                _sqlSelect.from(mainTableIdx.getTable(), mainTableIdx.getIdx());
            }
            tableIdx = _sqlSelect.getIndexer().getTableIdx(table.getSqlTable(), table.getMainTable().getSqlTable(),
                            "ID");

            if (tableIdx.isCreated()) {
                _sqlSelect.leftJoin(tableIdx.getTable(), tableIdx.getIdx(), "ID", mainTableIdx.getIdx(), "ID");
            }

        } else {
            tableIdx = _sqlSelect.getIndexer().getTableIdx(table.getSqlTable());
            if (tableIdx.isCreated()) {
                _sqlSelect.from(tableIdx.getTable(), tableIdx.getIdx());
            }
        }
        return tableIdx;
    }

    protected Section attribute(final SQLSelect _sqlSelect,
                                final Attribute _attr,
                                final Connection connection,
                                final IWhereElement _element)
    {
        Section ret = null;
        if (_attr != null) {
            final var tableIdx = tableIndex4Attr(_sqlSelect, _attr);
            ret = attribute(_sqlSelect, _attr, connection, _element, tableIdx, false);
        }
        return ret;
    }

    protected Section attribute(final SQLSelect _sqlSelect,
                                final Attribute _attr,
                                final Connection connection,
                                final IWhereElement _element,
                                final TableIdx _tableIdx,
                                final boolean _nullable)
    {
        Section ret = null;
        if (_attr != null) {
            final IAttributeType attrType = _attr.getAttributeType().getDbAttrType();

            final boolean noEscape;
            final List<String> values;
            if (attrType instanceof StatusType) {
                values = _element.getValuesList().stream()
                                .map(val -> convertStatusValue(_attr, val))
                                .collect(Collectors.toList());
                noEscape = true;
            } else if (attrType instanceof LinkType) {
                noEscape = true;
                values = _element.getValuesList().stream()
                                .map(this::convertLinkValue)
                                .collect(Collectors.toList());
            } else {
                noEscape = attrType instanceof LongType;
                values = Arrays.asList(_element.getValues());
            }

            if (_nullable && !Comparison.NULL.equals(_element.getComparison())
                            && !Comparison.NOTNULL.equals(_element.getComparison())) {
                final Group group = new Group().setConnection(Connection.AND);
                group.add(new Criteria()
                                .tableIndex(_tableIdx.getIdx())
                                .colName(_attr.getSqlColNames().get(0))
                                .comparison(_element.getComparison())
                                .values(new LinkedHashSet<>(values))
                                .escape(!noEscape)
                                .connection(Connection.OR));
                group.add(new Criteria()
                                .tableIndex(_tableIdx.getIdx())
                                .colName(_attr.getSqlColNames().get(0))
                                .comparison(Comparison.NULL)
                                .connection(Connection.OR));
                ret = group;
            } else {
                ret = new Criteria()
                                .tableIndex(_tableIdx.getIdx())
                                .colNames(_attr.getSqlColNames())
                                .comparison(_element.getComparison())
                                .values(new LinkedHashSet<>(values))
                                .escape(!noEscape)
                                .connection(connection)
                                .setMain(false);
            }
        }
        return ret;
    }

    protected String convertStatusValue(final Attribute _attr,
                                        final String _val)
    {
        String ret;
        if (StringUtils.isNumeric(_val)) {
            ret = _val;
        } else {
            Status status = null;
            try {
                status = Status.find(_attr.getLink().getUUID(), _val);
            } catch (final CacheReloadException e) {
                LOG.error("Catched error:", e);
            } finally {
                if (status == null) {
                    LOG.warn("No Status could be found for the given key {} on {}", _val, _attr);
                }
                ret = status == null ? _val : String.valueOf(status.getId());
            }
        }
        return ret;
    }

    protected String convertLinkValue(final String _val)
    {
        String ret;
        if (StringUtils.isNumeric(_val)) {
            ret = _val;
        } else {
            final var instance = Instance.get(_val);
            if (!instance.isValid()) {
                LOG.error("Invalid value for where term on LinkType Attribute: {} ", _val);
            }
            ret = String.valueOf(instance.getId());
        }
        return ret;
    }

    public void addTypeCriteria(final SQLSelect _sqlSelect,
                                final Set<TypeCriterion> _typeCriteria)
    {
        if (!_typeCriteria.isEmpty()) {
            final ComparatorChain<TypeCriterion> chain = new ComparatorChain<>();
            chain.addComparator(Comparator.comparing(TypeCriterion::getTableIndex));
            chain.addComparator(Comparator.comparing(TypeCriterion::getTypeId));

            final SQLWhere where = _sqlSelect.getWhere();
            _typeCriteria.stream()
                            .sorted(chain)
                            .collect(Collectors.groupingBy(TypeCriterion::getTableIndex))
                            .forEach((index,
                                      criteria) -> {
                                final boolean nullable = criteria.stream()
                                                .filter(TypeCriterion::isNullable)
                                                .findAny()
                                                .isPresent();
                                final Set<String> values = new LinkedHashSet<>();
                                criteria.stream()
                                                .map(citerion -> String.valueOf(citerion.getTypeId()))
                                                .forEach(typeId -> values.add(typeId));

                                if (nullable) {
                                    final Group group = new Group().setConnection(Connection.AND);
                                    group.add(new Criteria()
                                                    .tableIndex(index.intValue())
                                                    .colName(criteria.get(0).getSqlColType())
                                                    .comparison(Comparison.EQUAL)
                                                    .values(values)
                                                    .connection(Connection.OR));
                                    group.add(new Criteria()
                                                    .tableIndex(index.intValue())
                                                    .colName(criteria.get(0).getSqlColType())
                                                    .comparison(Comparison.EQUAL)
                                                    .connection(Connection.OR));
                                    where.section(group);
                                } else {
                                    final var fromTable = _sqlSelect.getFromTables().stream()
                                                    .filter(ft -> (criteria.get(0).getTableIdx().getIdx() == ft
                                                                    .getTableIndex()))
                                                    .findFirst();
                                    if (fromTable.isPresent() && fromTable.get() instanceof FromTableLeftJoin) {
                                        ((FromTableLeftJoin) fromTable.get()).addTypeCriterias(criteria.get(0));
                                    } else {
                                        where.addCriteria(index.intValue(),
                                                        Collections.singletonList(criteria.get(0).getSqlColType()),
                                                        Comparison.EQUAL, values, false, Connection.AND).setMain(true);
                                    }
                                }
                            });
        }
    }

    /**
     * Gets the.
     *
     * @param _where the where
     * @param _baseTypes the base types
     * @return the selection
     * @throws CacheReloadException the cache reload exception
     */
    public static Filter get(final IWhere _where,
                             final Type... _baseTypes)
        throws CacheReloadException
    {
        return new Filter().analyze(_where, Arrays.asList(_baseTypes));
    }
}
