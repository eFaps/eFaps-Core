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

import org.infinispan.protostream.annotations.ProtoAdapter;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

@ProtoAdapter(Status.class)
public class StatusAdapter
{

    @ProtoFactory
    Status create(long id,
                  String key,
                  String description,
                  String statusGroupUuid)
    {
        return new Status(statusGroupUuid, id, key, description);
    }

    @ProtoField(number = 1, defaultValue = "0")
    long getId(Status status)
    {
        return status.getId();
    }

    @ProtoField(number = 2)
    String getKey(Status status)
    {
        return status.getKey();
    }

    @ProtoField(number = 3)
    String getDescription(Status status)
    {
        return status.getDescription();
    }

    @ProtoField(number = 4)
    String getStatusGroupUuid(Status status)
    {
        return status.getStatusGroupUUID().toString();
    }
}
