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
package org.efaps.admin.datamodel.attributetype;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.efaps.admin.datamodel.Attribute;
import org.efaps.admin.datamodel.IEnum;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.db.wrapper.SQLInsert;
import org.efaps.db.wrapper.SQLUpdate;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.InfinispanCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
public class EnumType
    extends AbstractType
{

    /**
     * Name of the Cache for Instances.
     */
    public static final String CACHE = EnumType.class.getName() + ".Object";

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EnumType.class);

    @Override
    public Object readValue(final Attribute attribute,
                            final List<Object> valueList)
        throws EFapsException
    {
        Object ret = null;
        if (valueList.size() < 1) {
            ret = null;
        } else {
            final List<Object> list = new ArrayList<>();
            for (final Object value : valueList) {
                Integer num = null;
                if (value instanceof final Number number) {
                    num = number.intValue();
                } else if (value instanceof final String string) {
                    if (StringUtils.isNumeric(string)) {
                        num = Integer.parseInt(string);
                    } else {
                        list.add(getEnum4Str(attribute, string));
                    }
                } else if (value != null) {
                    num = Integer.parseInt(value.toString());
                }
                if (num != null) {
                    list.add(getEnum4Int(attribute, num));
                }
            }
            ret = list.isEmpty() ? null : list.size() > 1 ? list : list.get(0);
        }
        return ret;
    }

    protected Object getEnum4Str(final Attribute attribute,
                                 final String string)
    {
        final var cache = InfinispanCache.get().<String, Object>getCache(EnumType.CACHE);
        final String key = attribute.getClassName() + "-" + string;
        if (!cache.containsKey(key)) {
            Object ret = null;
            try {
                final Class<?> clazz = Class.forName(attribute.getClassName(), false, EFapsClassLoader.getInstance());
                final var consts = (Enum<?>[]) clazz.getEnumConstants();
                if (consts != null) {
                    for (final Enum<?> constant : consts) {
                        if (StringUtils.equalsIgnoreCase(constant.name(), string)) {
                            ret = constant;
                            break;
                        }
                    }
                }
            } catch (final ClassNotFoundException e) {
                LOG.error("Could not read clazz.", e);
            }
            cache.put(key, ret);
        }
        return cache.get(key);
    }

    /**
     * @param attribute Attribute the enum class is defined for
     * @param idx number from eFapsDatabase thta defines the enum
     * @return enum
     */
    protected Object getEnum4Int(final Attribute attribute,
                                 final Integer idx)
    {
        final var cache = InfinispanCache.get().<String, Object>getCache(EnumType.CACHE);
        final String key = attribute.getClassName() + "-" + idx;
        if (!cache.containsKey(key)) {
            Object ret = null;
            try {
                final Class<?> clazz = Class.forName(attribute.getClassName(), false, EFapsClassLoader.getInstance());
                final Object[] consts = clazz.getEnumConstants();
                if (consts != null) {
                    for (final Object cons : consts) {
                        if (idx == ((IEnum) cons).getInt()) {
                            ret = cons;
                            break;
                        }
                    }
                }
            } catch (final ClassNotFoundException e) {
                LOG.error("Could not read clazz.", e);
            }
            cache.put(key, ret);
        }
        return cache.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareInsert(final SQLInsert _insert,
                              final Attribute _attribute,
                              final Object... _values)
        throws SQLException
    {
        checkSQLColumnSize(_attribute, 1);
        _insert.column(_attribute.getSqlColNames().get(0), eval(_attribute, _values));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepareUpdate(final SQLUpdate _update,
                              final Attribute _attribute,
                              final Object... _values)
        throws SQLException
    {
        checkSQLColumnSize(_attribute, 1);
        _update.column(_attribute.getSqlColNames().get(0), eval(_attribute, _values));
    }

    /**
     * @param _attribute Attribute for this enumtype
     * @param _value value to be evluated
     * @return integer value
     */
    protected Integer eval(final Attribute _attribute,
                           final Object[] _value)
    {
        final Integer ret;
        if (_value == null) {
            ret = null;
        } else if (_value[0] instanceof String && ((String) _value[0]).length() > 0) {
            ret = Integer.parseInt((String) _value[0]);
        } else if (_value[0] instanceof Integer) {
            ret = (Integer) _value[0];
        } else if (_value[0] instanceof Long) {
            ret = ((Long) _value[0]).intValue();
        } else {
            ret = eval4Enum(_attribute, _value[0]);
        }
        return ret;
    }

    /**
     * @param _attribute Attribute for this enumtype
     * @param _value value to be evluated
     * @return integer value
     */
    protected Integer eval4Enum(final Attribute _attribute,
                                final Object _value)
    {
        final Integer ret;
        if (_value instanceof IEnum) {
            ret = ((IEnum) _value).getInt();
        } else {
            ret = null;
        }
        return ret;
    }
}
