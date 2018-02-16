/*
 * Copyright 2003 - 2017 The eFaps Team
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

package org.efaps.db.wrapper;

import java.util.ArrayList;
import java.util.List;

import org.efaps.db.wrapper.SQLSelect.EscapedValue;
import org.efaps.eql2.Comparison;

/**
 * The Class SQLWhere.
 */
public class SQLWhere
{

    /** The criterias. */
    private final List<Criteria> criterias = new ArrayList<>();

    /**
     * Adds the criteria.
     *
     * @param _idx the idx
     * @param _sqlColNames the sql col names
     * @param _comparison the comparison
     * @param _values the values
     * @param _escape the escape
     */
    public void addCriteria(final int _idx, final ArrayList<String> _sqlColNames, final Comparison _comparison,
                            final String[] _values, final boolean _escape)
    {
        this.criterias.add(new Criteria(_idx, _sqlColNames, _comparison, _values, _escape));

    }

    /**
     * Append SQL.
     *
     * @param _tablePrefix the table prefix
     * @param _cmd the cmd
     */
    protected void appendSQL(final String _tablePrefix,
                             final StringBuilder _cmd)
    {
        for (final Criteria criteria : this.criterias) {
            int i = 0;
            for (final String colName : criteria.colNames) {
                new SQLSelect.Column(_tablePrefix, criteria.tableIndex, colName).appendSQL(_cmd);
                new SQLSelect.SQLSelectPart(SQLPart.SPACE).appendSQL(_cmd);
                switch (criteria.comparison) {
                    case EQUAL:
                        new SQLSelect.SQLSelectPart(SQLPart.EQUAL).appendSQL(_cmd);
                        break;
                    default:
                        break;
                }
                new SQLSelect.SQLSelectPart(SQLPart.SPACE).appendSQL(_cmd);
                if (criteria.escape) {
                    new EscapedValue(criteria.values[i]).appendSQL(_cmd);
                } else {
                    new SQLSelect.Value(criteria.values[i]).appendSQL(_cmd);
                }
                i++;
            }
        }
    }

    public static class Criteria
    {

        private final int tableIndex;
        private final ArrayList<String> colNames;
        private final Comparison comparison;
        private final String[] values;
        private final boolean escape;

        public Criteria(final int _tableIndex,
                        final ArrayList<String> _sqlColNames,
                        final Comparison _comparison,
                        final String[] _values,
                        final boolean _escape)
        {
            this.tableIndex = _tableIndex;
            this.colNames = _sqlColNames;
            this.comparison = _comparison;
            this.values = _values;
            this.escape = _escape;
        }

    }
}