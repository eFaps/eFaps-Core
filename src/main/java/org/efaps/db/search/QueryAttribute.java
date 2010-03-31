/*
 * Copyright 2003 - 2010 The eFaps Team
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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */


package org.efaps.db.search;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.db.Context;
import org.efaps.db.InstanceQuery;


/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class QueryAttribute
    extends AbstractPart
{

    /**
     * Attribute this QueryAttribute is based on.
     */
    private Attribute attribute;

    /**
     * Name of the attribute this QueryAttribute is based on.
     */
    private final String attributeName;

    /**
     * Index of the table the attribute belongs to.
     */
    private Integer tableIndex;

    /**
     * @param _attribute Attribute
     */
    public QueryAttribute(final Attribute _attribute)
    {
        this.attribute = _attribute;
        this.attributeName = this.attribute.getName();
    }

    /**
     * @param _attributeName Name of the attribute
     */
    public QueryAttribute(final String _attributeName)
    {
        this.attributeName = _attributeName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractPart appendSQL(final StringBuilder _sql)
    {

        if (this.tableIndex != null)  {
            _sql.append('T').append(this.tableIndex).append('.');
        }
        _sql.append(Context.getDbType().getTableQuote())
            .append(this.attribute.getSqlColNames().get(0))
            .append(Context.getDbType().getTableQuote());
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractPart prepare(final InstanceQuery _query)
    {
        if (this.attribute == null) {
            this.attribute =  _query.getBaseType().getAttribute(this.attributeName);
        }
        this.tableIndex = _query.getSqlTable2Index().get(this.attribute.getTable());
        return this;
    }
}
