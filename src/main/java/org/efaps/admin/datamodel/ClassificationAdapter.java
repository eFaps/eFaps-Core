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

import org.efaps.admin.event.EventDefinition;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.ProtoUtils;
import org.infinispan.protostream.annotations.ProtoAdapter;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;

@ProtoAdapter(Classification.class)
public class ClassificationAdapter
    extends TypeAdapter
{

    @ProtoFactory
    Classification create(long id,
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
                          final Map<String, String> propertyMap,
                          List<EventDefinition> events,
                          boolean eventChecked,
                          boolean typeMenuChecked,
                          boolean typeFormChecked,
                          boolean classifiedByTypeChecked,
                          boolean accessSetChecked,
                          Long parentId,
                          boolean multipleSelect,
                          Set<Long> childrenIds,
                          Long classifiesTypeId,
                          Long classifyRelationId,
                          String linkAttributeName,
                          String relLinkAttributeName,
                          String relTypeAttributeName,
                          Set<Long> companyIds)
    {
        Classification classification = null;
        try {
            classification = new Classification(id, uuid, name);
            classification.setParentTypeID(parentTypeId);
            classification.setAbstract(abstractB);
            classification.setHistory(history);
            classification.setGeneralInstance(generalInstance);
            classification.setMainTableId(mainTableId);
            classification.setChildTypeIds(childTypeIds);
            classification.setClassifiedByTypeIds(classifiedByTypeIds);
            classification.setTableIds(tableIds);
            classification.setAccessSetIds(accessSetIds);
            classification.setStatusAttributeName(ProtoUtils.toNullString(relTypeAttributeName));
            classification.setCompanyAttributeName(ProtoUtils.toNullString(companyAttributeName));
            classification.setAssociationAttributeName(ProtoUtils.toNullString(associationAttributeName));
            classification.setGroupAttributeName(ProtoUtils.toNullString(groupAttributeName));
            classification.setTypeAttributeName(ProtoUtils.toNullString(typeAttributeName));
            classification.setTypeMenuId(typeMenuId);
            classification.setTypeIconId(typeIconId);
            classification.setTypeFormId(typeFormId);
            classification.setStoreId(storeId);
            classification.setAttributeIds(attributeIds);
            setPropertiesMap(classification, propertyMap);
            setEvents(classification, events, eventChecked);
            classification.setParentId(ProtoUtils.toNullLong(parentId));
            classification.setMultipleSelect(multipleSelect);
            classification.setChildrenIds(childrenIds);
            classification.setClassifiesTypeId(ProtoUtils.toNullLong(classifiesTypeId));
            classification.setClassifyRelationId(ProtoUtils.toNullLong(classifyRelationId));
            classification.setLinkAttributeName(ProtoUtils.toNullString(linkAttributeName));
            classification.setRelLinkAttributeName(ProtoUtils.toNullString(relLinkAttributeName));
            classification.setRelTypeAttributeName(ProtoUtils.toNullString(relTypeAttributeName));
            classification.setCompanyIds(companyIds);
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return classification;
    }

    @ProtoField(number = 200)
    Long getParentId(Classification classification)
    {
        return classification.getParentId();
    }

    @ProtoField(number = 201, defaultValue = "false")
    boolean isMultipleSelect(Classification classification)
    {
        return classification.isMultipleSelectInternal();
    }

    @ProtoField(number = 202)
    Set<Long> getChildrenIds(Classification classification)
    {
        return classification.getChildrenIds();
    }

    @ProtoField(number = 203)
    Long getClassifiesTypeId(Classification classification)
    {
        return classification.getClassifiesTypeId();
    }

    @ProtoField(number = 204)
    Long getClassifyRelationId(Classification classification)
    {
        return classification.getClassifyRelationId();
    }

    @ProtoField(number = 205)
    String getLinkAttributeName(Classification classification)
    {
        return classification.getLinkAttributeName();
    }

    @ProtoField(number = 206)
    String getRelLinkAttributeName(Classification classification)
    {
        return classification.getRelLinkAttributeNameInternal();
    }

    @ProtoField(number = 207)
    String getRelTypeAttributeName(Classification classification)
    {
        return classification.getRelTypeAttributeNameInternal();
    }

    @ProtoField(number = 208)
    Set<Long> getCompanyIds(Classification classification)
    {
        return classification.getCompanyIds();
    }
}
