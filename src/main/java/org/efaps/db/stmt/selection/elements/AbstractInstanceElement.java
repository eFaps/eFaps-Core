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
package org.efaps.db.stmt.selection.elements;

import org.efaps.admin.datamodel.SQLTable;
import org.efaps.admin.datamodel.Type;
import org.efaps.db.Instance;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.db.wrapper.TableIndexer.TableIdx;
import org.efaps.util.EFapsException;

public abstract class AbstractInstanceElement<T>
    extends AbstractDataElement<T>
{

    /** The type id. */
    private final Long typeId;

    /** The col idxs. */
    private int idColIdxs = -1;

    /** The type col idxs. */
    private int typeColIdxs = -1;

    protected AbstractInstanceElement(final Type _type)
    {
        setDBTable(_type.getMainTable());
        this.typeId = _type.getId();
    }

    protected TableIdx getTableIdx(final SQLSelect sqlSelect)
        throws EFapsException
    {
        final SQLTable table = (SQLTable) getTable();
        final TableIdx tableidx;
        if (getPrevious() != null && getPrevious() instanceof IJoinTableIdx) {
            tableidx = ((IJoinTableIdx) getPrevious()).getJoinTableIdx(sqlSelect);
        } else {
            tableidx = sqlSelect.getIndexer().getTableIdx(table.getSqlTable());
        }
        this.idColIdxs = sqlSelect.columnIndex(tableidx.getIdx(), table.getSqlColId());
        if (table.getSqlColType() != null) {
            this.typeColIdxs = sqlSelect.columnIndex(tableidx.getIdx(), table.getSqlColType());
        }
        if (tableidx.isCreated()) {
            sqlSelect.from(tableidx.getTable(), tableidx.getIdx());
        }
        return tableidx;
    }

    @Override
    public void append2SQLSelect(final SQLSelect _sqlSelect)
        throws EFapsException
    {
        if (getTable() instanceof SQLTable) {
            getTableIdx(_sqlSelect);
        }
    }

    @Override
    public Object getObject(final Object[] _row)
        throws EFapsException
    {
        Object ret = null;
        if (_row != null) {
            final Long idObject = getLongValue(_row[this.idColIdxs]);
            Type type = null;
            if (idObject == null) {
                ret = null;
            } else {
                if (this.typeColIdxs > -1) {
                    final Long typeIdTemp = getLongValue(_row[this.typeColIdxs]);
                    if (typeIdTemp != null) {
                        type = Type.get(typeIdTemp);
                    }
                } else {
                    type = Type.get(this.typeId);
                }
                ret = type == null ? null : Instance.get(type, idObject);
            }
        }
        return ret;
    }
}
