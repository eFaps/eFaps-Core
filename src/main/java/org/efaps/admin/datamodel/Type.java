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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.efaps.admin.access.AccessSet;
import org.efaps.admin.access.AccessType;
import org.efaps.admin.datamodel.attributetype.AssociationLinkType;
import org.efaps.admin.datamodel.attributetype.BitEnumType;
import org.efaps.admin.datamodel.attributetype.CompanyLinkType;
import org.efaps.admin.datamodel.attributetype.ConsortiumLinkType;
import org.efaps.admin.datamodel.attributetype.EnumType;
import org.efaps.admin.datamodel.attributetype.GroupLinkType;
import org.efaps.admin.datamodel.attributetype.StatusType;
import org.efaps.admin.datamodel.attributetype.TypeType;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.event.EventDefinition;
import org.efaps.admin.event.EventType;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.ui.Form;
import org.efaps.admin.ui.Image;
import org.efaps.admin.ui.Menu;
import org.efaps.ci.CIAdminAccess;
import org.efaps.ci.CIAdminDataModel;
import org.efaps.ci.CIAdminUserInterface;
import org.efaps.ci.CIType;
import org.efaps.db.AttributeQuery;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.QueryCache;
import org.efaps.db.wrapper.SQLPart;
import org.efaps.db.wrapper.SQLSelect;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;
import org.efaps.util.cache.InfinispanCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the class for the type description. The type description holds
 * information about creation of a new instance of a type with default values.
 *
 * @author The eFaps Team
 */
