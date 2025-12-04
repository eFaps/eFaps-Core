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
package org.efaps.db.stmt.print;

import java.util.EnumSet;
import java.util.UUID;

import org.efaps.admin.datamodel.Type;
import org.efaps.db.stmt.filter.Filter;
import org.efaps.db.stmt.runner.IFiltered;
import org.efaps.db.stmt.selection.Select;
import org.efaps.db.stmt.selection.Selection;
import org.efaps.db.stmt.selection.elements.CountElement;
import org.efaps.eql2.ICountQueryStatement;
import org.efaps.eql2.IStatement;
import org.efaps.eql2.IWhere;
import org.efaps.eql2.StmtFlag;
import org.efaps.util.EFapsException;
import org.efaps.util.UUIDUtil;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CountQuery
    extends AbstractPrint
    implements IFiltered
{

    private static final Logger LOG = LoggerFactory.getLogger(CountQuery.class);

    private final ICountQueryStatement eqlStmt;

    public CountQuery(final ICountQueryStatement eqlStmt,
                      final EnumSet<StmtFlag> flags)
        throws EFapsException
    {
        super(flags);
        this.eqlStmt = eqlStmt;
        for (final String typeStr : eqlStmt.getQuery().getTypes()) {
            final Type type;
            if (UUIDUtil.isUUID(typeStr)) {
                type = Type.get(UUID.fromString(typeStr));
            } else {
                type = Type.get(typeStr);
            }
            if (type.isAbstract()) {
                type.getChildTypes()
                                .stream()
                                .filter(t -> !t.isAbstract())
                                .forEach(this::addType);
            } else {
                addType(type);
            }
        }
        LOG.debug("Instanciated: {}", this);

        final var mainType = Selection.evalMainType(getTypes());
        final var selection = new Selection();
        final Select select = Select.get();
        select.getElements().add(new CountElement(getFlags()).setDBTable(mainType.getMainTable()));
        selection.getSelects().add(select);
        setSelection(selection);
    }

    @Override
    public Filter getFilter()
        throws CacheReloadException
    {
        final IWhere where = eqlStmt.getQuery().getWhere();
        return Filter.get(where, getTypes().toArray(new Type[getTypes().size()]));
    }

    @Override
    public IStatement<?> getStmt()
    {
        return eqlStmt;
    }

}
