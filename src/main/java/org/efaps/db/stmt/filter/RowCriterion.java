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

public class RowCriterion
    extends AbstractCriterion
{

    private String rowColumnValue;

    public RowCriterion(final TableIdx tableIdx,
                        final String sqlCol)
    {
        super(tableIdx, sqlCol, 0, false);
    }

    @Override
    public long getValue()
    {
        return 0;
    }

    public String getRowColumnValue()
    {
        return rowColumnValue;
    }

    public RowCriterion setRowColumnValue(String rowColumnValue)
    {
        this.rowColumnValue = rowColumnValue;
        return this;
    }

}
