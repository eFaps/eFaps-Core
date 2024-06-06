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
package org.efaps.db.store;

import java.util.List;
import java.util.Map;

import org.efaps.admin.AbstractAdminObjectAdapter;
import org.efaps.admin.event.EventDefinition;
import org.infinispan.protostream.annotations.ProtoAdapter;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

@ProtoAdapter(Store.class)
public class StoreAdapter extends AbstractAdminObjectAdapter
{
    @ProtoFactory
    Store create(final long id,
                  final String uuid,
                  final String name,
                  Map<String, String> resourceProperties,
                  Map<String, String> propertyMap,
                  List<EventDefinition> events,
                  boolean eventChecked)
    {
        final var store = new Store(id, uuid, name);
        setPropertiesMap(store, propertyMap);
        store.getResourceProperties().putAll(resourceProperties);
        return store;
    }

    @ProtoField(number = 101)
    Map<String, String> getResourceProperties(Store store)
    {
        return store.getResourceProperties();
    }
}
