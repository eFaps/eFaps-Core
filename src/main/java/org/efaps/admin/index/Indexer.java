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
package org.efaps.admin.index;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.efaps.admin.dbproperty.DBProperties;
import org.efaps.admin.index.IndexDefinition.IndexField;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Indexer.
 *
 * @author The eFaps Team
 */
public final class Indexer
{
    /**
     * The Enum Key.
     *
     * @author The eFaps Team
     */
    public enum Key
    {

        /** The oid. */
        OID,

        /** The all. */
        ALL,

        /** The msgphrase. */
        MSGPHRASE;

    }

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Indexer.class);

    /**
     * Instantiates a new indexer.
     */
    private Indexer()
    {

    }

    /**
     * Analyze.
     */
    public static void analyze()
    {
        try {
            final List<IndexDefinition> defs = IndexDefinition.get();

            final IndexWriterConfig config = new IndexWriterConfig(Index.getAnalyzer());
            final IndexWriter writer = new IndexWriter(Index.getDirectory(), config);

            for (final IndexDefinition def : defs) {
                final QueryBuilder queryBldr = new QueryBuilder(def.getUUID());
                final MultiPrintQuery multi = queryBldr.getPrint();
                for (final IndexField field : def.getFields()) {
                    multi.addSelect(field.getSelect());
                }
                multi.addMsgPhrase(def.getMsgPhrase());
                multi.executeWithoutAccessCheck();
                while (multi.next()) {
                    final String oid = multi.getCurrentInstance().getOid();
                    final String type = multi.getCurrentInstance().getType().getLabel();
                    final Document doc = new Document();
                    doc.add(new StringField(Key.OID.name(), oid, Store.YES));
                    doc.add(new StringField("type", type, Store.YES));

                    final StringBuilder allBldr = new StringBuilder()
                                .append(type).append(" ");

                    for (final IndexField field : def.getFields()) {
                        final String name = DBProperties.getProperty(field.getKey());
                        final Object value = multi.getSelect(field.getSelect());
                        switch (field.getFieldType()) {
                            case STRING:
                                doc.add(new StringField(name, String.valueOf(value), Store.YES));
                                allBldr.append(value).append(" ");
                                break;
                            case SEARCHSTRING:
                                doc.add(new StringField(name, String.valueOf(value), Store.NO));
                                allBldr.append(value).append(" ");
                                break;
                            case TEXT:
                                doc.add(new TextField(name, String.valueOf(value), Store.YES));
                                allBldr.append(value).append(" ");
                                break;
                            case SEARCHTEXT:
                                doc.add(new TextField(name, String.valueOf(value), Store.NO));
                                allBldr.append(value).append(" ");
                                break;
                            case STORED:
                                doc.add(new StoredField(name, String.valueOf(value)));
                                allBldr.append(value).append(" ");
                                break;
                            default:
                                break;
                        }
                    }
                    doc.add(new StoredField(Key.MSGPHRASE.name(), multi.getMsgPhrase(def.getMsgPhrase())));
                    doc.add(new TextField(Key.ALL.name(), allBldr.toString(), Store.NO));
                    //writer.addDocument(doc);
                    writer.updateDocument(new Term("oid", oid), doc);
                    LOG.debug("Add Document: {}", doc);
                }
            }
            writer.close();
        } catch (final IOException | EFapsException e) {
            LOG.error("Catched Exception", e);
        }
    }
}
