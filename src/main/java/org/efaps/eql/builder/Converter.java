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
package org.efaps.eql.builder;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.IEnum;
import org.efaps.admin.datamodel.Status;
import org.efaps.admin.datamodel.attributetype.JaxbType;
import org.efaps.ci.CIStatus;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Converter.
 */
public final class Converter
{

    private static final Logger LOG = LoggerFactory.getLogger(Converter.class);

    public static String convert(final Object value)
        throws EFapsException
    {
        return convert(value, null);
    }

    /**
     * Convert.
     *
     * @param _value the value
     * @return the string
     * @throws EFapsException
     */
    public static String convert(final Object value,
                                 final Attribute attribute)
        throws EFapsException
    {
        String ret = null;
        if (value == null) {
        } else if (value instanceof String) {
            ret = (String) value;
        } else if (value instanceof Instance) {
            ret = ((Instance) value).getOid();
        } else if (value instanceof Number) {
            ret = ((Number) value).toString();
        } else if (value instanceof LocalDate) {
            ret = ((LocalDate) value).toString();
        } else if (value instanceof LocalTime) {
            ret = ((LocalTime) value).toString();
        } else if (value instanceof OffsetDateTime) {
            ret = ((OffsetDateTime) value).toString();
        } else if (value instanceof Boolean) {
            ret = String.valueOf(value);
        } else if (value instanceof CIStatus) {
            ret = String.valueOf(Status.find((CIStatus) value).getId());
        } else if (value instanceof IEnum) {
            ret = String.valueOf(((IEnum) value).getInt());
        } else if (value instanceof Object[]) {
            ret = "[";
            final var values = (Object[]) value;
            for (int i = 0; i < values.length; i++) {
                if (i > 0) {
                    ret = ret + ",";
                }
                ret = ret + convert(values[i], attribute);
            }
            ret = ret + "]";
        } else if (attribute != null && attribute.getAttributeType().getDbAttrType() instanceof JaxbType) {
            try {
                ret = JaxbType.toSring(attribute, value);
            } catch (final SQLException e) {
                throw new EFapsException("catched", e);
            }
        } else if (value instanceof Enum) {
            ret = String.valueOf(value);
        } else {
            LOG.warn("No specific converter defined for: {}", value);
            ret = String.valueOf(value);
        }
        return ret;
    }

    public static Object convertEql(final String value)
    {
        Object ret = value;
        if (value instanceof String) {
            var strValue = ((String) ret).trim();
            if (strValue.startsWith("[")) {
                strValue = strValue.substring(1, strValue.length() - 1);
                ret = strValue.split(",");
            }
        }
        return ret;
    }
}
