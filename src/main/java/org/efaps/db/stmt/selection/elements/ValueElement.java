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
package org.efaps.db.stmt.selection.elements;

import org.efaps.admin.datamodel.attributetype.RateType;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValueElement
    extends AbstractElement<ValueElement>
    implements IAuxillary
{

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ValueElement.class);

    @Override
    public ValueElement getThis()
    {
        return this;
    }

    @Override
    public Object getObject(final Object[] row)
        throws EFapsException
    {
        Object object = row == null ? null : row[0];
        if (object != null) {
            if (getPrevious() != null && getPrevious() instanceof AttributeElement) {
                final var attribute = ((AttributeElement) getPrevious()).getAttribute();
                if (attribute.getAttributeType().getDbAttrType() instanceof RateType
                                && attribute.hasEvents(EventType.RATE_VALUE)) {
                    final var returns = attribute.executeEvents(EventType.RATE_VALUE,
                                    ParameterValues.OTHERS, object,
                                    ParameterValues.PARAMETERS, Context.getThreadContext().getParameters());
                    if (!returns.isEmpty()) {
                        object = returns.get(0).get(ReturnValues.VALUES);
                    }
                }
            } else {
                LOG.warn("ValueElement was called with unexpected Object: {}", object);
            }
        }
        return object;
    }
}
