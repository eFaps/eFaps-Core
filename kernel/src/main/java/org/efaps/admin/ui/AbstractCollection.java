/*
 * Copyright 2003-2007 The eFaps Team
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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.admin.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.ui.field.Field;
import org.efaps.admin.ui.field.FieldGroup;
import org.efaps.admin.ui.field.FieldHeading;
import org.efaps.admin.ui.field.FieldTable;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.CacheReloadException;

/**
 * @author tmo
 * @version $Id$
 * @todo description
 */
public abstract class AbstractCollection extends AbstractUserInterfaceObject {

  /**
   * Instance variable for all field expressions.
   *
   * @see #addFieldExpr
   * @see #getFieldExprIndex
   * @see #getAllFieldExpr
   */
  private final Map<String, Integer> allFieldExpr =
      new HashMap<String, Integer>();

  /**
   * All fields of the collection are stored sorted belonging to the id of the
   * field in a tree map.
   *
   * @see #getFields
   * @see #add(Field)
   */
  private final Map<Long, Field> fields = new TreeMap<Long, Field>();

  /**
   * Instance variable for the length of the field expression list.
   *
   * @see #allFieldExpr
   */
  private int selIndexLen = 1;

  /**
   * Select string for the statement.
   *
   * @see #setSelect
   * @see #getSelect
   */
  private String select = null;

  /**
   * Constructor passing on to the superconstructor
   *
   * @param _id
   * @param _uuid
   * @param _name
   */
  protected AbstractCollection(final long _id, final String _uuid,
                               final String _name) {
    super(_id, _uuid, _name);
  }

  /**
   * method to add a Field to this Collection
   *
   * @param _field
   */
  public void add(final Field _field) {
    this.fields.put(_field.getId(), _field);

    if (_field.getReference() != null && _field.getReference().length() > 0) {
      final String ref = _field.getReference();
      int index, end = 0;
      while ((index = ref.indexOf("$<", end)) > 0) {
        index += 2;
        end = ref.indexOf(">", index);
        addFieldExpr(ref.substring(index, end));
      }
    }

  }

  /**
   * Add a field expression to the select statement and the hash table of all
   * field expressions. The method returns the index of the field expression. If
   * the field expression is already added, the old index is returned, so a
   * expression is only added once.
   *
   * @param _expr
   *                field expression to add
   * @return index of the field expression
   * @see #getFieldExprIndex
   * @see #getAllFieldExpr
   * @see #allFieldExpr
   */
  protected int addFieldExpr(final String _expr) {
    int ret = -1;
    if (getAllFieldExpr().containsKey(_expr)) {
      ret = getFieldExprIndex(_expr);
    } else {
      getAllFieldExpr().put(_expr, Integer.valueOf(getSelIndexLen()));
      if (getSelect() == null) {
        setSelect(_expr);
      } else {
        setSelect(getSelect() + "," + _expr);
      }
      ret = getSelIndexLen();
      this.selIndexLen++;
    }
    return ret;
  }

  /**
   * For the parameter <i>_expr</i> the index in the list of all field
   * expressions is returned.
   *
   * @param _expr
   *                expression for which the index is searched
   * @return index of the field expression
   * @see #addFieldExpr
   * @see #getAllFieldExpr
   * @see #allFieldExpr
   */
  public int getFieldExprIndex(final String _expr) {
    int ret = -1;
    if (getAllFieldExpr().containsKey(_expr)) {
      final Integer ident = getAllFieldExpr().get(_expr);
      ret = ident.intValue();
    }
    return ret;
  }

  // ///////////////////////////////////////////////////////////////////////////
  /**
   * The instance method reads all needed information for this user interface
   * object.
   *
   * @see #readFromDB4Fields
   */
  @Override
  protected void readFromDB() throws CacheReloadException {
    super.readFromDB();
    readFromDB4Fields();
  }

  /**
   * Read all fields related to this collection object.
   */
  private void readFromDB4Fields() throws CacheReloadException {
    try {
      final Instance instance =
          new Instance(Type.get(EFapsClassName.COLLECTION.name), getId());
      final SearchQuery query = new SearchQuery();
      query.setExpand(instance, EFapsClassName.FIELD.name + "\\Collection");
      query.addSelect("Type");
      query.addSelect("ID");
      query.addSelect("Name");
      query.executeWithoutAccessCheck();

      while (query.next()) {
        final long id = (Long) query.get("ID");
        final String name = (String) query.get("Name");
        Field field;
        final Type type = (Type) query.get("Type");
        if (EFapsClassName.FIELDHEADING.name.equals(type.getName())) {
          field = new FieldHeading(id, null, name);
        } else if (EFapsClassName.FIELDTABLE.name.equals(type.getName())) {
          field = new FieldTable(id, null, name);
        } else if (EFapsClassName.FIELDGROUP.name.equals(type.getName())) {
          field = new FieldGroup(id, null, name);
        } else {
          field = new Field(id, null, name);
        }
        field.readFromDB();
        add(field);
      }
    } catch (EFapsException e) {
      throw new CacheReloadException("could not read fields for '"
          + getName()
          + "'", e);
    }
  }

  /**
   * The method takes values of the {@link #fields} and returnes them as
   * {@link java.util.ArrayList}.
   *
   * @return the values of the {@link #fields} map instance as array list
   * @see #fields
   */
  public List<Field> getFields() {
    return new ArrayList<Field>(this.fields.values());
  }

  // ///////////////////////////////////////////////////////////////////////////

  /**
   * @see #allFieldExpr
   * @return the hashtable which holds single field expression
   */
  private Map<String, Integer> getAllFieldExpr() {
    return this.allFieldExpr;
  }

  /**
   * Get the value of the attribute {@link #selIndexLen}.
   *
   * @return the value of the attribute {@link #selIndexLen}
   * @see #selIndexLen
   */
  private int getSelIndexLen() {
    return this.selIndexLen;
  }

  /**
   * This is the setter method for instance variable {@link #select}.
   *
   * @param _select
   *                new value for instance variable {@link #select}
   * @see #select
   * @see #getSelect
   */
  protected void setSelect(final String _select) {
    this.select = _select;
  }

  /**
   * Get the value of the {@link #select} clause.
   *
   * @return the value of the {@link #select} clause
   * @see #select
   * @see #setSelect
   */
  public String getSelect() {
    return this.select;
  }

  public Map<Long, Field> getFieldsMap() {
    return this.fields;
  }

}