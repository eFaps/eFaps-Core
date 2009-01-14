/*
 * Copyright 2003 - 2009 The eFaps Team
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

package org.efaps.esjp.teamwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.efaps.admin.event.EventExecution;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Insert;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.util.EFapsException;

/**
 * TODO description!
 *
 * @author jmox
 * @version $Id$
 */
@EFapsUUID("5725c5b0-f20b-4ce4-86f8-c0a277fe5715")
@EFapsRevision("$Rev$")
public class Abstract2Abstract implements EventExecution {

  /**
   * Logger for this class
   */
  private static final Logger LOG =
      LoggerFactory.getLogger(Abstract2Abstract.class);

  public Return execute(final Parameter _parameter) {
    final Instance instance = (Instance) _parameter.get(ParameterValues.INSTANCE);
    final String abstractlink = ((Long) instance.getId()).toString();

    SearchQuery query = new SearchQuery();
    String parent = null;

    try {
      query.setObject(instance.getOid());
      query.addSelect("ParentCollectionLink");
      query.executeWithoutAccessCheck();
      if (query.next()) {

        parent = query.get("ParentCollectionLink").toString();
      }
      query.close();

      query = new SearchQuery();
      query.setQueryTypes("TeamWork_Abstract2Abstract");
      query.addWhereExprEqValue("AbstractLink", parent);
      query.addSelect("AncestorLink");
      query.addSelect("Rank");
      query.executeWithoutAccessCheck();

      while (query.next()) {

        insertDB(abstractlink, query.get("AncestorLink").toString(), query.get(
            "Rank").toString());

      }
      query.close();

      query = new SearchQuery();
      query.setQueryTypes("TeamWork_Abstract2Abstract");
      query.addWhereExprEqValue("AbstractLink", parent);
      query.addWhereExprEqValue("AncestorLink", parent);
      query.addSelect("Rank");
      query.executeWithoutAccessCheck();
      if (query.next()) {
        final Long rank = ((Long) query.get("Rank")) + 1;

        insertDB(abstractlink, abstractlink, rank.toString());

      }
    } catch (final EFapsException e) {

      LOG.error("execute(Context, Instance, Map<TriggerKeys4Values,Map>)", e);
    }
    return null;
  }

  public Return insertNewRoot(final Parameter _parameter) {
    final Instance instance = (Instance) _parameter.get(ParameterValues.INSTANCE);
    final String abstractlink = ((Long) instance.getId()).toString();

    insertDB(abstractlink, abstractlink, "1");
    return null;
  }

  private void insertDB(final String _abstractlink, final String _ancestorlink,
                        final String _rank) {

    try {
      final Insert insert = new Insert("TeamWork_Abstract2Abstract");
      insert.add("AbstractLink", _abstractlink);
      insert.add("AncestorLink", _ancestorlink);
      insert.add("Rank", _rank);
      insert.execute();

    } catch (final EFapsException e) {

      LOG.error("insertDB(String, String, String)", e);
    }

  }

}
