/*
 * Copyright 2005 The eFaps Team
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

package org.efaps.admin.user;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.efaps.db.Cache;
import org.efaps.db.CacheInterface;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;

/**
 * @author tmo
 * @version $Id$
 */
public class Role extends UserObject implements CacheInterface  {

  /**
   * Logging instance used in this class.
   */
  private static final Log LOG = LogFactory.getLog(Role.class);

  /**
   * This is the sql select statement to select all roles from the database.
   */
  private static final String SQL_SELECT  = "select "
                                                + "ID,"
                                                + "NAME "
                                              + "from V_USERROLE";

  /**
   * Stores all instances of class {@link Role}.
   *
   * @see #getCache
   */
  private static final Cache <Role > cache = new Cache < Role > ();

  /**
   * Create a new role instance. The method is used from the static method
   * {@link #readRoles} to read all roles from the database.
   *
   * @param _id
   */
  private Role(final long _id, final String _name)  {
    super(_id, _name);
  }

  /**
   * Returns the viewable name of the role. The method {@link #getName} is
   * used for the viewing name.
   *
   * @param _context context for this request
   * @see #getName
   */
  public String getViewableName(final Context _context)  {
    return getName();
  }

  /**
   * Checks, if the given person is assigned to this role.
   *
   * @param _person person to test
   * @return <i>true</i> if the person is assigned to this role, otherwise
   *         <i>false</i>
   * @see #persons
   * @see #getPersons
   */
  public boolean hasChildPerson(final Person _person) {
// TODO: child roles
    return _person.isAssigned(this);
  }

  /////////////////////////////////////////////////////////////////////////////

  /**
   * Initialise the cache of JAAS systems.
   *
   * @param _context  eFaps context for this request
   */
  public static void initialise(final Context _context) throws Exception  {
    ConnectionResource con = null;
    try  {
      con = _context.getConnectionResource();

      Statement stmt = null;
      try  {

        stmt = con.getConnection().createStatement();

        ResultSet rs = stmt.executeQuery(SQL_SELECT);
        while (rs.next())  {
          long id =             rs.getLong(1);
          String name =         rs.getString(2).trim();

          LOG.debug("read role '" + name + "' (id = " + id + ")");
          cache.add(new Role(id, name));
        }
        rs.close();

      } finally  {
        if (stmt != null)  {
          stmt.close();
        }
      }

      con.commit();

    } finally  {
      if ((con != null) && con.isOpened())  {
        con.abort();
      }
    }
  }

  /**
   * Returns for given parameter <i>_id</i> the instance of class {@link Role}.
   *
   * @param _id id to search in the cache
   * @return instance of class {@link Role}
   * @see #getCache
   * @todo rewrite to use context instance
   */
  public static Role get(final long _id)  {
    return cache.get(_id);
  }

  /**
   * Returns for given parameter <i>_name</i> the instance of class
   * {@link Role}.
   *
   * @param _name name to search in the cache
   * @return instance of class {@link Role}
   * @see #getCache
   * @todo rewrite to use context instance
   */
  public static Role get(final String _name)  {
    return cache.get(_name);
  }

  /**
   * Static getter method for the role {@link #cache}.
   *
   * @return value of static variable {@link #cache}
   * @see #cache
   */
  static public Cache < Role > getCache()  {
    return cache;
  }

}
