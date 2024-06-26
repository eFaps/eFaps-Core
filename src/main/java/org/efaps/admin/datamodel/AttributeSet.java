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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * Basic class for AttributeSets.
 *
 * @author The eFaps Team
 *
 */
public class AttributeSet
    extends Type
{
    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Type of the attribute.
     */
    private long attributeTypeId;

    /**
     * Name of the attribute.
     */
    private String attributeName;

    /**
     * attributes of this set.
     */
    private Set<String> setAttributes = new HashSet<>();

    /**
     * @param _id               id of this set
     * @param _type             type of his set
     * @param _name             name of this set
     * @param _attributeType    type of the attribute
     * @param _sqlColNames      name of the sql column
     * @param _tableId          id of the table
     * @param _typeLinkId       id of the type link
     * @param _uuid             UUID of this AttributeSet as String
     * @throws EFapsException on error
     */
    //CHECKSTYLE:OFF
    protected AttributeSet(final long _id,
                           final long _typeId,
                           final String typeName,
                           final String _name,
                           final long _attributeTypeId,
                           final String _sqlColNames,
                           final long _tableId,
                           final long _typeLinkId,
                           final String _uuid)
        throws EFapsException
    {
        //CHECKSTYLE:ON
        super(_id, _uuid, AttributeSet.evaluateName(typeName, _name));

        this.attributeName = _name == null ? null : _name.trim();

        readFromDB4Properties();

        this.attributeTypeId = _attributeTypeId;

        final Attribute attr = new Attribute(_id, getId(), _name, _sqlColNames, _tableId, AttributeType
                        .get("Link").getId(), null, null);
        addAttributes(false, attr);
        attr.setLinkId(_typeId);
        Attribute.cacheAttribute(attr, this);
        if (_typeLinkId > 0) {
            setParentTypeID(_typeLinkId);
        }
        inheritAttributes();

    }

    protected AttributeSet(final long _id,
                           final String _uuid,
                           final String _name)
        throws EFapsException
    {
        super(_id, _uuid, _name);
    }

    /**
     * Getter method for instance variable {@link #attributeType}.
     *
     * @return value of instance variable {@link #attributeType}
     */
    public AttributeType getAttributeType()
    {
        try {
            return AttributeType.get(attributeTypeId);
        } catch (final CacheReloadException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    protected long getAttributeTypeId() {
        return attributeTypeId;
    }

    protected void setAttributeTypeId(long attributeTypeId)
    {
        this.attributeTypeId = attributeTypeId;
    }

    /**
     * This is the getter method for instance variable {@link #sqlColNames}.
     *
     * @return value of instance variable {@link #sqlColNames}
     * @see #sqlColNames
     */
    public List<String> getSqlColNames()
    {
        return getAttribute(this.attributeName).getSqlColNames();
    }

    /**
     * Getter method for instance variable {@link #attributeName}.
     *
     * @return value of instance variable {@link #attributeName}
     */
    public String getAttributeName()
    {
        return this.attributeName;
    }

    protected void setAttributeName(String attributeName)
    {
        this.attributeName = attributeName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addAttributes(final boolean _inherited,
                                 final Attribute... _attributes)
        throws CacheReloadException
    {
        super.addAttributes(_inherited, _attributes);
        // in the superconstructur this method is called, so the <code>Set<code> might not
        // be initialised
        if (this.setAttributes != null) {
            for (final Attribute attribute : _attributes) {
                this.setAttributes.add(attribute.getName());
            }
        }
    }

    /**
     * Getter method for instance variable {@link #setAttributes}.
     *
     * @return value of instance variable {@link #setAttributes}
     */
    public Set<String> getSetAttributes()
    {
        return this.setAttributes;
    }

    protected void setSetAttributes(Set<String> setAttributes)
    {
        this.setAttributes = setAttributes;
    }

    /**
     * Evaluate the name. (Build the name as the set is cached).
     *
     * @param _typeName name of the type
     * @param _name name of the attribute
     * @return String
     */
    public static String evaluateName(final String _typeName,
                                      final String _name)
    {
        final StringBuilder ret = new StringBuilder();
        ret.append(_typeName).append(":").append(_name).toString();
        return ret.toString();
    }

    /**
     * Method to get the type from the cache.
     *
     * @param _typeName name of the type
     * @param _name name of the attribute
     * @return AttributeSet
     * @throws CacheReloadException on error
     */
    public static AttributeSet get(final String _typeName,
                                   final String _name)
        throws CacheReloadException
    {
        return (AttributeSet) Type.get(AttributeSet.evaluateName(_typeName, _name));
    }

    /**
     * Method to get the type from the cache. Searches if not found in the type
     * hierarchy.
     *
     * @param _typeName name of the type
     * @param _name name of the attribute
     * @return AttributeSet
     * @throws CacheReloadException on error
     */
    public static AttributeSet find(final String _typeName,
                                    final String _name)
        throws CacheReloadException
    {
        AttributeSet ret = (AttributeSet) Type.get(AttributeSet.evaluateName(_typeName, _name));
        if (ret == null) {
            if (Type.get(_typeName).getParentType() != null) {
                ret = AttributeSet.find(Type.get(_typeName).getParentType().getName(), _name);
            }
        }
        return ret;
    }

}
