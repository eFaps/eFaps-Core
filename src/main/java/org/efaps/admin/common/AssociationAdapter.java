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
package org.efaps.admin.common;

import java.util.Set;

import org.infinispan.protostream.annotations.ProtoAdapter;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

@ProtoAdapter(Association.class)
public class AssociationAdapter
{

    @ProtoFactory
    Association create(long id,
                       String name,
                       String uuid,
                       Set<Long> companyIds)
    {
        final var association = new Association(id, name, uuid);
        companyIds.forEach(companyId -> {
            association.addCompanyId(companyId);
        });
        return association;
    }

    @ProtoField(number = 1, defaultValue = "0")
    long getId(Association association)
    {
        return association.getId();
    }

    @ProtoField(number = 2)
    String getName(Association association)
    {
        return association.getName();
    }

    @ProtoField(number = 3)
    String getUuid(Association association)
    {
        return association.getUUID().toString();
    }

    @ProtoField(number = 4)
    Set<Long> getCompanyIds(Association association)
    {
        return association.getCompanyIds();
    }
}
