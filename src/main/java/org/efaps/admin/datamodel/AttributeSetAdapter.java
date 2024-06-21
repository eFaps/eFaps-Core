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

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.efaps.admin.event.EventDefinition;
import org.efaps.util.EFapsException;
import org.infinispan.protostream.annotations.ProtoAdapter;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

@ProtoAdapter(AttributeSet.class)
public class AttributeSetAdapter
    extends TypeAdapter
{

    @ProtoFactory
    AttributeSet create(long id,
                        String uuid,
                        String name,
                        Long parentTypeId,
                        boolean abstractB,
                        boolean history,
                        boolean generalInstance,
                        Long mainTableId,
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
                        Set<Long> attributeIds,
                        long attributeTypeId,
                        String attributeName,
                        Set<String> setAttributes,
                        final Map<String, String> propertyMap,
                        List<EventDefinition> events,
                        boolean eventChecked,
                        boolean typeMenuChecked,
                        boolean typeFormChecked,
                        boolean classifiedByTypeChecked,
                        boolean accessSetChecked)
    {
        AttributeSet attributeSet = null;
        try {
            attributeSet = new AttributeSet(id, uuid, name);
            attributeSet.setParentTypeID(parentTypeId);
            attributeSet.setAbstract(abstractB);
            attributeSet.setHistory(history);
            attributeSet.setGeneralInstance(generalInstance);
            attributeSet.setMainTableId(mainTableId);
            attributeSet.setChildTypeIds(childTypeIds);
            attributeSet.setClassifiedByTypeIds(classifiedByTypeIds);
            attributeSet.setTableIds(tableIds);
            attributeSet.setAccessSetIds(accessSetIds);
            attributeSet.setStatusAttributeName(StringUtils.isNotBlank(statusAttributeName) ? statusAttributeName : null);
            attributeSet.setCompanyAttributeName(StringUtils.isNotBlank(companyAttributeName) ? companyAttributeName : null);
            attributeSet.setAssociationAttributeName(
                            StringUtils.isNotBlank(associationAttributeName) ? associationAttributeName : null);
            attributeSet.setGroupAttributeName(StringUtils.isNotBlank(groupAttributeName) ? groupAttributeName : null);
            attributeSet.setTypeAttributeName(StringUtils.isNotBlank(typeAttributeName) ? typeAttributeName : null);
            attributeSet.setTypeMenuId(typeMenuId);
            attributeSet.setTypeIconId(typeIconId);
            attributeSet.setTypeFormId(typeFormId);
            attributeSet.setStoreId(storeId);
            attributeSet.setAttributeTypeId(attributeTypeId);
            attributeSet.setAttributeName(attributeName);
            attributeSet.setAttributeIds(attributeIds);
            attributeSet.setSetAttributes(setAttributes);
            setPropertiesMap(attributeSet, propertyMap);
            setEvents(attributeSet, events, eventChecked);
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return attributeSet;
    }

    @ProtoField(number = 23, defaultValue = "0")
    long getAttributeTypeId(AttributeSet attributeSet)
    {
        return attributeSet.getAttributeTypeId();
    }

    @ProtoField(number = 24)
    String getAttributeName(AttributeSet attributeSet)
    {
        return attributeSet.getAttributeName();
    }

    @ProtoField(number = 25)
    Set<String> getSetAttributes(AttributeSet attributeSet)
    {
        return attributeSet.getSetAttributes();
    }
}
