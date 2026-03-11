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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.attributetype.ConsortiumLinkType;
import org.efaps.admin.user.Company;
import org.efaps.db.Context;
import org.efaps.db.wrapper.TableIndexer.TableIdx;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

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

    public static Set<CompanyCriterion> eval(final TableIdx tableIdx,
                                             final Attribute companyAttribute,
                                             boolean companyIndependent)
        throws EFapsException
    {
        final Set<CompanyCriterion> criteria = new HashSet<>();
        final boolean isConsortium = companyAttribute
                        .getAttributeType().getClassRepr().equals(ConsortiumLinkType.class);
        Set<Long> ids;
        if (companyIndependent) {
            if (isConsortium) {
                ids = Context.getThreadContext().getPerson().getCompanies().stream()
                                .flatMap(compId -> {
                                    try {
                                        return Company.get(compId).getConsortiums().stream();
                                    } catch (final CacheReloadException e) {
                                        return Arrays.asList(compId).stream();
                                    }
                                }).collect(Collectors.toSet());
            } else {
                ids = Context.getThreadContext().getPerson().getCompanies();
            }
        } else if (isConsortium) {
            ids = Context.getThreadContext().getCompany().getConsortiums();
        } else {
            ids = new HashSet<>();
            ids.add(Context.getThreadContext().getCompany().getId());
        }

        for (final var id : ids) {
            criteria.add(CompanyCriterion.of(tableIdx,
                            companyAttribute.getSqlColNames().get(0),
                            companyAttribute.getParentId(), id));
        }
        return criteria;
    }
}
