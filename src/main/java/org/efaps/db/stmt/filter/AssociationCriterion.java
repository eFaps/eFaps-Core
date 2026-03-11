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

import java.util.HashSet;
import java.util.Set;

import org.efaps.admin.common.Association;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.db.Context;
import org.efaps.db.wrapper.TableIndexer.TableIdx;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssociationCriterion
    extends AbstractCriterion
{

    private static final Logger LOG = LoggerFactory.getLogger(AssociationCriterion.class);

    private final long associationId;

    public AssociationCriterion(final TableIdx tableIdx,
                                final String sqlCol,
                                final long typeId,
                                final long associationId)
    {
        super(tableIdx, sqlCol, typeId, false);
        this.associationId = associationId;
    }

    @Override
    public long getValue()
    {
        return associationId;
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

    public static AssociationCriterion of(final TableIdx tableIdx,
                                          final String sqlCol,
                                          final long typeId,
                                          final long companyId)
    {
        return new AssociationCriterion(tableIdx, sqlCol, typeId, companyId);
    }

    public static Set<AssociationCriterion> eval(final TableIdx tableIdx,
                                                 final Attribute associationAttribute,
                                                 boolean companyIndependent)
        throws EFapsException
    {
        final Set<AssociationCriterion> criteria = new HashSet<>();
        final Set<Long> ids = new HashSet<>();
        if (companyIndependent) {
            for (final Long companyId : Context.getThreadContext().getPerson().getCompanies()) {
                final var association = Association.evaluate(associationAttribute.getParent(), companyId);
                if (association == null) {
                    LOG.debug("No valid Association was found");
                    ids.add(0L);
                } else {
                    ids.add(association.getId());
                }
            }
        } else {
            final Association association = Association.evaluate(associationAttribute.getParent());
            if (association == null) {
                LOG.debug("No valid Association was found");
                ids.add(0L);
            } else {
                ids.add(association.getId());
            }
        }
        for (final var id : ids) {
            criteria.add(AssociationCriterion.of(tableIdx,
                            associationAttribute.getSqlColNames().get(0),
                            associationAttribute.getParentId(), id));
        }
        return criteria;
    }
}
