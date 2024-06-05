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

@ProtoAdapter(Association.class)
public class AssociationAdapter
{
    @ProtoFactory
    Association create(final long id,
                       final long roleId,
                       final long groupId)
    {
        return new Association(id, roleId, groupId);
    }

    @ProtoField(number = 1, defaultValue = "0")
    long getId(Association association)
    {
        return association.getId();
    }

    @ProtoField(number = 2, defaultValue = "0")
    long getRoleId(Association association)
    {
        return association.getRoleId();
    }

    @ProtoField(number = 3, defaultValue = "0")
    long getGroupId(Association association)
    {
        return association.getGroupId();
    }
}
