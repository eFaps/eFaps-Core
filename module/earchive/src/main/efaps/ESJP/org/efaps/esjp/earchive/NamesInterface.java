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

package org.efaps.esjp.earchive;

import org.efaps.admin.program.esjp.EFapsRevision;
import org.efaps.admin.program.esjp.EFapsUUID;

/**
 * TODO comment!
 *
 * @author jmox
 * @version $Id$
 */
@EFapsUUID("434879b4-7b41-4776-bfc5-57f91e92c460")
@EFapsRevision("$Rev$")
public interface NamesInterface {
  String SEPERATOR_IDS = ".";
  String SEPERATOR_IDS_RE = "\\.";
  String SEPERATOR_INSTANCE = ":";
  String SEPERATOR_INSTANCE_RE = SEPERATOR_INSTANCE;
  String SEPERATOR_REVISION = "-";
  String SEPERATOR_REVISION_RE = SEPERATOR_REVISION;


  String TABLE_FILE ="T_EAFILE";
  String TABLE_FILE_C_ID ="ID";
  String TABLE_FILE_T_C_ID = TABLE_FILE + "." + TABLE_FILE_C_ID;
  String TABLE_FILE_C_TYPEID ="TYPEID";
  String TABLE_FILE_C_FILELENGTH ="FILELENGTH";
  String TABLE_FILE_C_FILENAME ="FILENAME";
  String TABLE_FILE_C_MD5FILE ="MD5FILE";
  String TABLE_FILE_C_MD5DELTA ="MD5DELTA";

  String TABLE_NODE ="T_EANODE";
  String TABLE_NODE_C_ID = "ID";
  String TABLE_NODE_T_C_ID = TABLE_NODE + "." + TABLE_NODE_C_ID;
  String TABLE_NODE_C_TYPEID ="TYPEID";
  String TABLE_NODE_C_HISTORYID ="HISTORYID";
  String TABLE_NODE_C_COPYID ="COPYID";
  String TABLE_NODE_C_REVISION ="REVISION";
  String TABLE_NODE_T_C_REVISION = TABLE_NODE + "." + TABLE_NODE_C_REVISION;
  String TABLE_NODE_C_FILEID ="FILEID";
  String TABLE_NODE_C_NAME ="NAME";

  String TABLE_REVISION ="T_EAREVISION";
  String TABLE_REVISION_C_REPOSITORYID ="REPOSITORYID";
  String TABLE_REVISION_T_C_REPOSITORYID = TABLE_REVISION + "." + TABLE_REVISION_C_REPOSITORYID;
  String TABLE_REVISION_C_REVISION ="REVISION";
  String TABLE_REVISION_T_C_REVISION = TABLE_REVISION + "." + TABLE_REVISION_C_REVISION;
  String TABLE_REVISION_C_NODEID ="NODEID";

  String SEQ_NODE_HISTORYID = "T_EANODE_HISTORYID_SEQ";


  String TYPE_FILE = "eArchive_File";

  String TYPE_NODEABSTRACT = "eArchive_NodeAbstract";
  String TYPE_NODEABSTRACT_A_TYPE = "Type";
  String TYPE_NODEABSTRACT_A_ID = "ID";
  String TYPE_NODEABSTRACT_A_HISTORYID = "HistoryId";
  String TYPE_NODEABSTRACT_A_COPYID = "CopyId";
  String TYPE_NODEABSTRACT_A_REVISION = "Revision";
  String TYPE_NODEABSTRACT_A_NAME = "Name";

  String TYPE_NODEABSTRACTREV = "eArchive_NodeAbstractRev";
  String TYPE_NODEABSTRACTREV_A_TYPE = "Type";
  String TYPE_NODEABSTRACTREV_A_ID = "ID";
  String TYPE_NODEABSTRACTREV_A_HISTORYID = "HistoryId";
  String TYPE_NODEABSTRACTREV_A_COPYID = "CopyId";
  String TYPE_NODEABSTRACTREV_A_REVISION = "Revision";
  String TYPE_NODEABSTRACTREV_A_NAME = "Name";

  String TYPE_NODEDIRECTORY = "eArchive_NodeDirectory";
  String TYPE_NODEDIRECTORYREV = "eArchive_NodeDirectoryRev";
  String TYPE_NODEFILE = "eArchive_NodeFile";
  String TYPE_NODEFILEREV = "eArchive_NodeFileRev";

}
