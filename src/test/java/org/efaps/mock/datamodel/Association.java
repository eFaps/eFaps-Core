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
package org.efaps.mock.datamodel;

import java.util.List;

import org.efaps.test.EFapsQueryHandler;

import acolyte.jdbc.QueryResult;
import acolyte.jdbc.RowLists;
import acolyte.jdbc.StatementHandler.Parameter;

public class Association
    extends AbstractType
{

    private static final String SQLDEFAULT = "select T0.ID from T_CMASSOC T0 where ( T0.ID_COL in (  select S0T0.AssociationLink_COL from T_CMASSOCDEF S0T0 where ( S0T0.CompanyLink_COL = %s )  ) )";

    private static final String SQLQUERY = "select T0.ID from T_CMASSOC T0 where ( T0.ID_COL = %s )";

    private static final String SQLPRINT = "select T0.ID,T0.ID,T0.Name_COL,T0.UUID_COL from T_CMASSOC T0 where T0.ID in ( %s )";

    private final Long companyId;

    int index = 0;

    /**
     * Instantiates a new person.
     *
     * @param _builder the builder
     */
    private Association(final AssociationBuilder builder)
    {
        super(builder);
        companyId = builder.companyId;
    }

    @Override
    public String[] getSqls()
    {
        return new String[] {
                        String.format(SQLDEFAULT, companyId),
                        String.format(SQLQUERY, getId()),
                        String.format(SQLPRINT, getId()) };
    }

    @Override
    public QueryResult getResult()
    {
        index++;
        return switch (index) {
            case 1 -> {
                yield RowLists.rowList1(Long.class).append(getId()).asResult();
            }
            case 2 -> {
                yield RowLists.rowList1(Long.class).append(getId()).asResult();
            }
            case 3 -> {
                yield RowLists.rowList4(Long.class, Long.class, String.class, String.class)
                                .append(getId(), getId(), getName(), getUuid().toString())
                                .asResult();
            }
            default -> throw new IllegalArgumentException("Unexpected value: " + index);
        };
    }

    @Override
    public boolean applies(final String _sql,
                           final List<Parameter> _parameters)
    {
        return true;
    }

    /**
     * Builder.
     *
     * @return the person builder
     */
    public static AssociationBuilder builder()
    {
        return new AssociationBuilder();
    }

    /**
     * The Class PersonBuilder.
     */
    public static class AssociationBuilder
        extends AbstractBuilder<AssociationBuilder>
    {

        private Long companyId;

        public AssociationBuilder withCompanyId(final Long companyId)
        {
            this.companyId = companyId;
            return this;
        }

        /**
         * Builds the.
         *
         * @return the person
         */
        public Association build()
        {
            final var ret = new Association(this);
            EFapsQueryHandler.get().register(ret);
            return ret;
        }
    }
}
