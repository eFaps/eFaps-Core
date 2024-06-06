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

import org.efaps.admin.AbstractAdminObjectAdapter;
import org.efaps.admin.event.EventDefinition;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.ProtoUtils;
import org.infinispan.protostream.annotations.ProtoAdapter;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

@ProtoAdapter(Attribute.class)
public class AttributeAdapter
    extends AbstractAdminObjectAdapter
{

    @ProtoFactory
    Attribute create(final long id,
                     final String uuid,
                     final String name,
                     final long parentId,
                     final List<String> sqlColNames,
                     final long sqlTableId,
                     final long attributeTypeId,
                     final String defaultValue,
                     final String dimensionUUID,
                     int size,
                     int scale,
                     boolean required,
                     Long linkId,
                     final Map<String, String> propertyMap,
                     List<EventDefinition> events,
                     boolean eventChecked)
    {
        try {
            final var attr =  new Attribute(id, parentId, name, sqlColNames, sqlTableId, attributeTypeId, defaultValue,
                            dimensionUUID, size, scale, required, ProtoUtils.toNullLong(linkId));
            setPropertiesMap(attr, propertyMap);
            setEvents(attr, events);
            setEventsChecked(attr, eventChecked);
            return attr;
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @ProtoField(number = 101, defaultValue = "0")
    long getParentId(Attribute attribute)
    {
        return attribute.getParentId();
    }

    @ProtoField(number = 103)
    List<String> getSqlColNames(Attribute attribute)
    {
        return attribute.getSqlColNames();
    }

    @ProtoField(number = 104, defaultValue = "0")
    long getSqlTableId(Attribute attribute)
    {
        return attribute.getSqlTableId();
    }

    @ProtoField(number = 105, defaultValue = "0")
    long getAttributeTypeId(Attribute attribute)
    {
        return attribute.getAttributeTypeId();
    }

    @ProtoField(number = 107)
    String getDefaultValue(Attribute attribute)
    {
        return attribute.getDefaultValue();
    }

    @ProtoField(number = 108)
    String getDimensionUUID(Attribute attribute)
    {
        return attribute.getDimensionUUID();
    }

    @ProtoField(number = 109, defaultValue = "0")
    int getSize(Attribute attribute)
    {
        return attribute.getSize();
    }

    @ProtoField(number = 110, defaultValue = "0")
    int getScale(Attribute attribute)
    {
        return attribute.getScale();
    }

    @ProtoField(number = 111, defaultValue = "0")
    boolean isRequired(Attribute attribute)
    {
        return attribute.isRequired();
    }

    @ProtoField(number = 112, defaultValue = "0")
    Long getLinkId(Attribute attribute)
    {
        return attribute.getLinkId();
    }

}
