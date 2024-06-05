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
package org.efaps.admin.user;

import org.infinispan.protostream.annotations.ProtoAdapter;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

@ProtoAdapter(Role.class)
public class RoleAdapter
{

    @ProtoFactory
    Role create(final long id,
                final String uuid,
                final String name,
                final boolean status,
                final long typeId)
    {
        return new Role(id, uuid, name, status, typeId);
    }

    @ProtoField(number = 1, defaultValue = "0")
    long getId(Role role)
    {
        return role.getId();
    }

    @ProtoField(number = 2)
    String getUuid(Role role)
    {
        return role.getUUID().toString();
    }

    @ProtoField(number = 3)
    String getName(Role role)
    {
        return role.getName();
    }

    @ProtoField(number = 4, defaultValue = "false")
    boolean getStatus(Role role)
    {
        return role.getStatus();
    }

    @ProtoField(number = 5, defaultValue = "0")
    long getTypeId(Role role)
    {
        return role.getTypeId();
    }
}
