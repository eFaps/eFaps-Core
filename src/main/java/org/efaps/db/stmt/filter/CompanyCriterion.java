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

import org.efaps.db.wrapper.TableIndexer.TableIdx;

public class CompanyCriterion
    extends AbstractCriterion
{

    private final long companyId;

    public CompanyCriterion(final TableIdx tableIdx,
                            final String sqlCol,
                            final long typeId,
                            final long companyId)
    {
        super(tableIdx, sqlCol, typeId, false);
        this.companyId = companyId;
    }

    @Override
    public long getValue()
    {
        return companyId;
    }

    @Override
    public boolean equals(final Object _obj)
    {
        final boolean ret;
        if (_obj instanceof final CompanyCriterion obj) {
            ret = getSqlCol().equals(obj.getSqlCol())
                            && getTypeId() == obj.getTypeId()
                            && isNullable() == obj.isNullable()
                            && getTableIdx().equals(obj.getTableIdx())
                            && getValue() == obj.getValue();
        } else {
            ret = super.equals(_obj);
        }
        return ret;
    }

    public static CompanyCriterion of(final TableIdx tableIdx,
                                      final String sqlCol,
                                      final long typeId,
                                      final long companyId)
    {
        return new CompanyCriterion(tableIdx, sqlCol, typeId, companyId);
    }
}
