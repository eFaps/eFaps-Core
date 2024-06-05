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
package org.efaps.admin.datamodel;

import org.apache.commons.lang3.StringUtils;
import org.infinispan.protostream.annotations.ProtoAdapter;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

@ProtoAdapter(SQLTable.class)
public class SQLTableAdapter
{

    @ProtoFactory
    SQLTable create(final long id,
                    final String uuid,
                    final String name,
                    final String sqlTable,
                    final String sqlColId,
                    final String sqlColType)
    {
        return new SQLTable(id, uuid, name, sqlTable, sqlColId, StringUtils.isNotEmpty(sqlColType) ? sqlColType : null);
    }

    @ProtoField(number = 1, defaultValue = "0")
    long getId(SQLTable sqlTable)
    {
        return sqlTable.getId();
    }

    @ProtoField(number = 2)
    String getUuid(SQLTable sqlTable)
    {
        return sqlTable.getUUID().toString();
    }

    @ProtoField(number = 3)
    String getName(SQLTable sqlTable)
    {
        return sqlTable.getName();
    }

    @ProtoField(number = 4)
    String getSqlTable(SQLTable sqlTable)
    {
        return sqlTable.getSqlTable();
    }

    @ProtoField(number = 5)
    String getSqlColId(SQLTable sqlTable)
    {
        return sqlTable.getSqlColId();
    }

    @ProtoField(number = 6)
    String getSqlColType(SQLTable sqlTable)
    {
        return sqlTable.getSqlColType();
    }
}
