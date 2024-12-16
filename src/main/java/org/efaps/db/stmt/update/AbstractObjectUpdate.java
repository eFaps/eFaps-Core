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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.AttributeType;
import org.efaps.admin.datamodel.Status;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.datamodel.attributetype.IStatusChangeListener;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.Listener;
import org.efaps.db.Instance;
import org.efaps.eql2.IInsertStatement;
import org.efaps.eql2.IUpdateElement;
import org.efaps.eql2.IUpdateElementsStmt;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class AbstractObjectUpdate.
 */
public abstract class AbstractObjectUpdate
    extends AbstractUpdate
{

    private static final Logger LOG = LoggerFactory.getLogger(AbstractObjectUpdate.class);

    /** The instance. */
    protected Instance instance;

    protected Long statusId;

    /**
     * Instantiates a new abstract object update.
     *
     * @param _eqlStmt the eql stmt
     */
    public AbstractObjectUpdate(final IUpdateElementsStmt<?> _eqlStmt)
    {
        super(_eqlStmt);
    }

    /**
     * Gets the instance.
     *
     * @return the instance
     */
    public Instance getInstance()
    {
        return this.instance;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public Type getType()
    {
        return getInstance().getType();
    }

    protected void evalStatusChange()
        throws CacheReloadException
    {
        if (getType().isCheckStatus()) {
            for (final IUpdateElement updateElement : ((IInsertStatement) getEqlStmt()).getUpdateElements()) {
                if (getType().getStatusAttribute().getName().equals(updateElement.getAttribute())) {
                    if (StringUtils.isNumeric(updateElement.getValue())) {
                        statusId = Long.valueOf(updateElement.getValue());
                    } else if (updateElement.getValue() != null) {
                        final var status = Status.find(getType().getStatusAttribute().getLink().getUUID(),
                                        updateElement.getValue());
                        if (status != null) {
                            statusId = status.getId();
                            updateElement.value(String.valueOf(statusId));
                        }
                    } else {
                        LOG.warn("Cannot convert status value to status ID: {}", updateElement.getValue());
                    }
                }
            }
        }
    }

    @Override
    public boolean executeEvents(final EventType eventtype)
        throws EFapsException
    {
        boolean ret = false;
        final List<EventDefinition> triggers = getType().getEvents(eventtype);
        if (triggers != null) {
            ret = true;
            final Parameter parameter = new Parameter();
            parameter.put(ParameterValues.INSTANCE, instance);
            parameter.put(ParameterValues.NEW_VALUES, getNewValuesMap());
            for (final EventDefinition evenDef : triggers) {
                evenDef.execute(parameter);
            }
        }
        return ret;
    }

    protected final Map<Attribute, Object[]> getNewValuesMap()
    {
        final Map<Attribute, Object[]> ret = new HashMap<>();
        for (final IUpdateElement updateElement : getEqlStmt().getUpdateElements()) {
            final var attr = getType().getAttribute(updateElement.getAttribute());
            if (attr != null) {
                final AttributeType attrType = attr.getAttributeType();
                if (!(attrType.isAlwaysUpdate() || attrType.isCreateUpdate())) {
                    ret.put(attr, new Object[] { updateElement.getValue() });
                }
            }
        }
        return ret;
    }

    public void triggerListeners()
        throws EFapsException
    {
        if (getType().isCheckStatus() && statusId != null) {
            for (final IStatusChangeListener listener : Listener.get()
                            .<IStatusChangeListener>invoke(IStatusChangeListener.class)) {
                listener.onInsert(getInstance(), statusId);
            }
        }
    }
}
