/*
 * Copyright 2003 - 2016 The eFaps Team
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
 *
 */

package org.efaps.admin.datamodel.ui;

import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.util.cache.CacheReloadException;

/**
 * Class to represent a String for the user interface.
 *
 * @author The eFaps Team
 *
 *
 */
public class StringWithUoMUI
    extends AbstractWithUoMProvider
{

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    @Override
    public String validateValue(final UIValue _value)
        throws CacheReloadException
    {
        String ret = null;
        if (_value.getAttribute() != null && _value.getDbValue() != null) {
            if (String.valueOf(_value.getDbValue()).length() > _value.getAttribute().getSize()) {
                ret = DBProperties.getProperty(StringWithUoMUI.class.getName() + ".InvalidValue") + " "
                                + _value.getAttribute().getSize();
            }
        }
        return ret;
    }
}
