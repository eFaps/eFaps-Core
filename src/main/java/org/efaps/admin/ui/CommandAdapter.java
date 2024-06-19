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

@ProtoAdapter(Command.class)
public class CommandAdapter
    extends AbstractCommandAdapter
{

    @ProtoFactory
    Command create(final long id,
                   final String uuid,
                   final String name,
                   final Map<String, String> propertyMap,
                   Long targetFormId,
                   Long targetMenuId,
                   Long targetTableId,
                   Long targetSearchId,
                   Long targetCommandId,
                   Long targetModuleId,
                   List<EventDefinition> events,
                   boolean eventChecked)
    {
        final var command = new Command(id, uuid, name);
        command.setTargetFormId(ProtoUtils.toNullLong(targetFormId));
        command.setTargetMenuId(ProtoUtils.toNullLong(targetMenuId));
        command.setTargetSearchId(ProtoUtils.toNullLong(targetSearchId));
        command.setTargetTableId(ProtoUtils.toNullLong(targetTableId));
        command.setTargetCommandId(ProtoUtils.toNullLong(targetCommandId));
        command.setTargetModuleId(ProtoUtils.toNullLong(targetModuleId));
        setPropertiesMap(command, propertyMap);
        setEvents(command, events, eventChecked);
        return command;
    }

}
