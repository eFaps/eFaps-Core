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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.efaps.admin.datamodel.Type;
import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;
import org.efaps.db.Context;
import org.efaps.db.transaction.ConnectionResource;
import org.efaps.esjp.earchive.NamesInterface;
import org.efaps.esjp.earchive.repository.Repository;
import org.efaps.esjp.earchive.revision.Revision;
import org.efaps.util.EFapsException;

/**
 * TODO comment!
 *
 * @author jmox
 * @version $Id$
 */
@EFapsUUID("4108effb-988f-42c0-a143-9b15ae57d4d9")
@EFapsRevision("$Rev$")
public class Node implements NamesInterface{

  private final Long id;

  private final Long historyId;

  private final Long copyId;

  private final Long revision;

  /**
   * Getter method for instance variable {@link #revision}.
   *
   * @return value of instance variable {@link #revision}
   */
  public Long getRevision() {
    return this.revision;
  }


  private Type type;

  private String name;

  private Long repositoryId;

  private Node ancestor;

  private Long fileId;

  private String idPath;

  private Node parent;

  /**
   * Getter method for instance variable {@link #parent}.
   *
   * @return value of instance variable {@link #parent}
   */
  public Node getParent() {
    return this.parent;
  }


  /**
   * Setter method for instance variable {@link #parent}.
   *
   * @param parent value for instance variable {@link #parent}
   */
  public void setParent(final Node parent) {
    this.parent = parent;
  }


  /**
   * Getter method for instance variable {@link #children}.
   *
   * @return value of instance variable {@link #children}
   */
  public List<Node> getChildren() {
    return this.children;
  }


  private final List<Node> children = new ArrayList<Node>();

  public Node() {
    this.id = null;
    this.historyId = null;
    this.copyId = null;
    this.revision = null;
  }


  /**
   * @param _id
   * @param i
   * @param j
   * @param k
   * @param _name
   */
  private Node(final Long _id, final Type _type, final Long _historyId,
              final Long _copyId, final Long _revision, final String _name,
              final Long _repositoryId, final Long _fileId) {
    this.id = _id;
    this.type = _type;
    this.historyId = _historyId != null ? _historyId : new Long(0);
    this.copyId = _copyId != null ? _copyId : new Long(0);
    this.revision = _revision != null ? _revision : new Long(0);
    this.name = _name;
    this.repositoryId = _repositoryId;
    this.fileId = _fileId;
  }


  /**
   * Getter method for instance variable {@link #fileId}.
   *
   * @return value of instance variable {@link #fileId}
   */
  public Long getFileId() {
    return this.fileId;
  }


  /**
   * Getter method for instance variable {@link #type}.
   *
   * @return value of instance variable {@link #type}
   */
  public Type getType() {
    return this.type;
  }


  /**
   * Getter method for instance variable {@link #id}.
   *
   * @return value of instance variable {@link #id}
   */
  public Long getId() {
    return this.id;
  }


  /**
   * Getter method for instance variable {@link #idPath}.
   *
   * @return value of instance variable {@link #idPath}
   */
  public String getIdPath() {
    return this.idPath;
  }


  /**
   * Setter method for instance variable {@link #idPath}.
   *
   * @param _idPath value for instance variable {@link #idPath}
   */
  public void setIdPath(final String _idPath) {
    this.idPath = _idPath;
  }


  /**
   * Getter method for instance variable {@link #ancestor}.
   *
   * @return value of instance variable {@link #ancestor}
   */
  public Node getAncestor() {
    return this.ancestor;
  }


  /**
   * Getter method for instance variable {@link #repositoryId}.
   *
   * @return value of instance variable {@link #repositoryId}
   */
  public Long getRepositoryId() {
    return this.repositoryId;
  }


  /**
   * Getter method for instance variable {@link #historyId}.
   *
   * @return value of instance variable {@link #historyId}
   */
  public Long getHistoryId() {
    return this.historyId;
  }


  /**
   * Getter method for instance variable {@link #copyId}.
   *
   * @return value of instance variable {@link #copyId}
   */
  public Long getCopyId() {
    return this.copyId;
  }


