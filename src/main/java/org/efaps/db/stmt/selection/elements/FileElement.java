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

import org.efaps.admin.datamodel.Type;
import org.efaps.db.store.AbstractStoreResource;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;

public class FileElement
    extends AbstractGenInstElement<FileElement>
{

    private Integer columnIndex;

    public FileElement(final Type type)
    {
        super(type);
    }

    @Override
    public FileElement getThis()
    {
        return this;
    }

    @Override
    public void append2SQLSelect(final SQLSelect sqlSelect)
        throws EFapsException
    {
        super.append2SQLSelect(sqlSelect);
        final var genInstJoinIdx = getJoinGenInstTableIdx(sqlSelect);
        final var storeIdx = sqlSelect.getIndexer().getTableIdx(AbstractStoreResource.TABLENAME_STORE, "ID");
        if (storeIdx.isCreated()) {
            sqlSelect.leftJoin(AbstractStoreResource.TABLENAME_STORE, storeIdx.getIdx(), "ID", genInstJoinIdx.getIdx(),
                            "ID");
        }
        sqlSelect.column(storeIdx.getIdx(), "ID");
        if (getNext() != null && getNext() instanceof LabelElement) {
            columnIndex = sqlSelect.columnIndex(storeIdx.getIdx(), AbstractStoreResource.COLNAME_FILENAME);
        }
    }

    @Override
    public Object getObject(final Object[] _row)
        throws EFapsException
    {
        Object ret = null;
        if (getNext() != null && getNext() instanceof LabelElement) {
            ret = _row[columnIndex];
        }
        return callAuxillary(ret);
    }
}
