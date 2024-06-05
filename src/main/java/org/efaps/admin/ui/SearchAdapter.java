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

import org.efaps.admin.event.EventDefinition;
import org.efaps.util.cache.ProtoUtils;
import org.infinispan.protostream.annotations.ProtoAdapter;
import org.infinispan.protostream.annotations.ProtoFactory;

@ProtoAdapter(Search.class)
public class SearchAdapter
    extends AbstractCommandAdapter
{

    @ProtoFactory
    Search create(final long id,
                  final String uuid,
                  final String name,
                  Map<String, String> propertyMap,
                  Long targetFormId,
                  Long targetMenuId,
                  Long targetTableId,
                  Long targetSearchId,
                  Long targetCommandId,
                  List<EventDefinition> events,
                  boolean eventChecked)
    {
        final var search = new Search(id, uuid, name);
        search.setTargetFormId(ProtoUtils.toNullLong(targetFormId));
        search.setTargetMenuId(ProtoUtils.toNullLong(targetMenuId));
        search.setTargetSearchId(ProtoUtils.toNullLong(targetSearchId));
        search.setTargetTableId(ProtoUtils.toNullLong(targetTableId));
        search.setTargetCommandId(ProtoUtils.toNullLong(targetCommandId));
        setPropertiesMap(search, propertyMap);
        return search;
    }

}