  public static Node createNewNode(final String _name, final String _type)
      throws EFapsException {

    Long fileIdTmp = null;
    if (_type.equals(TYPE_NODEFILE)) {
     fileIdTmp =  createFile();
    }

    final StringBuilder cmd = new StringBuilder();

    final Long typeId = Type.get(_type).getId();

    cmd.append("insert into ").append(TABLE_NODE)
      .append("(id, typeid, historyid, copyid, revision, name");

    if (fileIdTmp != null) {
      cmd.append(",").append(TABLE_NODE_C_FILEID);
    }
    cmd.append(")")
      .append(" values (?,?,?,?,?,?").append(fileIdTmp != null ? ",?)" : ")");

    Long id = null;
    Long histId = null;

    final Context context = Context.getThreadContext();
    ConnectionResource con = null;

    try {
      con = context.getConnectionResource();
      id = Context.getDbType().getNewId(con.getConnection(), TABLE_NODE, "ID");
      histId = Context.getDbType().getNewId(con.getConnection(), TABLE_NODE,
             "historyid");

      PreparedStatement stmt = null;
      try {
        stmt = con.getConnection().prepareStatement(cmd.toString());
        stmt.setLong(1, id);
        stmt.setLong(2, typeId);
        stmt.setLong(3, histId);
        stmt.setInt(4, 0);
        stmt.setInt(5, 0);
        stmt.setString(6, _name);
        if (fileIdTmp != null) {
          stmt.setLong(7, fileIdTmp);
        }
        final int rows = stmt.executeUpdate();
        if (rows == 0) {
//           TODO fehler schmeissen
        }
      } finally {
        stmt.close();
      }
      con.commit();
  } catch (final SQLException e) {
//    TODO fehler schmeissen
  } finally {
    if ((con != null) && con.isOpened()) {
      con.abort();
    }
  }
   return new Node(id, Type.get(typeId), histId, null, null, _name, null, fileIdTmp);
  }

  private static long createFile()
      throws EFapsException {
    long ret = 0;
    final StringBuilder cmd = new StringBuilder();
    cmd.append("insert into ").append(TABLE_FILE)
      .append("(")
      .append(TABLE_FILE_C_ID).append(",")
      .append(TABLE_FILE_C_TYPEID).append(",")
      .append(TABLE_FILE_C_FILELENGTH).append(",")
      .append(TABLE_FILE_C_FILENAME).append(",")
      .append(TABLE_FILE_C_MD5FILE).append(",")
      .append(TABLE_FILE_C_MD5DELTA).append(")")
      .append(" values (?,?,?,?,?,?)");
    final Context context = Context.getThreadContext();
    ConnectionResource con = null;
    System.out.println(cmd);
    try {
      con = context.getConnectionResource();
      ret = Context.getDbType().getNewId(con.getConnection(), TABLE_FILE, "ID");
      final long typeid = Type.get(TYPE_FILE).getId();

      PreparedStatement stmt = null;
      try {
        stmt = con.getConnection().prepareStatement(cmd.toString());
        stmt.setLong(1, ret);
        stmt.setLong(2, typeid);
        stmt.setLong(3, new Long(0));
        stmt.setString(4, "empty");
        stmt.setString(5, "empty");
        stmt.setString(6, "empty");

        final int rows = stmt.executeUpdate();
        if (rows == 0) {
  //         TODO fehler schmeissen
        }
      } finally {
        stmt.close();
      }
      con.commit();
    } catch (final SQLException e) {
    //  TODO fehler schmeissen
      e.printStackTrace();
    } finally {
      if ((con != null) && con.isOpened()) {
        con.abort();
      }
    }
    return ret;
  }

