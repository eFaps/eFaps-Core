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
package org.efaps.admin.event;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.EnumUtils;
import org.efaps.admin.AbstractAdminObjectAdapter;
import org.infinispan.protostream.annotations.ProtoAdapter;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

@ProtoAdapter(EventDefinition.class)
public class EventDefinitionAdapter
    extends AbstractAdminObjectAdapter
{

    @ProtoFactory
    EventDefinition create(long id,
                           String name,
                           String uuid,
                           int indexPos,
                           String resourceName,
                           String methodName,
                           Map<String, String> propertyMap,
                           List<EventDefinition> events,
                           boolean eventChecked,
                           String eventType)
    {
        final var event = new EventDefinition(EnumUtils.getEnum(EventType.class, eventType), id, name, indexPos,
                        resourceName, methodName);
        setPropertiesMap(event, propertyMap);
        return event;
    }

    @ProtoField(number = 102, defaultValue = "0")
    int getIndexPos(EventDefinition event)
    {
        return event.getIndexPos();
    }

    @ProtoField(number = 103)
    String getResourceName(EventDefinition event)
    {
        return event.getResourceName();
    }

    @ProtoField(number = 104)
    String getMethodName(EventDefinition event)
    {
        return event.getMethodName();
    }

    @ProtoField(number = 105)
    String getEventType(EventDefinition event)
    {
        return event.getEventType().name();
    }
}
