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
import org.infinispan.protostream.annotations.ProtoField;

@ProtoAdapter(Menu.class)
public class MenuAdapter
    extends AbstractCommandAdapter
{

    @ProtoFactory
    Menu create(long id,
                String uuid,
                String name,
                boolean typeMenu,
                Map<String, String> commands,
                Map<String, String> propertyMap,
                Long targetFormId,
                Long targetMenuId,
                Long targetTableId,
                Long targetSearchId,
                Long targetCommandId,
                List<EventDefinition> events,
                boolean eventChecked)
    {
        final var menu = new Menu(id, uuid, name);
        menu.setTypeMenu(typeMenu);
        menu.setCommandsInternal(ProtoUtils.fromMap(commands));
        setPropertiesMap(menu, propertyMap);
        menu.setTargetFormId(ProtoUtils.toNullLong(targetFormId));
        menu.setTargetMenuId(ProtoUtils.toNullLong(targetMenuId));
        menu.setTargetSearchId(ProtoUtils.toNullLong(targetSearchId));
        menu.setTargetTableId(ProtoUtils.toNullLong(targetTableId));
        menu.setTargetCommandId(ProtoUtils.toNullLong(targetCommandId));
        return menu;
    }

    @ProtoField(number = 5, defaultValue = "false")
    boolean isTypeMenu(Menu menu)
    {
        return menu.isTypeMenu();
    }

    @ProtoField(number = 6)
    Map<String, String> getCommands(Menu menu)
    {
        return ProtoUtils.toMap(menu.getCommandsInternal());
    }
}
