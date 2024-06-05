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
package org.efaps.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.efaps.admin.event.EventDefinition;
import org.efaps.util.cache.CacheReloadException;
import org.infinispan.protostream.annotations.ProtoField;

public class AbstractAdminObjectAdapter
{

    @ProtoField(number = 1)
    public Map<String, String> getPropertyMap(AbstractAdminObject adminObject)
    {
        return adminObject.getPropertyMap();
    }

    @ProtoField(number = 2)
    public List<EventDefinition> getEvents(AbstractAdminObject adminObject)
    {
        final List<EventDefinition> eventDefinitions = new ArrayList<>();
        for (final var entry : adminObject.getEvents().values()) {
            eventDefinitions.addAll(entry);
        }
        return eventDefinitions;
    }

    @ProtoField(number = 3, defaultValue = "false")
    public boolean isEventChecked(AbstractAdminObject adminObject)
    {
        return adminObject.isEventChecked();
    }

    protected void setPropertiesMap(AbstractAdminObject adminObject,
                                    Map<String, String> propertiesMap)
    {
        try {
            for (final var entry : propertiesMap.entrySet()) {
                adminObject.setProperty(entry.getKey(), entry.getValue());
            }
        } catch (final CacheReloadException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void setEvents(final AbstractAdminObject adminObject,
                             final List<EventDefinition> events)
    {
        if (!events.isEmpty()) {
            final var map = events.stream().collect(Collectors.groupingBy(EventDefinition::getEventType));
            adminObject.getEvents().putAll(map);
        }
    }

    protected void setEventsChecked(AbstractAdminObject adminObject,
                                    boolean eventChecked)
    {
        adminObject.setEventChecked(eventChecked);
    }
}
