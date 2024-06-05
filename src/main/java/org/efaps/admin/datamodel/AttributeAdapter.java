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

import org.efaps.util.EFapsException;
import org.infinispan.protostream.annotations.ProtoAdapter;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

@ProtoAdapter(Attribute.class)
public class AttributeAdapter
{

    @ProtoFactory
    Attribute create(final long id,
                     final long parentId,
                     final String name,
                     final  List<String> sqlColNames,
                     final long sqlTableId,
                     final long attributeTypeId,
                     final String defaultValue,
                     final String dimensionUUID,
                     int size,
                     int scale,
                     boolean required)
    {
        try {
            return new Attribute(id, parentId, name, sqlColNames, sqlTableId, attributeTypeId, defaultValue,
                            dimensionUUID, size, scale, required);
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    @ProtoField(number = 1, defaultValue = "0")
    long getId(Attribute attribute)
    {
        return attribute.getId();
    }

    @ProtoField(number = 2, defaultValue = "0")
    long getParentId(Attribute attribute)
    {
        return attribute.getParentId();
    }

    @ProtoField(number = 3)
    String getName(Attribute attribute)
    {
        return attribute.getName();
    }

    @ProtoField(number = 4)
    List<String> getSqlColNames(Attribute attribute)
    {
        return attribute.getSqlColNames();
    }

    @ProtoField(number = 5, defaultValue = "0")
    long getSqlTableId(Attribute attribute)
    {
        return attribute.getSqlTableId();
    }
    @ProtoField(number = 6, defaultValue = "0")
    long getAttributeTypeId(Attribute attribute)
    {
        return attribute.getAttributeTypeId();
    }

    @ProtoField(number = 7)
    String getDefaultValue(Attribute attribute)
    {
        return attribute.getDefaultValue();
    }
    @ProtoField(number = 8)
    String getDimensionUUID(Attribute attribute)
    {
        return attribute.getDimensionUUID();
    }

    @ProtoField(number = 9, defaultValue = "0")
    int getSize(Attribute attribute)
    {
        return attribute.getSize();
    }
    @ProtoField(number = 10, defaultValue = "0")
    int getScale(Attribute attribute)
    {
        return attribute.getScale();
    }

    @ProtoField(number = 11, defaultValue = "0")
    boolean isRequired(Attribute attribute)
    {
        return attribute.isRequired();
    }

}
