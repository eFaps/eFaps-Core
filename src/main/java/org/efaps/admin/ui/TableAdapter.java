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
package org.efaps.admin.ui;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.efaps.admin.event.EventDefinition;
import org.infinispan.protostream.annotations.ProtoAdapter;
import org.infinispan.protostream.annotations.ProtoFactory;

@ProtoAdapter(Table.class)
public class TableAdapter
    extends AbstractCollectionAdapter
{

    @ProtoFactory
    Table create(final long id,
                 final String uuid,
                 final String name,
                 Map<String, String> propertyMap,
                 List<EventDefinition> events,
                 boolean eventChecked,
                 List<Long> fieldIds,
                 Set<Long> access)
    {
        final var table = new Table(id, uuid, name);
        setPropertiesMap(table, propertyMap);
        setFields(table, fieldIds);
        setAccess(table, access);
        return table;
    }

}