  public static List<Node> getNodeHirachy(final String _instanceKey)
      throws EFapsException {
    final List<Node> ret = new ArrayList<Node>();
    String revision = null;
    final String instanceKey;
    if (_instanceKey.contains(SEPERATOR_REVISION)) {
      final int pos = _instanceKey.indexOf(SEPERATOR_REVISION);
      revision = _instanceKey.substring(pos + 1);
      instanceKey = _instanceKey.substring(0, pos);
    } else {
      instanceKey = _instanceKey;
    }
    final String[] pairs;
    if (instanceKey.contains(SEPERATOR_INSTANCE)) {
      pairs = instanceKey.split(SEPERATOR_INSTANCE_RE);
    } else {
      pairs = new String[]{instanceKey};
    }

    final StringBuilder cmd = new StringBuilder();
    cmd.append("select")
      .append(" t_eanode.id").append(",")
      .append(" t_eanode.typeid").append(",")
      .append(" t_eanode.historyid").append(",")
      .append(" t_eanode.copyid").append(",")
      .append(" t_earevision.revision").append(",")
      .append(" t_eanode.name").append(",")
      .append(" t_earevision.repositoryid");
   if (revision == null) {
      cmd.append(" from").append(" t_earepository")
         .append(" join").append(" t_earevision").append(" on ")
        .append(" t_earevision.repositoryid")
        .append("=").append(" t_earepository.id")
      .append(" and").append(" t_earevision.revision").append("=")
      .append(" t_earepository.lastrevision");
   } else {
     cmd.append(" from ").append(TABLE_REVISION);
   }
     cmd.append(" join ").append(TABLE_NODE)
      .append(" on ").append(TABLE_NODE_T_C_ID)
      .append("=").append(" t_earevision.nodeid")
      .append(" where ").append(" t_eanode.historyid").append("=?")
      .append(" and ").append(" t_eanode.copyid").append("=? ");
    if (revision != null) {
      cmd.append(" and ").append(TABLE_NODE_T_C_REVISION).append("=?");
    }
    final ConnectionResource con
                          = Context.getThreadContext().getConnectionResource();
    try {

      PreparedStatement stmt = null;
      try {
        stmt = con.getConnection().prepareStatement(cmd.toString());
        final String[] pair = pairs[0].split(SEPERATOR_IDS_RE);
        stmt.setLong(1, Long.parseLong(pair[0]));
        stmt.setLong(2, Long.parseLong(pair[1]));
        if (revision != null) {
          stmt.setLong(3, Long.parseLong(revision));
        }
        final ResultSet resultset = stmt.executeQuery();
        Node current = null;
        if (resultset.next()) {
          final Node root = new Node(resultset.getLong(1),
                               Type.get(resultset.getLong(2)),
                               resultset.getLong(3),
                               resultset.getLong(4), resultset.getLong(5),
                               resultset.getString(6), resultset.getLong(7),
                               null);
          ret.add(root);
          current = root;
        }
        resultset.close();

        for (int i = 1; i < pairs.length; i++) {
          final String[] childPair = pairs[i].split(SEPERATOR_IDS_RE);
          final Node child = getChildNode(current, Long.parseLong(childPair[0]),
                                          Long.parseLong(childPair[1]));
          ret.add(child);
          current = child;
        }

      } finally {
        stmt.close();
      }
      con.commit();
     } catch (final SQLException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     } finally {
       if ((con != null) && con.isOpened()) {
         con.abort();
       }
     }
    return ret;
  }

  private static Node getChildNode(final Node _parent, final long _historyId,
                            final long _copyId) throws EFapsException {
    Node ret = null;
    final StringBuilder cmd = new StringBuilder();
    cmd.append("select")
      .append(" t_eanode2node.childid").append(",")
      .append(" t_eanode.typeid").append(",")
      .append(" t_eanode.historyid").append(",")
      .append(" t_eanode.copyid").append(",")
      .append(" t_eanode.revision").append(",")
      .append(" t_eanode.name")
      .append(" from").append(" t_eanode2node")
      .append(" join ").append(TABLE_NODE).append(" on").append(" t_eanode.id")
      .append("=").append(" t_eanode2node.childid")
      .append(" where ").append(" t_eanode.historyid").append("=?")
      .append(" and").append(" t_eanode.copyid").append("=? ")
      .append(" and").append(" t_eanode2node.parentid").append("=? ");

    final ConnectionResource con
                          = Context.getThreadContext().getConnectionResource();
    try {

      PreparedStatement stmt = null;
      try {
        stmt = con.getConnection().prepareStatement(cmd.toString());

        stmt.setLong(1, _historyId);
        stmt.setLong(2, _copyId);
        stmt.setLong(3, _parent.getId());
        final ResultSet resultset = stmt.executeQuery();

        if (resultset.next()) {
          ret = new Node(resultset.getLong(1), Type.get(resultset.getLong(2)),
              resultset.getLong(3), resultset.getLong(4), resultset.getLong(5),
              resultset.getString(6), null, null);
        }
        resultset.close();
      } finally {
        stmt.close();
      }
      con.commit();
     } catch (final SQLException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     } finally {
       if ((con != null) && con.isOpened()) {
         con.abort();
       }
     }
    return ret;
  }


