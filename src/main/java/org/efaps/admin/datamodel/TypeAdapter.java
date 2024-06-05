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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.efaps.admin.AbstractAdminObjectAdapter;
import org.efaps.admin.event.EventDefinition;
import org.efaps.util.cache.CacheReloadException;
import org.infinispan.protostream.annotations.ProtoAdapter;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

@ProtoAdapter(Type.class)
public class TypeAdapter extends AbstractAdminObjectAdapter
{

    @ProtoFactory
    Type create(final long id,
                final String uuid,
                final String name,
                final Long parentTypeId,
                final boolean abstractB,
                final boolean history,
                final boolean generalInstance,
                final Long mainTableId,
                Set<Long> childTypeIds,
                Set<Long> classifiedByTypeIds,
                Set<Long> tableIds,
                Set<Long> accessSetIds,
                String statusAttributeName,
                String companyAttributeName,
                String associationAttributeName,
                String groupAttributeName,
                String typeAttributeName,
                Long typeMenuId,
                Long typeIconId,
                Long typeFormId,
                Long storeId,
                Map<String, String> attributeIds,
                final Map<String, String> propertyMap,
                List<EventDefinition> events,
                boolean eventChecked)
    {
        Type type = null;
        try {
            type = new Type(id, uuid, name);
            type.setParentTypeID(parentTypeId);
            type.setAbstract(abstractB);
            type.setHistory(history);
            type.setGeneralInstance(generalInstance);
            type.setMainTableId(mainTableId);
            type.setChildTypeIds(childTypeIds);
            type.setClassifiedByTypeIds(classifiedByTypeIds);
            type.setTableIds(tableIds);
            type.setAccessSetIds(accessSetIds);
            type.setStatusAttributeName(StringUtils.isNotBlank(statusAttributeName) ? statusAttributeName : null);
            type.setCompanyAttributeName(StringUtils.isNotBlank(companyAttributeName) ? companyAttributeName : null);
            type.setAssociationAttributeName(
                            StringUtils.isNotBlank(associationAttributeName) ? associationAttributeName : null);
            type.setGroupAttributeName(StringUtils.isNotBlank(groupAttributeName) ? groupAttributeName : null);
            type.setTypeAttributeName(StringUtils.isNotBlank(typeAttributeName) ? typeAttributeName : null);
            type.setTypeMenuId(typeMenuId);
            type.setTypeIconId(typeIconId);
            type.setTypeFormId(typeFormId);
            type.setStoreId(storeId);

            final Map<String, Long> ret = new HashMap<>();
            for (final var entry : attributeIds.entrySet()) {
                ret.put(entry.getKey(), Long.valueOf(entry.getValue()));
            }
            type.setAttributeIds(ret);

            setPropertiesMap(type, propertyMap);
            setEvents(type, events);
            setEventsChecked(type, eventChecked);

        } catch (final CacheReloadException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return type;
    }

    @ProtoField(number = 101, defaultValue = "0")
    long getId(Type type)
    {
        return type.getId();
    }

    @ProtoField(number = 102)
    String getUuid(Type type)
    {
        return type.getUUID().toString();
    }

    @ProtoField(number = 103)
    String getName(Type type)
    {
        return type.getName();
    }

    @ProtoField(number =104)
    Long getParentTypeId(Type type)
    {
        return type.getParentTypeId();
    }

    @ProtoField(number = 105, defaultValue = "false")
    boolean isAbstractB(Type type)
    {
        return type.isAbstract();
    }

    @ProtoField(number = 106, defaultValue = "false")
    boolean isHistory(Type type)
    {
        return type.isHistory();
    }

    @ProtoField(number = 107, defaultValue = "false")
    boolean isGeneralInstance(Type type)
    {
        return type.isGeneralInstance();
    }

    @ProtoField(number = 108)
    Long getMainTableId(Type type)
    {
        return type.getMainTableId();
    }

    @ProtoField(number = 109)
    Set<Long> getChildTypeIds(Type type)
    {
        return type.getChildTypeIds();
    }

    @ProtoField(number = 110)
    Set<Long> getClassifiedByTypeIds(Type type)
    {
        return type.getClassifiedByTypeIds();
    }

    @ProtoField(number = 111)
    Set<Long> getTableIds(Type type)
    {
        return type.getTableIds();
    }

    @ProtoField(number = 112)
    Set<Long> getAccessSetIds(Type type)
    {
        return type.getAccessSetIds();
    }

    @ProtoField(number = 113)
    String getStatusAttributeName(Type type)
    {
        return type.getStatusAttributeName();
    }

    @ProtoField(number = 114)
    String getCompanyAttributeName(Type type)
    {
        return type.getCompanyAttributeName();
    }

    @ProtoField(number = 115)
    String getAssociationAttributeName(Type type)
    {
        return type.getAssociationAttributeName();
    }

    @ProtoField(number = 116)
    String getGroupAttributeName(Type type)
    {
        return type.getGroupAttributeName();
    }

    @ProtoField(number = 117)
    String getTypeAttributeName(Type type)
    {
        return type.getTypeAttributeName();
    }

    @ProtoField(number = 118)
    Long getTypeMenuId(Type type)
    {
        return type.getTypeMenuId();
    }

    @ProtoField(number = 119)
    Long getTypeIconId(Type type)
    {
        return type.getTypeIconId();
    }

    @ProtoField(number = 120)
    Long getTypeFormId(Type type)
    {
        return type.getTypeFormId();
    }

    @ProtoField(number = 121)
    Long getStoreId(Type type)
    {
        return type.getStoreIdInternal();
    }

    @ProtoField(number = 122)
    Map<String, String> getAttributeIds(Type type)
    {
        final Map<String, String> ret = new HashMap<>();
        for (final var entry : type.getAttributeIds().entrySet()) {
            ret.put(entry.getKey(), entry.getValue().toString());
        }
        return ret;
    }
}
