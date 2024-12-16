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
package org.efaps.db.stmt.update;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.access.user.AccessCache;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeType;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.eql2.IUpdateElement;
import org.efaps.eql2.IUpdateListStatement;
import org.efaps.util.EFapsException;

public class ListUpdate
    extends AbstractUpdate
{

    final List<Instance> instances;

    public ListUpdate(final IUpdateListStatement _eqlStmt)
        throws EFapsException
    {
        super(_eqlStmt);
        this.instances = _eqlStmt.getOidsList().stream().map(Instance::get).collect(Collectors.toList());

        this.instances.forEach(AccessCache::registerUpdate);

        final Map<Type, List<Instance>> typeMap = this.instances.stream().collect(Collectors.groupingBy(
                        Instance::getType));

        for (final Entry<Type, List<Instance>> entry : typeMap.entrySet()) {
            final Map<Instance, Boolean> access = entry.getKey().checkAccess(entry.getValue(), AccessTypeEnums.MODIFY
                            .getAccessType());
            if (access.values().contains(Boolean.FALSE)) {
                throw new EFapsException(getClass(), "execute.NoAccess", Context.getThreadContext().getPerson());
            }
        }
    }

    public List<Instance> getInstances()
    {
        return Collections.unmodifiableList(this.instances);
    }

    @Override
    public boolean executeEvents(final EventType eventtype)
        throws EFapsException
    {
        boolean ret = false;
        final Map<Type, List<Instance>> typeMap = this.instances.stream().collect(Collectors.groupingBy(
                        Instance::getType));

        for (final Entry<Type, List<Instance>> entry : typeMap.entrySet()) {
            if (entry.getValue().isEmpty()) {
                final List<EventDefinition> triggers = entry.getValue().get(0).getType().getEvents(eventtype);
                if (triggers != null) {
                    for (final var instance : entry.getValue()) {
                        final Parameter parameter = new Parameter();
                        parameter.put(ParameterValues.NEW_VALUES, getNewValuesMap(instance));
                        parameter.put(ParameterValues.INSTANCE, instance);
                        for (final EventDefinition evenDef : triggers) {
                            evenDef.execute(parameter);
                        }
                    }
                    ret = true;
                }
            }
        }
        return ret;
    }

    protected final Map<Attribute, Object[]> getNewValuesMap(final Instance instance)
    {
        // convert the map in a more simple map (following existing API)
        final Map<Attribute, Object[]> ret = new HashMap<>();
        final Type type = instance.getType();
        for (final IUpdateElement element : getEqlStmt().getUpdateElements()) {
            final Attribute attr = type.getAttribute(element.getAttribute());
            if (attr != null) {
                final AttributeType attrType = attr.getAttributeType();
                if (!attrType.isAlwaysUpdate()) {
                    ret.put(attr, element.getValues());
                }
            }
        }
        return ret;
    }
}
