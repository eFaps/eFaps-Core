/*
 * Copyright 2003 - 2023 The eFaps Team
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
 */
package org.efaps.db.stmt.selection.elements;

import java.util.Set;

import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.GeneralInstance;
import org.efaps.db.stmt.filter.TypeCriterion;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.db.wrapper.TableIndexer.TableIdx;
import org.efaps.util.EFapsException;

public abstract class AbstractGenInstElement<T>
    extends AbstractDataElement<T>
    implements ITypeCriterion
{

    private final Type type;
    private boolean addTypeClause = false;

    public AbstractGenInstElement(final Type type)
    {
        this.type = type;
        setDBTable(type.getMainTable());
    }

    public Type getType()
    {
        return type;
    }

    public TableIdx getJoinGenInstTableIdx(final SQLSelect sqlSelect)
        throws EFapsException
    {
        final String tableName = ((SQLTable) getTable()).getSqlTable();
        final String joinTableName = GeneralInstance.TABLENAME;
        return sqlSelect.getIndexer().getTableIdx(joinTableName, tableName, GeneralInstance.ISIDCOLUMN,
                        GeneralInstance.ISTYPECOLUMN);
    }

    @Override
    public void append2SQLSelect(final SQLSelect sqlSelect)
        throws EFapsException
    {
        final var joinTableIdx = getJoinGenInstTableIdx(sqlSelect);
        if (joinTableIdx.isCreated()) {
            final TableIdx tableidx;
            if (getPrevious() != null && getPrevious() instanceof IJoinTableIdx) {
                tableidx = ((IJoinTableIdx) getPrevious()).getJoinTableIdx(sqlSelect);
            } else {
                tableidx = sqlSelect.getIndexer().getTableIdx(((SQLTable) getTable()).getSqlTable());
            }
            if (tableidx.isCreated()) {
                sqlSelect.from(tableidx.getTable(), tableidx.getIdx());
            }

            if (getType().getMainTable().getSqlColType() != null) {
                sqlSelect.leftJoin(GeneralInstance.TABLENAME, joinTableIdx.getIdx(),
                                new String[] { GeneralInstance.ISIDCOLUMN, GeneralInstance.ISTYPECOLUMN },
                                tableidx.getIdx(), new String[] { "ID", getType().getMainTable().getSqlColType() });
            } else {
                sqlSelect.leftJoin(GeneralInstance.TABLENAME, joinTableIdx.getIdx(), GeneralInstance.ISIDCOLUMN,
                                tableidx.getIdx(), "ID");
                addTypeClause = true;
            }
            sqlSelect.column(joinTableIdx.getIdx(), "ID");
        }
    }

    @Override
    public void add2TypeCriteria(final SQLSelect sqlSelect,
                                 final Set<TypeCriterion> typeCriterias)
        throws EFapsException
    {
        if (addTypeClause) {
            final TableIdx tableidx = getJoinGenInstTableIdx(sqlSelect);
            typeCriterias.add(TypeCriterion.of(tableidx, GeneralInstance.ISTYPECOLUMN, getType().getId()));

            for (final Type childType : getType().getChildTypes()) {
                typeCriterias.add(TypeCriterion.of(tableidx, GeneralInstance.ISTYPECOLUMN, childType.getId()));
            }
        }
    }
}
