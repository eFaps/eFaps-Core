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

import org.efaps.util.EFapsException;
import org.infinispan.protostream.annotations.ProtoAdapter;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

@ProtoAdapter(AttributeType.class)
public class AttributeTypeAdapter
{

    @ProtoFactory
    AttributeType create(final long id,
                         final String uuid,
                         final String name,
                         final String dbAttrTypeName,
                         final String uiAttrTypeName)
    {
        try {
            return new AttributeType(id, uuid, name, dbAttrTypeName, uiAttrTypeName);
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @ProtoField(number = 1, defaultValue = "0")
    long getId(AttributeType attributeType)
    {
        return attributeType.getId();
    }

    @ProtoField(number = 2)
    String getUuid(AttributeType attributeType)
    {
        return attributeType.getUUID().toString();
    }

    @ProtoField(number = 3)
    String getName(AttributeType attributeType)
    {
        return attributeType.getName();
    }

    @ProtoField(number = 4)
    String getDbAttrTypeName(AttributeType attributeType)
    {
        return attributeType.getDbAttrType().getClass().getName();
    }

    @ProtoField(number = 5)
    String getUiAttrTypeName(AttributeType attributeType)
    {
        return attributeType.getUIProvider().getClass().getName();
    }

}
