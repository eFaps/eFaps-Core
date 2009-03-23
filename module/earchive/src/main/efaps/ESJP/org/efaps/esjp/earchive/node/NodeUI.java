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

package org.efaps.esjp.earchive.node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.event.Parameter;
import org.efaps.admin.event.Return;
import org.efaps.admin.event.Parameter.ParameterValues;
import org.efaps.admin.event.Return.ReturnValues;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Checkin;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.SearchQuery;
import org.efaps.esjp.earchive.NamesInterface;
import org.efaps.esjp.earchive.repository.Repository;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author jmox
 * @version $Id$
 */
@EFapsUUID("d6cb382d-a8e8-4b35-9d28-6b344c53f676")
@EFapsRevision("$Rev$")
public class NodeUI implements NamesInterface {

  public Return getTableUI(final Parameter _parameter) throws EFapsException {
    final Return ret = new Return();
    final Instance instance = _parameter.getInstance();
    Node node = null;
    if ("eArchive_Repository".equals(instance.getType().getName())) {
      node = Node.getRootNodeFromDB(new Repository(instance));
    }
    final String parentInstanceKey;
    final long parentId;
    String revision = null;
    if (node == null) {
      parentId = instance.getId();
      final String keyTmp = instance.getKey();
      if (keyTmp.contains("-")) {
        final int pos = keyTmp.indexOf("-");
        revision = keyTmp.substring(pos);
        parentInstanceKey = keyTmp.substring(0, pos);
      } else {
        parentInstanceKey = keyTmp;
      }
    } else {
      parentId = node.getId();
      parentInstanceKey = node.getHistoryId() + "." + node.getCopyId();
    }
    final SearchQuery query = new SearchQuery();
    query.setQueryTypes("eArchive_Node2NodeView");
    query.setExpandChildTypes(true);
    query.addWhereExprEqValue("Parent", parentId);
    query.addSelect("NodeType");
    query.addSelect("Child");
    query.addSelect("HistoryId");
    query.addSelect("CopyId");
    query.execute();

    final List<List<Instance>> list = new ArrayList<List<Instance>>();
    while (query.next()) {
      final List<Instance> instances = new ArrayList<Instance>(1);
      final StringBuilder instanceKey = new StringBuilder()
        .append(parentInstanceKey).append("|")
        .append(query.get("HistoryId")).append(".").append(query.get("CopyId"))
        .append(revision != null ? revision : "");

      instances.add(Instance.get(Type.get((Long) query.get("NodeType")) ,
                                (Long) query.get("Child"),
                                instanceKey.toString()));
      list.add(instances);
    }

    ret.put(ReturnValues.VALUES, list);

    return ret;
  }


  public Return getRevisionTableUI(final Parameter _parameter)
      throws EFapsException {
    final Return ret = new Return();
    final Instance instance = _parameter.getInstance();
    final Node node;
    final String instanceKey;
    if ("eArchive_Repository".equals(instance.getType().getName())) {
      node = Node.getRootNodeFromDB(new Repository(instance));
      instanceKey = node.getHistoryId() + "." + node.getCopyId();
    } else {
      node = Node.getNodeFromDB(instance.getId(), instance.getKey());
      instanceKey = instance.getKey();
    }

    final SearchQuery query = new SearchQuery();
    query.setQueryTypes(TYPE_NODEABSTRACT);
    query.setExpandChildTypes(true);
    query.addWhereExprEqValue(TYPE_NODEABSTRACT_A_HISTORYID,
                              node.getHistoryId());
    query.addWhereExprEqValue(TYPE_NODEABSTRACT_A_COPYID, node.getCopyId());
    query.addSelect(TYPE_NODEABSTRACT_A_TYPE);
    query.addSelect(TYPE_NODEABSTRACT_A_REVISION);
    query.addSelect(TYPE_NODEABSTRACT_A_ID);
    query.execute();

    final List<List<Instance>> list = new ArrayList<List<Instance>>();
    while (query.next()) {
      final List<Instance> instances = new ArrayList<Instance>(1);
      final StringBuilder keyBldr = new StringBuilder()
        .append(instanceKey).append("-")
        .append(query.get(TYPE_NODEABSTRACT_A_REVISION));

       instances.add(Instance.get((Type) query.get(TYPE_NODEABSTRACT_A_TYPE),
                                  (Long) query.get(TYPE_NODEABSTRACT_A_ID),
                                  keyBldr.toString()));
      list.add(instances);
    }

    ret.put(ReturnValues.VALUES, list);

    return ret;
  }


  public Return createDirectory(final Parameter _parameter)
      throws EFapsException {
    final String name = _parameter.getParameterValue("name");
    final Instance instance = _parameter.getInstance();
    final Node parentNode;
    if ("eArchive_Repository".equals(instance.getType().getName())) {
      parentNode = Node.getRootNodeFromDB(new Repository(instance));
    } else {
      parentNode = Node.getNodeFromDB(instance.getId(), instance.getKey());
    }
    final Node newDir = Node.createNewNode(name, Node.TYPE_NODEDIRECTORY);
    newDir.connect2Parent(parentNode);
    return new Return();
  }

  public Return createFile(final Parameter _parameter)
      throws EFapsException {
    final String name = _parameter.getParameterValue("name");
    final Instance instance = _parameter.getInstance();
    final Node node;
    if ("eArchive_Repository".equals(instance.getType().getName())) {
      node = Node.getRootNodeFromDB(new Repository(instance));
    } else {
      node = Node.getNodeFromDB(instance.getId(), instance.getKey());
    }
    final Node newFile = Node.createNewNode(name, Node.TYPE_NODEFILE);
    newFile.connect2Parent(node);
    final Instance fileInstance = Instance.get(Type.get(TYPE_FILE),
                                               newFile.getFileId());
    final Context.FileParameter fileItem =
                Context.getThreadContext().getFileParameters().get("upload");

    final Checkin checkin = new Checkin(fileInstance);
    try {
      checkin.execute(fileItem.getName(), fileItem.getInputStream(),
          (int) fileItem.getSize());
    } catch (final IOException e) {
      throw new EFapsException(this.getClass(), "execute", e, _parameter);
    }

    return  new Return();
  }


  public Return rename(final Parameter _parameter)
    throws EFapsException {
    final String name = _parameter.getParameterValue("name");
    final Instance instance = _parameter.getInstance();

    final Node node = Node.getNodeFromDB(instance.getId(), instance.getKey());
    node.rename(name);
    return new Return();
  }


  public Return getInstance(final Parameter _parameter) throws EFapsException {
    final String instanceKey = (String) _parameter.get(ParameterValues.OTHERS);
    Instance instance = null;
    if (instanceKey != null) {
      if (instanceKey.indexOf("|") < 0 && instanceKey.indexOf("-") < 0) {
        instance = Instance.get(instanceKey);
      } else {
        final List<Node> nodes = Node.getNodeHirachy(instanceKey);
        final Node node = nodes.get(nodes.size() - 1);
        instance = Instance.get(node.getType(), node.getId(), instanceKey);
      }
    }
    final Return ret = new Return();
    ret.put(ReturnValues.VALUES, instance);
    return ret;
  }

  public Return removeNode(final Parameter _parameter) throws EFapsException {
    final Instance instance = _parameter.getInstance();
    final Node node;
    if ("eArchive_Repository".equals(instance.getType().getName())) {
      node = Node.getRootNodeFromDB(new Repository(instance));
    } else {
      node = Node.getNodeFromDB(instance.getId(), instance.getKey());
    }

    return  new Return();
  }

}
