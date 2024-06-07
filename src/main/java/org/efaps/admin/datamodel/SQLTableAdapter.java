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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.efaps.admin.AbstractAdminObjectAdapter;
import org.efaps.admin.event.EventDefinition;
import org.efaps.util.cache.ProtoUtils;
import org.infinispan.protostream.annotations.ProtoAdapter;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

@ProtoAdapter(SQLTable.class)
public class SQLTableAdapter
    extends AbstractAdminObjectAdapter
{

    @ProtoFactory
    SQLTable create(final long id,
                    final String uuid,
                    final String name,
                    final String sqlTable,
                    final String sqlColId,
                    final String sqlColType,
                    final Long mainTableId,
                    Set<Long> typeIds,
                    final Map<String, String> propertyMap,
                    List<EventDefinition> events,
                    boolean eventChecked)
    {
        final var sqlTableObj = new SQLTable(id, uuid, name, sqlTable, sqlColId,
                        StringUtils.isNotEmpty(sqlColType) ? sqlColType : null);
        sqlTableObj.setMainTableId(ProtoUtils.toNullLong(mainTableId));
        sqlTableObj.getTypeIds().addAll(typeIds);
        return sqlTableObj;
    }

    @ProtoField(number = 100)
    String getSqlTable(SQLTable sqlTable)
    {
        return sqlTable.getSqlTable();
    }

    @ProtoField(number = 101)
    String getSqlColId(SQLTable sqlTable)
    {
        return sqlTable.getSqlColId();
    }

    @ProtoField(number = 102)
    String getSqlColType(SQLTable sqlTable)
    {
        return sqlTable.getSqlColType();
    }

    @ProtoField(number = 103)
    Long getMainTableId(SQLTable sqlTable)
    {
        return sqlTable.getMainTableId();
    }

    @ProtoField(number = 104)
    Set<Long> getTypeIds(SQLTable sqlTable)
    {
        return sqlTable.getTypeIds();
    }
}
