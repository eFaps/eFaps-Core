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

import java.util.Set;

import org.infinispan.protostream.annotations.ProtoAdapter;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

@ProtoAdapter(Person.class)
public class PersonAdapter
{

    @ProtoFactory
    Person create(final long id,
                  final String uuid,
                  final String name,
                  final boolean status,
                  final Set<Long> roles,
                  final Set<Long> companies,
                  final Set<Long> associations)
    {
        return new Person(id, uuid, name, status, roles, companies, associations);
    }

    @ProtoField(number = 1, defaultValue = "0")
    long getId(Person person)
    {
        return person.getId();
    }

    @ProtoField(number = 2)
    String getUuid(Person person)
    {
        return person.getUUID().toString();
    }

    @ProtoField(number = 3)
    String getName(Person person)
    {
        return person.getName();
    }

    @ProtoField(number = 4, defaultValue = "false")
    boolean getStatus(Person person)
    {
        return person.getStatus();
    }

    @ProtoField(number = 5)
    Set<Long> getRoles(Person person)
    {
        return person.getRoles();
    }

    @ProtoField(number = 6)
    Set<Long> getCompanies(Person person)
    {
        return person.getCompanies();
    }

    @ProtoField(number = 7)
    Set<Long> getAssociations(Person person)
    {
        return person.getAssociations();
    }
}