  public static Node getNodeFromDB(final Long _nodeId, final String _idPath)
      throws EFapsException {
    final StringBuilder cmd = new StringBuilder();
    cmd.append(" select ")
      .append(TABLE_NODE_T_C_ID).append(",")
      .append(TABLE_NODE_C_TYPEID).append(",")
      .append(TABLE_NODE_C_HISTORYID).append(",")
      .append(TABLE_NODE_C_COPYID).append(",")
      .append(TABLE_NODE_T_C_REVISION).append(",")
      .append(TABLE_NODE_C_NAME).append(",")
      .append(TABLE_REVISION_T_C_REPOSITORYID).append(",")
      .append(TABLE_NODE_C_FILEID)
      .append(" from ").append(TABLE_NODE)
      .append(" left join ").append(TABLE_REVISION).append(" on ")
        .append(TABLE_REVISION).append(".").append(TABLE_REVISION_C_NODEID)
        .append(" = ").append(TABLE_NODE).append(".").append(TABLE_NODE_C_ID)
      .append(" where ").append(TABLE_NODE).append(".").append(TABLE_NODE_C_ID)
      .append(" = ?");

    final ConnectionResource con
                          = Context.getThreadContext().getConnectionResource();
    Node ret = null;
    try {

     PreparedStatement stmt = null;
     try {
       stmt = con.getConnection().prepareStatement(cmd.toString());

       stmt.setLong(1, _nodeId);
       final ResultSet resultset = stmt.executeQuery();

       if (resultset.next()) {
         ret = new Node(resultset.getLong(1), Type.get(resultset.getLong(2)),
                        resultset.getLong(3), resultset.getLong(4),
                        resultset.getLong(5), resultset.getString(6),
                        resultset.getLong(7), resultset.getLong(8));
         ret.setIdPath(_idPath);
       }
       resultset.close();
     } finally {
       stmt.close();
     }
     con.commit();
    } catch (final SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      if ((con != null) && con.isOpened()) {
        con.abort();
      }
    }
    return ret;
  }


  public static Node getRootNodeFromDB(final Repository _repository) throws EFapsException {

    final StringBuilder cmd = new StringBuilder();
    cmd.append("  SELECT t_eanode.id,t_eanode.typeid, t_eanode.historyid,  copyid ,t_earevision.revision,t_eanode.name, repositoryid from ")
      .append("t_earevision")
      .append(" join t_earepository on t_earevision.repositoryid = t_earepository.id and t_earevision.revision = t_earepository.lastrevision")
      .append(" join t_eanode on t_eanode.id = t_earevision.nodeid")
      .append(" where repositoryid = ?");
    final ConnectionResource con = Context.getThreadContext().getConnectionResource();
    Node ret = null;
    try {

     PreparedStatement stmt = null;
     try {
       stmt = con.getConnection().prepareStatement(cmd.toString());

       stmt.setLong(1, _repository.getId());
       final ResultSet resultset = stmt.executeQuery();

       if (resultset.next()) {
         final Long historyIdTmp = resultset.getLong(3);
         final Long copyIdTmp = resultset.getLong(4);
         ret = new Node(resultset.getLong(1), Type.get(resultset.getLong(2)),
             historyIdTmp, copyIdTmp , resultset.getLong(5),
             resultset.getString(6), resultset.getLong(7), null);
         ret.setIdPath(historyIdTmp + "." + copyIdTmp);
       }
       resultset.close();
     } finally {
       stmt.close();
     }
     con.commit();
    } catch (final SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      if ((con != null) && con.isOpened()) {
        con.abort();
      }
    }
    return ret;

  }

  /**
   * @param nodeid
   * @return
   * @throws EFapsException
   */
  public List<Node> connect2Parent(final Node _parentNode)
      throws EFapsException {
    final List<Node> nodes = getNodeHirachy(_parentNode.getIdPath());
    return bubbleUp(nodes);
  }