public class Type
    extends AbstractDataModelObject
{

    /**
     * Enum for the different purpose of a type.
     */
    public enum Purpose
                    implements IBitEnum
    {

        /** Abstract purpose. */
        ABSTRACT,
        /** classification purpose. */
        CLASSIFICATION,
        /** GeneralInstane. */
        GENERALINSTANCE,
        /** No GeneralInstane. */
        NOGENERALINSTANCE,
        /** Has history and therfor cannot be deleted. */
        HISTORY;

        /**
         * {@inheritDoc}
         */
        @Override
        public int getInt()
        {
            return BitEnumType.getInt4Index(ordinal());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getBitIndex()
        {
            return ordinal();
        }
    }

    /**
     * Needed for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Type.class);

    /**
     * SQL select statement to select a type from the database by its UUID.
     */
    private static final String SQL_UUID = new SQLSelect()
                    .column("ID")
                    .column("UUID")
                    .column("NAME")
                    .column("PURPOSE")
                    .column("PARENTDMTYPE")
                    .column("PARENTCLASSDMTYPE")
                    .from("V_ADMINTYPE", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "UUID").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * SQL select statement to select a type from the database by its ID.
     */
    private static final String SQL_ID = new SQLSelect()
                    .column("ID")
                    .column("UUID")
                    .column("NAME")
                    .column("PURPOSE")
                    .column("PARENTDMTYPE")
                    .column("PARENTCLASSDMTYPE")
                    .from("V_ADMINTYPE", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "ID").addPart(SQLPart.EQUAL).addValuePart("?").toString();

    /**
     * SQL select statement to select a type from the database by its Name.
     */
    private static final String SQL_NAME = new SQLSelect()
                    .column("ID")
                    .column("UUID")
                    .column("NAME")
                    .column("PURPOSE")
                    .column("PARENTDMTYPE")
                    .column("PARENTCLASSDMTYPE")
                    .from("V_ADMINTYPE", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "NAME").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * SQL select statement to select the ids of child types from the database.
     */
    private static final String SQL_CHILD = new SQLSelect()
                    .column("ID")
                    .column("PURPOSE")
                    .from("V_ADMINTYPE", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "PARENTDMTYPE").addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * SQL select statement to select the ids of child types from the database.
     */
    private static final String SQL_CLASSCHILD = new SQLSelect()
                    .column("ID")
                    .column("PURPOSE")
                    .from("V_ADMINTYPE", 0)
                    .addPart(SQLPart.WHERE).addColumnPart(0, "PARENTCLASSDMTYPE")
                    .addPart(SQLPart.EQUAL).addValuePart("?")
                    .toString();

    /**
     * Name of the Cache by UUID.
     */
    private static String UUIDCACHE = Type.class.getName() + ".UUID";

    /**
     * Name of the Cache by ID.
     */
    private static String IDCACHE = Type.class.getName() + ".ID";

    /**
     * Name of the Cache by Name.
     */
    private static String NAMECACHE = Type.class.getName() + ".Name";

    /**
     * Instance variable for the parent type from which this type is derived.
     *
     * @see #getParentType
     * @see #setParentType
     */
    private Long parentTypeId = null;

    /**
     * Instance variable for all child types ids derived from this type.
     *
     * @see #getChildTypes
     */
    private Set<Long> childTypeIds = new TreeSet<>();

    /**
     * Classification ids which are classifying this type.
     */
    private Set<Long> classifiedByTypeIds = new HashSet<>();

    private boolean classifiedByTypeChecked;

    /**
     * Caching
     *
     */
    private Set<Long> attributeIds = new HashSet<>();

    /**
     * Internal
     */
    private Map<String, Attribute> attributesInternal;
    /**
     * Instance of a HashSet to store all needed tables for this type. The
     * tables are automatically added via the method {@link #add(Attribute)}.
     *
     * @see #add(Attribute)
     * @see #getTables
     */
    private Set<Long> tableIds = new HashSet<>();

    /**
     * The instance variable stores the main table, which must be inserted
     * first. In the main table stands also the select statement to get a new
     * id. The value is automatically set with method {@link #add(Attribute)}.
     *
     * @see Table.mainTable
     * @see #add(Attribute)
     * @see #getMainTable
     * @see #setMainTable
     */
    private Long mainTableId = null;

    /**
     * All access sets ids which are assigned to this type are store in this
     * instance variable. If <code>null</code> the variable was not evaluated
     * yet;
     *
     * @see #addAccessSet
     * @see #getAccessSets
     */
    private Set<Long> accessSetIds = new HashSet<>();

    /**
     * Have the accessSet been evaluated.
     */
    private boolean accessSetChecked = false;

    /**
     * Have the children been evaluated.
     */
    private boolean checked4Children = false;

    /**
     * Stores all type of events which are allowed to fire on this type.
     *
     * @see #setLinkProperty
     */
    private final Set<Long> allowedEventTypes = new HashSet<>();

    /**
     * Id of the store for this type.
     */
    private long storeId;

    /**
     * Is the type abstract.
     */
    private boolean abstractBool;

    /**
     * Is the type abstract.
     */
    private boolean history;

    /**
     * Are the instance of this type general also. Used as a TRISTATE
     * <ol>
     * <li>null = Inherit the value from the parent.</li>
     * <li>true = The instance of this type are general too</li>
     * <li>false = The instance are not general</li>
     * </ol>
     */
    private Boolean generalInstance;

    /**
     * Stores the name of attribute that contains the status of this type. (if
     * exist)
     */
    private String statusAttributeName;

    /**
     * Stores the name of attribute that contains the company of this type. (if
     * exist)
     */
    private String companyAttributeName;

    /**
     * Stores the name of attribute that contains the association of this type.
     * (if exist)
     */
    private String associationAttributeName;

    /**
     * Stores the name of attribute that contains the company of this type. (if
     * exist)
     */
    private String groupAttributeName;

    /**
     * Stores the name of attribute that contains the type of this type. (if
     * exist)
     */
    private String typeAttributeName;

    /**
     * Id of the Menu defined as TypeMenu for this Type.<br/>
     * TRISTATE:<br/>
     * <ul>
     * <li>NULL: TypeMenu not evaluated yet</li>
     * <li>0: has got no TypeMenu</li>
     * <li>n: ID of the TypeMenu</li>
     * </ul>
     */
    private Long typeMenuId;

    private boolean typeMenuChecked = false;

    /**
     * Id of the Icon defined as TypeIcon for this Type.<br/>
     * TRISTATE:<br/>
     * <ul>
     * <li>NULL: TypeIcon not evaluated yet</li>
     * <li>0: has got no TypeMenu</li>
     * <li>n: ID of the TypeMenu</li>
     * </ul>
     */
    private Long typeIconId;

    /**
     * Id of the From defined as TypeFrom for this Type.<br/>
     * TRISTATE:<br/>
     * <ul>
     * <li>NULL: TypeFrom not evaluated yet</li>
     * <li>0: has got no TypeMenu</li>
     * <li>n: ID of the TypeMenu</li>
     * </ul>
     */
    private Long typeFormId;

    private boolean typeFormChecked = false;

    /**
     * This is the constructor for class Type. Every instance of class Type must
     * have a name (parameter <i>_name</i>).
     *
     * @param _id id of th type
     * @param _uuid universal unique identifier
     * @param _name name of the type name of the instance
     * @throws CacheReloadException on error
     */
    protected Type(final long _id,
                   final String _uuid,
                   final String _name)
        throws CacheReloadException
    {
        super(_id, _uuid, _name);
    }

    /**
     * Getter method for instance variable {@link #abstractBool}.
     *
     * @return value of instance variable {@link #abstractBool}
     */
    public boolean isAbstract()
    {
        return abstractBool;
    }

    /**
     * Setter method for instance variable {@link #abstractBool}.
     *
     * @param _abstract value for instance variable {@link #abstractBool}
     */
    protected void setAbstract(final boolean _abstract)
    {
        abstractBool = _abstract;
    }

    /**
     * Getter method for the instance variable {@link #generalInstance}.
     *
     * @return value of instance variable {@link #generalInstance}
     */
    public boolean isGeneralInstance()
    {
        boolean ret = true;
        if (generalInstance != null) {
            ret = generalInstance;
        } else if (getParentType() != null) {
            ret = getParentType().isGeneralInstance();
        }
        return ret;
    }

    /**
     * Setter method for instance variable {@link #generalInstance}.
     *
     * @param _generalInstance value for instance variable
     *            {@link #generalInstance}
     */

    protected void setGeneralInstance(final boolean _generalInstance)
    {
        generalInstance = _generalInstance;
    }

    /**
     * Getter method for the instance variable {@link #history}.
     *
     * @return value of instance variable {@link #history}
     */
    public boolean isHistory()
    {
        return history;
    }

    /**
     * Setter method for instance variable {@link #history}.
     *
     * @param _history value for instance variable {@link #history}
     */
    protected void setHistory(final boolean _history)
    {
        history = _history;
    }

    /**
     * Add attributes to this type and all child types of this type. Recursive
     * method.
     *
     * @param _inherited is the attribute inherited or form this type
     * @param _attributes attributes to add
     * @throws CacheReloadException on error
     */
    protected void addAttributes(final boolean _inherited,
                                 final Attribute... _attributes)
        throws CacheReloadException
    {
        for (final Attribute attribute : _attributes) {
            if (!attributeIds.contains(attribute.getId())) {
                Type.LOG.trace("adding Attribute:'{}' to type: '{}'", attribute.getName(), getName());
                // evaluate for type attribute
                if (attribute.getAttributeType().getClassRepr().equals(TypeType.class)) {
                    typeAttributeName = attribute.getName();
                } else if (attribute.getAttributeType().getClassRepr().equals(StatusType.class) && !_inherited) {
                    // evaluate for status, an inherited attribute will not
                    // overwrite the original attribute
                    statusAttributeName = attribute.getName();
                } else if (attribute.getAttributeType().getClassRepr().equals(CompanyLinkType.class)
                                || attribute.getAttributeType().getClassRepr().equals(ConsortiumLinkType.class)) {
                    // evaluate for company
                    companyAttributeName = attribute.getName();
                } else if (attribute.getAttributeType().getClassRepr().equals(GroupLinkType.class)) {
                    // evaluate for group
                    groupAttributeName = attribute.getName();
                } else if (attribute.getAttributeType().getClassRepr().equals(AssociationLinkType.class)) {
                    // evaluate for association
                    associationAttributeName = attribute.getName();
                }
                attributeIds.add(attribute.getId());
                if (attribute.getTable() != null) {
                    tableIds.add(attribute.getTable().getId());
                    attribute.getTable().addType(getId());
                    if (getMainTable() == null) {
                        setMainTable(attribute.getTable());
                    }
                }
            }
        }
    }

    /**
     * Inherit Attributes are child types.
     *
     * @throws CacheReloadException on error
     */
    protected void inheritAttributes()
        throws CacheReloadException
    {
        Type parent = getParentType();
        final List<Attribute> attributesTmp = new ArrayList<>();
        while (parent != null) {
            for (final Attribute attribute : parent.getAttributes().values()) {
                attributesTmp.add(attribute.copy(getId()));
            }
            parent = parent.getParentType();
        }
        addAttributes(true, attributesTmp.toArray(new Attribute[attributesTmp.size()]));
    }

    /**
     * Getter method for instance variable {@link #statusAttribute}.
     *
     * @return value of instance variable {@link #statusAttribute}
     */
    public Attribute getStatusAttribute()
    {
        return getAttribute(statusAttributeName);
    }

    protected String getStatusAttributeName()
    {
        return statusAttributeName;
    }

    /**
     * Method to evaluate if the status must be checked on an accesscheck.
     *
     * @return true if {@link #statusAttribute} !=null , else false
     */
    public boolean isCheckStatus()
    {
        return statusAttributeName != null;
    }

    /**
     * Method to evaluate if this type depends on companies.
     *
     * @return true if {@link #companyAttribute} !=null , else false
     */
    public boolean isCompanyDependent()
    {
        return companyAttributeName != null;
    }

    public boolean hasAssociation()
    {
        return associationAttributeName != null;
    }

    /**
     * Method to evaluate if this type depends on companies.
     *
     * @return true if {@link #groupAttributeName} !=null , else false
     */
    public boolean isGroupDependent()
    {
        return groupAttributeName != null;
    }

    /**
     * Get the attribute containing the company information.
     *
     * @return attribute containing the company information
     */
    public Attribute getCompanyAttribute()
    {
        return getAttribute(companyAttributeName);
    }

    /**
     * Get the attribute containing the company information.
     *
     * @return attribute containing the company information
     */
    public Attribute getAssociationAttribute()
    {
        return getAttribute(associationAttributeName);
    }

    /**
     * Get the attribute containing the group information.
     *
     * @return attribute containing the group information
     */
    public Attribute getGroupAttribute()
    {
        return getAttribute(groupAttributeName);
    }

    /**
     * Get the attribute containing the type information.
     *
     * @return attribute containing the type information
     */
    public Attribute getTypeAttribute()
    {
        Attribute ret = null;
        if (typeAttributeName == null && getParentType() != null) {
            ret = getParentType().getTypeAttribute();
        } else {
            ret = getAttribute(typeAttributeName);
        }
        return ret;
    }

    /**
     * Returns for the given parameter <b>_name</b> the attribute.
     *
     * @param _name name of the attribute for this type to return
     * @return instance of class {@link Attribute}
     */
    public final Attribute getAttribute(final String _name)
    {
        return getAttributes().get(_name);
    }

    /**
     * The instance method returns all attributes which are from the same
     * attribute type as the described with the parameter <i>_class</i>.
     *
     * @param _class searched attribute type
     * @return all attributes assigned from parameter <i>_class</i>
     */
    public final Set<Attribute> getAttributes(final Class<?> _class)
    {
        final Set<Attribute> ret = new HashSet<>();
        for (final Attribute attribute : getAttributes().values()) {
            if (attribute.getAttributeType().getClassRepr() == _class) {
                ret.add(attribute);
            }
        }
        return ret;
    }

    /**
     * Tests, if this type is kind of the type in the parameter (question is, is
     * this type a child of the parameter type).
     *
     * @param _type type to test for parent
     * @return true if this type is a child, otherwise false
     */
    public boolean isKindOf(final Type _type)
    {
        boolean ret = false;
        if (_type != null) {
            Type type = this;
            while (type != null && type.getId() != _type.getId()) {
                type = type.getParentType();
            }
            if (type != null && type.getId() == _type.getId()) {
                ret = true;
            }
        }
        return ret;
    }

    /**
     * Tests, if this type is kind of the type in the parameter (question is, is
     * this type a child of the parameter type).
     *
     * @param _ciType CIType to test for parent
     * @return true if this type is a child, otherwise false
     */
    public boolean isKindOf(final CIType _ciType)
    {
        return isKindOf(_ciType.getType());
    }

    /**
     * Tests, if this type is the given CIType.
     *
     * @param _ciType CIType to test for parent
     * @return true if this type is a child, otherwise false
     */
    public boolean isCIType(final CIType _ciType)
    {
        return equals(_ciType.getType());
    }

    /**
     * Checks if the current type holds the property with the given name. If
     * not, the value of the property of the parent type (see
     * {@link #getParentType}) is returned (if a parent type exists).
     *
     * @param _name name of the property (key)
     * @return value of the property with the given name / key.
     * @see org.efaps.admin.AbstractAdminObject#getProperty
     */
    @Override
    public String getProperty(final String _name)
    {
        String value = super.getProperty(_name);
        if (value == null && getParentType() != null) {
            value = getParentType().getProperty(_name);
        }
        return value;
    }

    /**
     * Checks, if the current context user has all access defined in the list of
     * access types for the given instance.
     *
     * @param _instance instance for which the access must be checked
     * @param _accessType list of access types which must be checked
     * @throws EFapsException on error
     * @return true if user has access, else false
     */
    public boolean hasAccess(final Instance _instance,
                             final AccessType _accessType)
        throws EFapsException
    {
        return hasAccess(_instance, _accessType, null);
    }

    /**
     * Checks, if the current context user has all access defined in the list of
     * access types for the given instance.
     *
     * @param _instance instance for which the access must be checked
     * @param _accessType list of access types which must be checked
     * @param _newValues objects that will be passed to esjp as
     *            <code>NEW_VALUES</code>
     * @throws EFapsException on error
     * @return true if user has access, else false
     */
    public boolean hasAccess(final Instance _instance,
                             final AccessType _accessType,
                             final Object _newValues)
        throws EFapsException
    {
        boolean hasAccess = true;
        final List<EventDefinition> events = super.getEvents(EventType.ACCESSCHECK);
        if (events != null) {
            final Parameter parameter = new Parameter();
            parameter.put(ParameterValues.INSTANCE, _instance);
            parameter.put(ParameterValues.ACCESSTYPE, _accessType);
            parameter.put(ParameterValues.NEW_VALUES, _newValues);
            parameter.put(ParameterValues.CLASS, this);

            for (final EventDefinition event : events) {
                final Return ret = event.execute(parameter);
                hasAccess = ret.get(ReturnValues.TRUE) != null;
            }
        }
        return hasAccess;
    }

    /**
     * Method to check the access right for a list of instances.
     *
     * @param _instances list of instances
     * @param _accessType access type
     * @throws EFapsException on error
     * @return Map of instances to boolean
     */
    @SuppressWarnings("unchecked")
    public Map<Instance, Boolean> checkAccess(final Collection<Instance> _instances,
                                              final AccessType _accessType)
        throws EFapsException
    {
        Map<Instance, Boolean> ret = new HashMap<>();
        if (_instances != null && !_instances.isEmpty() && _instances.size() == 1) {
            final Instance instance = _instances.iterator().next();
            ret.put(instance, hasAccess(instance, _accessType));
        } else {
            final List<EventDefinition> events = super.getEvents(EventType.ACCESSCHECK);
            if (events != null) {
                final Parameter parameter = new Parameter();
                parameter.put(ParameterValues.OTHERS, _instances);
                parameter.put(ParameterValues.ACCESSTYPE, _accessType);
                parameter.put(ParameterValues.CLASS, this);
                for (final EventDefinition event : events) {
                    final Return retrn = event.execute(parameter);
                    ret = (Map<Instance, Boolean>) retrn.get(ReturnValues.VALUES);
                }
            } else {
                for (final Instance instance : _instances) {
                    ret.put(instance, true);
                }
            }
        }
        return ret;
    }

    /**
     * @param _accessSet AccessSet to add to this Type
     */
    public void addAccessSet(final AccessSet _accessSet)
    {
        accessSetIds.add(_accessSet.getId());
    }

    /**
     * This is the getter method for instance variable {@link #accessSets}.
     *
     * @return value of instance variable {@link #accessSets}
     * @see #accessSets
     * @throws EFapsException on error
     */
    public Set<AccessSet> getAccessSets()
        throws EFapsException
    {
        if (!accessSetChecked) {
            final QueryBuilder queryBldr = new QueryBuilder(CIAdminAccess.AccessSet2DataModelType);
            queryBldr.addWhereAttrEqValue(CIAdminAccess.AccessSet2DataModelType.DataModelTypeLink, getId());
            final MultiPrintQuery multi = queryBldr.getPrint();
            multi.addAttribute(CIAdminAccess.AccessSet2DataModelType.AccessSetLink);
            multi.executeWithoutAccessCheck();
            while (multi.next()) {
                final Long accessSet = multi.<Long>getAttribute(CIAdminAccess.AccessSet2DataModelType.AccessSetLink);
                AccessSet.get(accessSet);
                accessSetIds.add(accessSet);
            }
            accessSetChecked = true;
            updateCache();
        }
        final Set<AccessSet> ret = new HashSet<>();
        for (final Long id : accessSetIds) {
            ret.add(AccessSet.get(id));
        }
        return Collections.unmodifiableSet(ret);
    }

    protected Set<Long> getAccessSetIds()
    {
        return accessSetIds;
    }

    protected void setAccessSetIds(Set<Long> accessSetIds)
    {
        this.accessSetIds = accessSetIds;
    }

    protected boolean isAccessSetChecked()
    {
        return accessSetChecked;
    }

    protected void setAccessSetChecked(final boolean accessSetChecked)
    {
        this.accessSetChecked = accessSetChecked;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setLinkProperty(final UUID _linkTypeUUID,
                                   final long _toId,
                                   final UUID _toTypeUUID,
                                   final String _toName)
        throws EFapsException
    {
        if (_linkTypeUUID.equals(CIAdminDataModel.Type2Store.uuid)) {
            storeId = _toId;
        } else if (_linkTypeUUID.equals(CIAdminDataModel.TypeEventIsAllowedFor.uuid)) {
            allowedEventTypes.add(_toId);
        }
        super.setLinkProperty(_linkTypeUUID, _toId, _toTypeUUID, _toName);
    }

    /**
     * For the given type it is tested if a store is defined for the type.
     *
     * @return <i>true</i> if a store resource is defined for the type,
     *         otherwise <i>false</i> is returned
     */
    public boolean hasStore()
    {
        return getStoreId() > 0 ? true : false;
    }

    /**
     * This is the getter method for instance variable {@link #parentType}.
     *
     * @return value of instance variable {@link #parentType}
     * @see #parentType
     * @see #setParentType
     */
    public Type getParentType()
    {
        Type ret = null;
        if (parentTypeId != null && parentTypeId != 0) {
            try {
                ret = Type.get(parentTypeId);
            } catch (final CacheReloadException e) {
                Type.LOG.error("Could not read parentType for id: {}", parentTypeId);
            }
        }
        return ret;
    }

    /**
     * Getter method for the instance variable {@link #parentTypeId}.
     *
     * @return value of instance variable {@link #parentTypeId}
     */
    protected Long getParentTypeId()
    {
        return parentTypeId;
    }

    /**
     * Setter method for instance variable {@link #parentType}.
     *
     * @param _parentTypeId parentid to set
     */
    protected void setParentTypeID(final Long parentTypeId)
    {
        this.parentTypeId = parentTypeId;
    }

    /**
     * Add a root Classification to this type.
     *
     * @param _classification classifixation that classifies this type
     */
    protected void addClassifiedByType(final Classification _classification)
    {
        classifiedByTypeIds.add(_classification.getId());
    }

    protected Set<Long> getClassifiedByTypeIds()
    {
        return classifiedByTypeIds;
    }

    protected void setClassifiedByTypeIds(Set<Long> classifiedByTypeIds)
    {
        this.classifiedByTypeIds = classifiedByTypeIds;
    }

    /**
     * Getter method for instance variable {@link #classifiedByTypes}. The
     * method retrieves lazy the Classification Types.
     *
     * @return value of instance variable {@link #classifiedByTypes}
     * @throws EFapsException on error
     */
    public Set<Classification> getClassifiedByTypes()
        throws EFapsException
    {
        if (!classifiedByTypeChecked) {
            final QueryBuilder attrQueryBldr = new QueryBuilder(CIAdminDataModel.TypeClassifies);
            attrQueryBldr.addWhereAttrEqValue(CIAdminDataModel.TypeClassifies.To, getId());
            final AttributeQuery attrQuery = attrQueryBldr.getAttributeQuery(CIAdminDataModel.TypeClassifies.From);
            final QueryBuilder queryBldr = new QueryBuilder(CIAdminDataModel.Type);
            queryBldr.addWhereAttrInQuery(CIAdminDataModel.Type.ID, attrQuery);
            final InstanceQuery query = queryBldr.getQuery();
            query.executeWithoutAccessCheck();
            while (query.next()) {
                classifiedByTypeIds.add(query.getCurrentValue().getId());
            }
            classifiedByTypeChecked = true;
            updateCache();
        }
        final Set<Classification> ret = new HashSet<>();
        if (getParentType() != null) {
            ret.addAll(getParentType().getClassifiedByTypes());
        }
        for (final Long id : classifiedByTypeIds) {
            final var classType = Type.get(id);
            if (classType instanceof Classification) {
                ret.add((Classification) classType);
            }
        }
        return Collections.unmodifiableSet(ret);
    }

    protected boolean isClassifiedByTypeChecked()
    {
        return classifiedByTypeChecked;
    }

    protected void setClassifiedByTypeChecked(boolean classifiedByTypeChecked)
    {
        this.classifiedByTypeChecked = classifiedByTypeChecked;
    }

    /**
     * This is the getter method for instance variable {@link #childTypes}.
     *
     * @return value of instance variable {@link #childTypes}
     * @see #childTypes
     * @throws CacheReloadException on error
     */
    public Set<Type> getChildTypes()
        throws CacheReloadException
    {
        final Set<Type> ret = new LinkedHashSet<>();
        for (final Long id : childTypeIds) {
            Type child = Type.get(id);
            if (child == null) {
                child = Type.get(id);
            }
            ret.add(child);
            ret.addAll(child.getChildTypes());
        }
        return Collections.unmodifiableSet(ret);
    }

    protected Set<Long> getChildTypeIds()
    {
        return childTypeIds;
    }

    protected void setChildTypeIds(Set<Long> childTypeIds)
    {
        this.childTypeIds = childTypeIds;
        this.checked4Children = true;
    }

    public boolean hasChildren()
    {
        return !childTypeIds.isEmpty();
    }

    /**
     * This is the getter method for instance variable {@link #attributes}.
     *
     * @return value of instance variable {@link #attributes}
     * @see #attributes
     */
    public Map<String, Attribute> getAttributes()
    {
        final Map<String, Attribute> ret;
        if (attributesInternal != null) {
            ret = attributesInternal;
        } else {
            ret = new HashMap<>();
            for (final var attributeId : attributeIds) {
                try {
                    final var attr = Attribute.get(attributeId);
                    ret.put(attr.getName(), attr.copy(getId()));
                } catch (final CacheReloadException e) {
                    LOG.error("Problems on loading attrbutes", e);
                }
            }
            attributesInternal = ret;
        }
        return ret;
    }

    /**
     * This is the getter method for instance variable {@link #tables}.
     *
     * @return value of instance variable {@link #tables}
     * @see #tables
     */
    public Set<SQLTable> getTables()
    {
        return tableIds.stream().map(arg0 -> {
            try {
                return SQLTable.get(arg0);
            } catch (final CacheReloadException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    protected Set<Long> getTableIds()
    {
        return tableIds;
    }

    protected void setTableIds(Set<Long> tableIds)
    {
        this.tableIds = tableIds;
    }

    /**
     * This is the getter method for instance variable {@link #mainTable}.
     *
     * @return value of instance variable {@link #mainTable}
     * @see #setMainTable
     * @see #mainTable
     */
    public SQLTable getMainTable()
    {
        SQLTable ret = null;
        if (mainTableId != null && mainTableId != 0) {
            try {
                ret = SQLTable.get(mainTableId);
            } catch (final CacheReloadException e) {
                e.printStackTrace();
            }
        } else if (getParentType() != null) {
            ret = getParentType().getMainTable();
        }
        return ret;
    }

    protected Long getMainTableId()
    {
        return mainTableId;
    }

    protected void setMainTableId(Long mainTableId)
    {
        this.mainTableId = mainTableId;
    }

    /**
     * This is the setter method for instance variable {@link #mainTable}.
     *
     * @param _mainTable new value for instance variable {@link #mainTable}
     * @see #getMainTable
     * @see #mainTable
     */
    private void setMainTable(final SQLTable _mainTable)
    {
        SQLTable table = _mainTable;
        while (table.getMainTable() != null) {
            table = table.getMainTable();
        }
        mainTableId = table == null ? null : table.getId();
    }

    /**
     * This is the getter method for instance variable
     * {@link #allowedEventTypes}.
     *
     * @return value of instance variable {@link #allowedEventTypes}
     * @see #allowedEventTypes
     * @throws CacheReloadException on error
     */
    public Set<Type> getAllowedEventTypes()
        throws CacheReloadException
    {
        final Set<Type> ret = new HashSet<>();
        for (final Long id : allowedEventTypes) {
            ret.add(Type.get(id));
        }
        return Collections.unmodifiableSet(ret);
    }

    /**
     * Getter method for instance variable {@link #storeId}.
     *
     * @return value of instance variable {@link #storeId}
     */
    public long getStoreId()
    {
        final long ret;
        if (storeId == 0 && getParentType() != null) {
            ret = getParentType().getStoreId();
        } else {
            ret = storeId;
        }
        return ret;
    }

    /**
     * Method to get the key to the label.
     *
     * @return key to the label
     */
    public String getLabelKey()
    {
        final StringBuilder keyStr = new StringBuilder();
        return keyStr.append(getName()).append(".Label").toString();
    }

    /**
     * Method to get the translated label for this Status.
     *
     * @return translated Label
     */
    public String getLabel()
    {
        return DBProperties.getProperty(getLabelKey());
    }

    /**
     * @return the TypeMenu for this type
     * @throws EFapsException on errot
     */
    public Menu getTypeMenu()
        throws EFapsException
    {
        Menu ret = null;
        if (!typeMenuChecked) {
            final QueryBuilder queryBldr = new QueryBuilder(CIAdminUserInterface.LinkIsTypeTreeFor);
            queryBldr.addWhereAttrEqValue(CIAdminUserInterface.LinkIsTypeTreeFor.To, getId());
            final MultiPrintQuery multi = queryBldr.getPrint();
            multi.addAttribute(CIAdminUserInterface.LinkIsTypeTreeFor.From);
            multi.executeWithoutAccessCheck();
            if (multi.next()) {
                final Long menuId = multi.<Long>getAttribute(CIAdminUserInterface.LinkIsTypeTreeFor.From);
                ret = Menu.get(menuId);
                if (ret != null) {
                    typeMenuId = ret.getId();
                    ret.setTypeMenu(true);
                }
            }
            typeMenuChecked = true;
            updateCache();
        }
        if (typeMenuId == null && getParentType() != null) {
            ret = getParentType().getTypeMenu();
        } else {
            ret = Menu.get(typeMenuId);
        }
        return ret;
    }

    protected boolean isTypeMenuChecked()
    {
        return typeMenuChecked;
    }

    protected void setTypeMenuChecked(boolean typeMenuChecked)
    {
        this.typeMenuChecked = typeMenuChecked;
    }

    /**
     * @return the TypeIcon for this type
     * @throws EFapsException on errot
     */

    public Image getTypeIcon()
        throws EFapsException
    {
        Image ret = null;
        if (typeIconId == null) {
            final QueryBuilder queryBldr = new QueryBuilder(CIAdminUserInterface.LinkIsTypeIconFor);
            queryBldr.addWhereAttrEqValue(CIAdminUserInterface.LinkIsTypeIconFor.To, getId());
            final MultiPrintQuery multi = queryBldr.getPrint();
            multi.addAttribute(CIAdminUserInterface.LinkIsTypeIconFor.From);
            multi.executeWithoutAccessCheck();
            if (multi.next()) {
                final Long menuId = multi.<Long>getAttribute(CIAdminUserInterface.LinkIsTypeIconFor.From);
                ret = Image.get(menuId);
                if (ret != null) {
                    typeIconId = ret.getId();
                } else {
                    typeIconId = (long) 0;
                }
            } else {
                typeIconId = (long) 0;
            }
        }
        if (typeIconId == 0 && getParentType() != null) {
            ret = getParentType().getTypeIcon();
        } else {
            ret = Image.get(typeIconId);
        }
        return ret;
    }

    /**
     * @return the TypeFrom for this type
     * @throws EFapsException on errot
     */
    public Form getTypeForm()
        throws EFapsException
    {
        Form ret = null;
        if (!typeFormChecked) {
            final QueryBuilder queryBldr = new QueryBuilder(CIAdminUserInterface.LinkIsTypeFormFor);
            queryBldr.addWhereAttrEqValue(CIAdminUserInterface.LinkIsTypeFormFor.To, getId());
            final MultiPrintQuery multi = queryBldr.getPrint();
            multi.addAttribute(CIAdminUserInterface.LinkIsTypeFormFor.From);
            multi.executeWithoutAccessCheck();
            if (multi.next()) {
                final Long formId = multi.<Long>getAttribute(CIAdminUserInterface.LinkIsTypeFormFor.From);
                ret = Form.get(formId);
                if (ret != null) {
                    typeFormId = ret.getId();
                }
            }
            typeFormChecked = true;
            updateCache();
        }
        if (typeFormId == null && getParentType() != null) {
            ret = getParentType().getTypeForm();
        } else {
            ret = Form.get(typeFormId);
        }
        return ret;
    }

    protected boolean isTypeFormChecked()
    {
        return typeFormChecked;
    }

    protected void setTypeFormChecked(boolean typeFormChecked)
    {
        this.typeFormChecked = typeFormChecked;
    }

    protected String getCompanyAttributeName()
    {
        return companyAttributeName;
    }

    protected void setStatusAttributeName(String statusAttributeName)
    {
        this.statusAttributeName = statusAttributeName;
    }

    protected void setCompanyAttributeName(String companyAttributeName)
    {
        this.companyAttributeName = companyAttributeName;
    }

    protected void setGroupAttributeName(String groupAttributeName)
    {
        this.groupAttributeName = groupAttributeName;
    }

    protected void setTypeAttributeName(String typeAttributeName)
    {
        this.typeAttributeName = typeAttributeName;
    }

    protected void setTypeMenuId(Long typeMenuId)
    {
        this.typeMenuId = typeMenuId;
    }

    protected void setTypeIconId(Long typeIconId)
    {
        this.typeIconId = typeIconId;
    }

    protected void setTypeFormId(Long typeFormId)
    {
        this.typeFormId = typeFormId;
    }

    protected String getAssociationAttributeName()
    {
        return associationAttributeName;
    }

    protected String getGroupAttributeName()
    {
        return groupAttributeName;
    }

    protected String getTypeAttributeName()
    {
        return typeAttributeName;
    }

    protected Long getTypeMenuId()
    {
        return typeMenuId;
    }

    protected Long getTypeIconId()
    {
        return typeIconId;
    }

    protected Long getTypeFormId()
    {
        return typeFormId;
    }

    protected long getStoreIdInternal()
    {
        return storeId;
    }

    protected void setStoreId(long storeId)
    {
        this.storeId = storeId;
    }

    protected void setAssociationAttributeName(String associationAttributeName)
    {
        this.associationAttributeName = associationAttributeName;
    }

    protected Set<Long> getAttributeIds()
    {
        return attributeIds;
    }

    protected void setAttributeIds(final Set<Long> attributeIds)
    {
        this.attributeIds = attributeIds;
    }

    @Override
    protected void updateCache()
        throws CacheReloadException
    {
        cacheType(this);
    }

    /**
     * The method overrides the original method 'toString' and returns
     * information about this type instance.
     *
     * @return name of the user interface object
     */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this).appendSuper(super.toString())
                        .append("parentTypeId", parentTypeId)
                        .append("attributes", attributeIds.size())
                        .append("children", childTypeIds.size())
                        .append("abstract", abstractBool)
                        .append("accessSets", accessSetIds.size())
                        .append("companyDependend", isCompanyDependent())
                        .append("hasAssociation", hasAssociation())
                        .append("groupDependend", isGroupDependent())
                        .append("statusDependend", isCheckStatus())
                        .append("accessSetChecked", accessSetChecked)
                        .append("checked4Children", checked4Children)
                        .append("classifiedByTypeChecked", classifiedByTypeChecked)
                        .toString();
    }

    @Override
    public boolean equals(final Object _obj)
    {
        final boolean ret;
        if (_obj instanceof Type) {
            ret = ((Type) _obj).getId() == getId();
        } else {
            ret = super.equals(_obj);
        }
        return ret;
    }

    @Override
    public int hashCode()
    {
        return Long.valueOf(getId()).intValue();
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     *
     * @param _class class that called the method
     * @throws CacheReloadException on error
     */
    public static void initialize(final Class<?> _class)
        throws CacheReloadException
    {
        InfinispanCache.get().<UUID, Type>initCache(Type.UUIDCACHE, Type.LOG);
        InfinispanCache.get().<Long, Type>initCache(Type.IDCACHE, Type.LOG);
        InfinispanCache.get().<String, Type>initCache(Type.NAMECACHE, Type.LOG);
        InfinispanCache.get().<String, Object>initCache(EnumType.CACHE, Type.LOG);
        QueryCache.initialize();
    }

    /**
     * Method to initialize the Cache of this CacheObjectInterface.
     *
     * @throws CacheReloadException on error
     */
    public static void initialize()
        throws CacheReloadException
    {
        Type.initialize(Type.class);
    }

    /**
     * Returns for given parameter <i>_id</i> the instance of class {@link Type}
     * .
     *
     * @param _id id of the type to get
     * @return instance of class {@link Type}
     * @throws CacheReloadException on error
     */
    public static Type get(final long _id)
        throws CacheReloadException
    {
        final var cache = InfinispanCache.get().<Long, Type>getCache(Type.IDCACHE);
        if (!cache.containsKey(_id)) {
            Type.getTypeFromDB(Type.SQL_ID, _id);
        }
        return cache.get(_id);
    }

    /**
     * Returns for given parameter <i>_name</i> the instance of class
     * {@link Type}.
     *
     * @param _name name of the type to get
     * @return instance of class {@link Type}
     * @throws CacheReloadException on error
     */
    public static Type get(final String _name)
        throws CacheReloadException
    {
        final var cache = InfinispanCache.get().<String, Type>getCache(Type.NAMECACHE);
        if (!cache.containsKey(_name)) {
            Type.getTypeFromDB(Type.SQL_NAME, _name);
        }
        return cache.get(_name);
    }

    /**
     * Returns for given parameter <i>_uuid</i> the instance of class
     * {@link Type}.
     *
     * @param _uuid uuid of the type to get
     * @return instance of class {@link Type}
     * @throws CacheReloadException on error
     */
    public static Type get(final UUID _uuid)
        throws CacheReloadException
    {
        final var cache = InfinispanCache.get().<UUID, Type>getCache(Type.UUIDCACHE);
        if (!cache.containsKey(_uuid)) {
            Type.getTypeFromDB(Type.SQL_UUID, _uuid.toString());
        }
        return cache.get(_uuid);
    }

    /**
     * @param _type type to be cached
     */
    protected static void cacheType(final Type _type)
    {
        final var cache4UUID = InfinispanCache.get().<UUID, Type>getCache(Type.UUIDCACHE);
        cache4UUID.put(_type.getUUID(), _type);

        final var nameCache = InfinispanCache.get().<String, Type>getCache(Type.NAMECACHE);
        nameCache.put(_type.getName(), _type);

        final var idCache = InfinispanCache.get().<Long, Type>getCache(Type.IDCACHE);
        idCache.put(_type.getId(), _type);
    }

    /**
     * @param _parentID id to be searched for
     * @param _statement statement to be executed
     * @return a list of object containing the id and the purpose
     * @throws CacheReloadException on error
     */
    private static List<Object[]> getChildTypeIDs(final long _parentID,
                                                  final String _statement)
        throws CacheReloadException
    {
        final List<Object[]> ret = new ArrayList<>();
        Connection con = null;
        try {
            con = Context.getConnection();
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement(_statement);
                stmt.setObject(1, _parentID);
                final ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    ret.add(new Object[] { rs.getLong(1), rs.getInt(2) });
                }
                rs.close();
            } finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
            con.commit();
            con.close();
        } catch (final SQLException e) {
            throw new CacheReloadException("could not read child type ids", e);
        } catch (final EFapsException e) {
            throw new CacheReloadException("could not read child type ids", e);
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (final SQLException e) {
                throw new CacheReloadException("could not read child type ids", e);
            }
        }
        return ret;
    }

    /**
     * @param _sql SQLStatement to be executed
     * @param _criteria the filter criteria
     * @return Type instance
     * @throws CacheReloadException on error
     */
    private static Type getTypeFromDB(final String _sql,
                                      final Object _criteria)
        throws CacheReloadException
    {
        LOG.info("Loading Type from db by: {}", _criteria);
        Type ret = null;
        Connection con = null;
        try {
            con = Context.getConnection();
            final PreparedStatement stmt = con.prepareStatement(_sql);
            stmt.setObject(1, _criteria);
            final ResultSet rs = stmt.executeQuery();
            long parentTypeId = 0;
            long parentClassTypeId = 0;
            long id = 0;

            if (rs.next()) {
                id = rs.getLong(1);
                final String uuid = rs.getString(2).trim();
                final String name = rs.getString(3).trim();
                final int purpose = rs.getInt(4);
                parentTypeId = rs.getLong(5);
                parentClassTypeId = rs.getLong(6);

                Type.LOG.debug("read type '{}' (id = {}) (purpose = {}) (parentTypeId = {}) (parentClassTypeId = {})",
                                name, id, purpose, parentTypeId, parentClassTypeId);

                if (BitEnumType.isSelected(purpose, Type.Purpose.CLASSIFICATION)) {
                    ret = new Classification(id, uuid, name);
                    if (parentClassTypeId != 0) {
                        ((Classification) ret).setParentClassification(parentClassTypeId);
                    }
                } else {
                    ret = new Type(id, uuid, name);
                }
                if (parentTypeId != 0) {
                    ret.setParentTypeID(parentTypeId);
                }
                ret.setAbstract(BitEnumType.isSelected(purpose, Type.Purpose.ABSTRACT));
                ret.setHistory(BitEnumType.isSelected(purpose, Type.Purpose.HISTORY));
                if (BitEnumType.isSelected(purpose, Type.Purpose.GENERALINSTANCE)) {
                    ret.setGeneralInstance(true);
                }
                if (BitEnumType.isSelected(purpose, Type.Purpose.NOGENERALINSTANCE)) {
                    ret.setGeneralInstance(false);
                }
            }
            rs.close();
            stmt.close();
            con.commit();
            con.close();
            if (ret != null) {
                if (!ret.checked4Children) {
                    ret.checked4Children = true;
                    for (final Object[] childIDs : Type.getChildTypeIDs(ret.getId(), Type.SQL_CHILD)) {
                        Type.LOG.trace("reading Child Type with id: {} for type :{}", childIDs[0], ret.getName());
                        ret.childTypeIds.add((Long) childIDs[0]);
                    }
                    if (ret instanceof Classification) {
                        for (final Object[] childIDs : Type.getChildTypeIDs(ret.getId(), Type.SQL_CLASSCHILD)) {
                            Type.LOG.trace("reading Child class Type with id: {} for type :{}",
                                            childIDs[0], ret.getName());
                            ((Classification) ret).getChildrenIds().add((Long) childIDs[0]);
                        }
                    }
                }
                // Type.cacheType(ret);
                Attribute.add4Type(ret);
                ret.readFromDB4Links();
                ret.readFromDB4Properties();
                ret.inheritAttributes();
                // needed due to cluster serialization that does not update
                // automatically
                Type.cacheType(ret);
                Type.LOG.trace("ended reading type '{}'", ret.getName());
            }
        } catch (final EFapsException e) {
            Type.LOG.error("initialiseCache()", e);
        } catch (final SQLException e) {
            Type.LOG.error("initialiseCache()", e);
        } finally {
            try {
                if (con != null && !con.isClosed()) {
                    con.close();
                }
            } catch (final SQLException e) {
                throw new CacheReloadException("Cannot read a type for an attribute.", e);
            }
        }
        return ret;
    }

    /**
     * Compares a given id and UUID of a type to evaluate if they are from the
     * same Type. In case that they are not cached (during initialize) the
     * database is requested.
     *
     * @param _typeId Id of the type to be checked
     * @param _typeUUID uuid of the type to be checked
     * @return true if the id and the UUID belong to the same type
     * @throws CacheReloadException on error
     */
    protected static boolean check4Type(final long _typeId,
                                        final UUID _typeUUID)
        throws CacheReloadException
    {
        boolean ret = false;
        final var cache = InfinispanCache.get().<Long, Type>getCache(Type.IDCACHE);
        if (cache.containsKey(_typeId)) {
            ret = cache.get(_typeId).getUUID().equals(_typeUUID);
        } else {
            Connection con = null;
            String uuidTmp = "";
            try {
                con = Context.getConnection();
                PreparedStatement stmt = null;
                try {
                    stmt = con.prepareStatement(Type.SQL_ID);
                    stmt.setObject(1, _typeId);
                    final ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        uuidTmp = rs.getString(2).trim();
                    }
                    rs.close();
                } finally {
                    if (stmt != null) {
                        stmt.close();
                    }
                }
                con.commit();
                con.close();
            } catch (final SQLException e) {
                throw new CacheReloadException("could not read child type ids", e);
            } catch (final EFapsException e) {
                throw new CacheReloadException("could not read child type ids", e);
            } finally {
                try {
                    if (con != null && !con.isClosed()) {
                        con.close();
                    }
                } catch (final SQLException e) {
                    throw new CacheReloadException("could not read child type ids", e);
                }
            }
            ret = StringUtils.isNotEmpty(uuidTmp) && UUID.fromString(uuidTmp).equals(_typeUUID);
        }
        return ret;
    }

    /**
     * During the initial caching of types, the mapping does not exists but is
     * necessary.
     *
     * @param _typeUUID UUID of the type the id is wanted for
     * @return id of the type
     * @throws CacheReloadException on error
     */
    protected static long getId4UUID(final UUID _typeUUID)
        throws CacheReloadException
    {
        long ret = 0;
        final var cache = InfinispanCache.get().<UUID, Type>getCache(Type.UUIDCACHE);
        if (cache.containsKey(_typeUUID)) {
            ret = cache.get(_typeUUID).getId();
        } else {
            Connection con = null;
            try {
                con = Context.getConnection();
                PreparedStatement stmt = null;
                try {
                    stmt = con.prepareStatement(Type.SQL_UUID);
                    stmt.setObject(1, _typeUUID.toString());
                    final ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        ret = rs.getLong(1);
                    }
                    rs.close();
                } finally {
                    if (stmt != null) {
                        stmt.close();
                    }
                }
                con.commit();
            } catch (final SQLException e) {
                throw new CacheReloadException("could not read child type ids", e);
            } catch (final EFapsException e) {
                throw new CacheReloadException("could not read child type ids", e);
            } finally {
                try {
                    if (con != null && !con.isClosed()) {
                        con.close();
                    }
                } catch (final SQLException e) {
                    throw new CacheReloadException("could not read child type ids", e);
                }
            }
        }
        return ret;
    }

    /**
     * During the initial caching of types, the mapping does not exists but is
     * necessary.
     *
     * @param _typeId id of the type the UUID is wanted for
     * @return id of the type
     * @throws CacheReloadException on error
     */
    public static UUID getUUID4Id(final long _typeId)
        throws CacheReloadException
    {
        UUID ret = null;
        final var cache = InfinispanCache.get().<Long, Type>getCache(Type.IDCACHE);
        if (cache.containsKey(_typeId)) {
            ret = cache.get(_typeId).getUUID();
        } else {
            Connection con = null;
            try {
                con = Context.getConnection();
                PreparedStatement stmt = null;
                try {
                    stmt = con.prepareStatement(Type.SQL_ID);
                    stmt.setObject(1, _typeId);
                    final ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        ret = UUID.fromString(rs.getString(2).trim());
                    }
                    rs.close();
                } finally {
                    if (stmt != null) {
                        stmt.close();
                    }
                }
                con.commit();
            } catch (final SQLException e) {
                throw new CacheReloadException("could not read child type ids", e);
            } catch (final EFapsException e) {
                throw new CacheReloadException("could not read child type ids", e);
            } finally {
                try {
                    if (con != null && !con.isClosed()) {
                        con.close();
                    }
                } catch (final SQLException e) {
                    throw new CacheReloadException("Cannot read a type for an attribute.", e);
                }
            }
        }
        return ret;
    }

    /**
     * Checks if is initialized.
     *
     * @return true, if is initialized
     */
    public static boolean isInitialized()
    {
        final var cache1 = InfinispanCache.get().<Long, Type>getCache(Type.IDCACHE);
        final var cache2 = InfinispanCache.get().<String, Type>getCache(Type.NAMECACHE);
        final var cache3 = InfinispanCache.get().<UUID, Type>getCache(Type.UUIDCACHE);
        return !cache1.isEmpty() || !cache2.isEmpty() || !cache3.isEmpty();
    }
}
