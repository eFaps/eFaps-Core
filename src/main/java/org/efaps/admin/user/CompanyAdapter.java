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

@ProtoAdapter(Company.class)
public class CompanyAdapter
{
    @ProtoFactory
    Company create(final long id,
                   final String uuid,
                   final String name,
                   final boolean status,
                   final Set<Long> consortiumIds)
    {
        return new Company(id, uuid, name, status, consortiumIds);
    }

    @ProtoField(number = 1, defaultValue = "0")
    long getId(Company company)
    {
        return company.getId();
    }

    @ProtoField(number = 2)
    String getUuid(Company company)
    {
        return company.getUUID() == null ? null : company.getUUID().toString();
    }

    @ProtoField(number = 3)
    String getName(Company company)
    {
        return company.getName();
    }

    @ProtoField(number = 4, defaultValue = "false")
    boolean getStatus(Company company)
    {
        return company.getStatus();
    }

    @ProtoField(number = 5, defaultValue = "0")
    Set<Long> getConsortiumIds(Company company)
    {
        return company.getConsortiums();
    }
}