  private List<Node> bubbleUp(final List<Node> _nodes) throws EFapsException{
    final List<Node> ret = new ArrayList<Node>();
    Collections.reverse(_nodes);
    final Iterator<Node> iter = _nodes.iterator();
    Node current = this;
    while (iter.hasNext()) {
      final Node node = iter.next();
      final Node reviseNode = node.getNodeClone();
      ret.add(reviseNode);
      final List<Node> list = new ArrayList<Node>();
      if (current.getAncestor() != null) {
        list.add(current.getAncestor());
      }
      final List<Node> children = node.getChildNodes(list, null);
      children.add(current);
      Node2Node.connect(reviseNode, children);
      if (reviseNode.isRoot()) {
        Revision.getNewRevision(new Repository(reviseNode.getRepositoryId()),
                                               reviseNode);
      } else {
        current = reviseNode;
      }
    }
    return ret;
  }
  /**
   * @return
   * @throws EFapsException
   *
   */
  private List<Node> getChildNodes(final List<Node> _excludeNodes,
                                   final Long _revision)
      throws EFapsException {

    final StringBuilder cmd = new StringBuilder();
    cmd.append(" SELECT id, nodetype, childid, historyid, copyid, revision, name  from ")
      .append(" v_eanode2node")
      .append(" where parentid = ?");
    if (_revision != null) {
      cmd.append(" and revision = ?");
    }
    final ConnectionResource con
                          = Context.getThreadContext().getConnectionResource();
    final List<Node> ret = new ArrayList<Node>();
    try {

     PreparedStatement stmt = null;
     try {
       stmt = con.getConnection().prepareStatement(cmd.toString());

       stmt.setLong(1, this.id);
       if (_revision != null) {
         stmt.setLong(2, _revision);
       }
       final ResultSet resultset = stmt.executeQuery();

       while (resultset.next()) {
         final Long childid = resultset.getLong(3);
         final Long historyIdTmp = resultset.getLong(4);
         final Long copyIdTmp = resultset.getLong(5);
         boolean add = true;
         if (_excludeNodes != null) {
           for (final Node excludeNode : _excludeNodes) {
             if (excludeNode.getId() != null) {
               if (childid == excludeNode.getId()) {
                 add = false;
                 break;
               }
             } else {
               if (historyIdTmp == excludeNode.getHistoryId()
                   && copyIdTmp == excludeNode.getCopyId()) {
                 add = false;
                 break;
               }
             }
           }

         }
         if (add) {
           final Node child = new Node(childid, Type.get(resultset.getLong(2)),
                                       historyIdTmp, copyIdTmp,
                                       resultset.getLong(6),
                                       resultset.getString(7), null, null);
           child.setParent(this);
           ret.add(child);
         }
       }
       resultset.close();
     } finally {
       stmt.close();
     }
     con.commit();
    } catch (final SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      if ((con != null) && con.isOpened()) {
        con.abort();
      }
    }
    return ret;

  }


  public Node getNodeClone() throws EFapsException {
    final StringBuilder cmd = new StringBuilder();

    cmd.append("insert into ").append(TABLE_NODE)
      .append("(id, typeid, historyid, copyid, revision, name");
    if (this.fileId != null) {
      cmd.append(",").append(TABLE_NODE_C_FILEID);
    }
    cmd.append(")")
      .append(" values (?,?,?,?,?,?").append(this.fileId == null ? ")" : ",?)");

    Long idTmp = null;

    final Context context = Context.getThreadContext();
    ConnectionResource con = null;

    try {
      con = context.getConnectionResource();
      idTmp = Context.getDbType().getNewId(con.getConnection(),
                                           TABLE_NODE,
                                           "ID");

      PreparedStatement stmt = null;
      try {
        stmt = con.getConnection().prepareStatement(cmd.toString());
        stmt.setLong(1, idTmp);
        stmt.setLong(2, this.type.getId());
        stmt.setLong(3, this.historyId);
        stmt.setLong(4, this.copyId);
        stmt.setInt(5, 0);
        stmt.setString(6, this.name);
        if (this.fileId != null) {
          stmt.setLong(7, this.fileId);
        }
        final int rows = stmt.executeUpdate();
        if (rows == 0) {
//           TODO fehler schmeissen
        }
      } finally {
        stmt.close();
      }
    con.commit();
  } catch (final SQLException e) {
//    TODO fehler schmeissen
    e.printStackTrace();
  } finally {
    if ((con != null) && con.isOpened()) {
      con.abort();
    }
  }

  final Node ret = new Node(idTmp, this.type, this.historyId, this.copyId, null,
                            this.name, this.repositoryId, this.fileId);
  ret.setAncestor(this);
  ret.setIdPath(this.idPath);
  return ret;
  }


  /**
   * @param ret
   */
  private void setAncestor(final Node _ancestor) {
    this.ancestor = _ancestor;
  }


  public boolean isRoot() {
    return this.repositoryId != null && this.repositoryId > 0;
  }


