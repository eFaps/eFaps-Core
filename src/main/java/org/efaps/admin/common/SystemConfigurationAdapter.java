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

import org.infinispan.protostream.annotations.ProtoAdapter;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

@ProtoAdapter(SystemConfiguration.class)
public class SystemConfigurationAdapter
{
    @ProtoFactory
    SystemConfiguration create(final long id,
                               final String name,
                               final String uuid) {
      return new SystemConfiguration(id, name, uuid);
    }


    @ProtoField(number = 1, defaultValue = "0")
    long getId(SystemConfiguration systemConfiguration)
    {
        return systemConfiguration.getId();
    }

    @ProtoField(number = 2)
    String getUuid(SystemConfiguration systemConfiguration)
    {
        return systemConfiguration.getUUID().toString();
    }

    @ProtoField(number = 3)
    String getName(SystemConfiguration systemConfiguration)
    {
        return systemConfiguration.getName();
    }
}
