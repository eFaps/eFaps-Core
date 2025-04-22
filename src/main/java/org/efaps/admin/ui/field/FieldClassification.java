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
package org.efaps.admin.ui.field;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.efaps.admin.access.AccessType;
import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.admin.datamodel.Classification;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.AbstractCommand;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.jaas.AppAccessHandler;
import org.efaps.util.EFapsException;
import org.efaps.util.UUIDUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FieldClassification
    extends Field
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(FieldClassification.class);

    /**
     * This is the constructor of the field class.
     *
     * @param id id of the field instance
     * @param uuid UUID of the field instance
     * @param name name of the field instance
     */
    public FieldClassification(final long id,
                               final String uuid,
                               final String name)
    {
        super(id, uuid, name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasAccess(final TargetMode targetMode,
                             final Instance instance,
                             final AbstractCommand callCmd,
                             final Instance callInstance)
        throws EFapsException
    {
        boolean ret = false;

        for (final Classification clazz : evalRootClassifications()) {
            if (clazz.isAssigendTo(Context.getThreadContext().getCompany())
                            && !AppAccessHandler.excludeMode()) {
                // check if any of the type ahs access
                ret = checkAccessOnChild(clazz, instance, targetMode == TargetMode.CREATE
                                || targetMode == TargetMode.EDIT ? AccessTypeEnums.CREATE.getAccessType()
                                                : AccessTypeEnums.SHOW.getAccessType());
                if (ret) {
                    break;
                }
            }
        }
        if ((ret || AppAccessHandler.excludeMode()) && super.hasEvents(EventType.UI_ACCESSCHECK)) {
            ret = false;
            final List<EventDefinition> events = super.getEvents(EventType.UI_ACCESSCHECK);

            final Parameter parameter = new Parameter();
            parameter.put(ParameterValues.UIOBJECT, this);
            parameter.put(ParameterValues.ACCESSMODE, targetMode);
            parameter.put(ParameterValues.INSTANCE, instance);
            parameter.put(ParameterValues.CALL_CMD, callCmd);
            parameter.put(ParameterValues.CALL_INSTANCE, callInstance);
            for (final EventDefinition event : events) {
                final Return retIn = event.execute(parameter);
                ret = retIn.get(ReturnValues.TRUE) != null;
            }
        }
        return ret;
    }

    public List<Classification> evalRootClassifications()
        throws EFapsException
    {

        final List<Classification> ret = new ArrayList<>();
        String[] classificationNames = null;
        final var config = getProperty("ClassificationConfig");
        final var attr = getProperty("ClassificationAttribute");
        if ((StringUtils.isEmpty(config) || StringUtils.isEmpty(attr))
                        && StringUtils.isEmpty(getClassificationName())) {
            LOG.warn("FieldClassification {} has neither ClassificationName "
                            + "nor valid ClassificationConfig/ClassificationAttribute", getName());
        }
        if (StringUtils.isNotEmpty(config) && StringUtils.isNotEmpty(attr)) {
            SystemConfiguration sysConf;
            if (UUIDUtil.isUUID(config)) {
                sysConf = SystemConfiguration.get(UUID.fromString(config));
            } else {
                sysConf = SystemConfiguration.get(config);
            }
            final var attrValue = sysConf.getAttributeValue(attr);
            if (StringUtils.isNotEmpty(attrValue)) {
                classificationNames = attrValue.split("\\r?\\n");
            }
        } else if (getClassificationName() != null) {
            classificationNames = getClassificationName().split(";");
        }
        if (classificationNames != null) {
            for (final var classificationName : classificationNames) {
                Classification clazz;
                if (UUIDUtil.isUUID(classificationName)) {
                    clazz = Classification.get(UUID.fromString(classificationName));
                } else {
                    clazz = Classification.get(classificationName);
                }
                if (clazz != null) {
                    ret.add(clazz);
                }
            }
        }
        return ret;
    }

    /**
     * @param parent parent to iterate down
     * @param instance instance to check
     * @param accessType accesstype
     * @return true of access is granted
     * @throws EFapsException on error
     */
    private boolean checkAccessOnChild(final Classification parent,
                                       final Instance instance,
                                       final AccessType accessType)
        throws EFapsException
    {
        boolean ret = false;
        if (!parent.isAbstract()) {
            ret = parent.hasAccess(getInstance4Classification(instance, parent), accessType);
        }
        if (!ret) {
            for (final Classification childClass : parent.getChildClassifications()) {
                ret = childClass.hasAccess(getInstance4Classification(instance, childClass), accessType);
                if (ret) {
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * @param instance Instance of the classifcation
     * @param clazz classification to be searched
     * @return Instance of the classification
     */
    private Instance getInstance4Classification(final Instance instance,
                                                final Classification clazz)
    {
        Instance inst = instance;
        if (!(instance.getType() instanceof Classification)) {
            inst = Instance.get(clazz, 0);
        }
        return inst;
    }
}