  /**
   * @param _name
   * @return
   * @throws EFapsException
   */
  public List<Node> rename(final String _name) throws EFapsException {
    final List<Node> ret = new ArrayList<Node>();
    this.name = _name;
    //make a clone
    final Node clone = getNodeClone();
    ret.add(clone);
    //connect existing children to clone
    final List<Node> childrenTmp = getChildNodes(null, null);
    Node2Node.connect(clone, childrenTmp);
    final List<Node> nodes = getNodeHirachy(this.idPath);
    // remove last node from the hirachy because it is the node that was renamed
    nodes.remove(nodes.size() - 1);
    clone.bubbleUp(nodes);
    return ret;
  }


  public List<Node> deleteChildren(final String[] _instanceKeys) throws EFapsException{
    final List<Node> ret = new ArrayList<Node>();
    final List<Node> remove = new ArrayList<Node>();
    for (final String key : _instanceKeys) {
      System.out.println(key);
      final String inst = key.substring(key.lastIndexOf(SEPERATOR_INSTANCE) + 1);
      System.out.println(inst);
      final String[] ids = inst.split(SEPERATOR_IDS_RE);
      remove.add(new Node(null, null, Long.parseLong(ids[0]),
                          Long.parseLong(ids[1]), null, null, null, null));
    }
    final List<Node> childrenTmp = getChildNodes(remove, null);
    final Node clone = getNodeClone();
    Node2Node.connect(clone, childrenTmp);
    final List<Node> nodes = getNodeHirachy(this.idPath);
    // remove last node from the hirachy because it is this node
    nodes.remove(nodes.size() - 1);
    clone.bubbleUp(nodes);
    ret.add(clone);
    return ret;
  }


  public static void updateRevisions() throws EFapsException {
    final StringBuilder cmd = new StringBuilder();
    cmd.append("select ")
      .append(TABLE_NODE_T_C_ID).append(",")
      .append(TABLE_NODE_C_TYPEID).append(",")
      .append(TABLE_NODE_C_HISTORYID).append(",")
      .append(TABLE_NODE_C_COPYID).append(",")
      .append(TABLE_REVISION_T_C_REVISION).append(",")
      .append(TABLE_NODE_C_NAME).append(",")
      .append(TABLE_REVISION_C_REPOSITORYID)
      .append(" from ").append(TABLE_NODE)
      .append(" join ").append(TABLE_REVISION).append(" on ")
        .append(TABLE_NODE_T_C_ID).append(" = ")
        .append(TABLE_REVISION_C_NODEID)
      .append(" order by ").append(TABLE_REVISION_T_C_REVISION);

    final ConnectionResource con
                          = Context.getThreadContext().getConnectionResource();
    final List<Node> nodes = new ArrayList<Node>();
    try {

     Statement stmt = null;
     try {
       stmt = con.getConnection().createStatement();
       final ResultSet resultset = stmt.executeQuery(cmd.toString());

       while (resultset.next()) {
        nodes.add(new Node(resultset.getLong(1), Type.get(resultset.getLong(2)),
                        resultset.getLong(3), resultset.getLong(4),
                        resultset.getLong(5), resultset.getString(6),
                        resultset.getLong(7), null));

       }
       resultset.close();
     } finally {
       stmt.close();
     }
     con.commit();
    } catch (final SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      if ((con != null) && con.isOpened()) {
        con.abort();
      }
    }

    for (final Node node : nodes) {
      node.updateRevision(node.getRevision());
    }
  }


  private void updateRevision(final Long _revision) throws EFapsException {
    this.children.addAll(getChildNodes(null, new Long (0)));
    for (final Node child : this.children) {
      child.updateRevision(_revision);
    }
    final StringBuilder cmd = new StringBuilder();
    cmd.append("update ")
      .append(TABLE_NODE)
      .append(" set ").append(TABLE_NODE_C_REVISION).append("=?")
      .append(" where ").append(TABLE_NODE_C_ID).append("=?");

    final ConnectionResource con
                = Context.getThreadContext().getConnectionResource();
    try {

      PreparedStatement stmt = null;
      try {
        con.getConnection().setAutoCommit(true);
        stmt = con.getConnection().prepareStatement(cmd.toString());
        stmt.setLong(1, _revision);
        stmt.setLong(2, this.id);
        final int rows = stmt.executeUpdate();
        if (rows == 0) {
//           TODO fehler schmeissen
        }
      } finally {
        stmt.close();
      }
      con.commit();
     } catch (final SQLException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
     } finally {
       if ((con != null) && con.isOpened()) {
         con.abort();
       }
     }
  }
}
