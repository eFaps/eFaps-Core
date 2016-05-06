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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.efaps.admin.access.AccessTypeEnums;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.index.Indexer.Key;
import org.efaps.db.Instance;
import org.efaps.json.index.SearchResult;
import org.efaps.json.index.SearchResult.Element;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Search.
 *
 * @author The eFaps Team
 */
public final class Search
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Search.class);

    /** The types. */
    private final Map<Type, List<Instance>> typeMapping = new HashMap<>();

    /** The docs. */
    private final Map<Instance, Element> elements = new LinkedHashMap<>();

    /**
     * Instantiates a new search.
     */
    private Search()
    {
    }

    /**
     * Search.
     *
     * @param _query the query
     * @param _numHits the num hits
     * @return the search result
     * @throws EFapsException on error
     */
    public SearchResult search(final String _query,
                               final int _numHits)
        throws EFapsException
    {
        final SearchResult ret = new SearchResult();
        try {
            LOG.debug("Starting search with: {}", _query);
            final StandardQueryParser queryParser = new StandardQueryParser(Index.getAnalyzer());
            final Query query = queryParser.parse(_query, "ALL");

            final IndexReader reader = DirectoryReader.open(Index.getDirectory());

            final IndexSearcher searcher = new IndexSearcher(reader);
            final TopDocs docs = searcher.search(query, _numHits);
            final ScoreDoc[] hits = docs.scoreDocs;

            LOG.debug("Found {} hits.", hits.length);
            for (int i = 0; i < hits.length; ++i) {
                final Document doc = searcher.doc(hits[i].doc);
                final String oid = doc.get(Key.OID.name());
                final String text = doc.get(Key.MSGPHRASE.name());
                LOG.debug("{}. {}\t {}", i + 1, oid, text);
                final Instance instance = Instance.get(oid);
                final List<Instance> list;
                if (this.typeMapping.containsKey(instance.getType())) {
                    list = this.typeMapping.get(instance.getType());
                } else {
                    list = new ArrayList<Instance>();
                    this.typeMapping.put(instance.getType(), list);
                }
                list.add(instance);
                this.elements.put(instance, new Element().setOid(oid).setText(text));
            }
            reader.close();
            checkAccess();
            ret.getElements().addAll(this.elements.values());
        } catch (final IOException | QueryNodeException e) {
            LOG.error("Catched Exception", e);
        }
        return ret;
    }

    /**
     * Check access.
     *
     * @throws EFapsException on error
     */
    private void checkAccess()
        throws EFapsException
    {
        // check the access for the given instances
        final Map<Instance, Boolean> accessmap = new HashMap<Instance, Boolean>();
        for (final Entry<Type, List<Instance>> entry : this.typeMapping.entrySet()) {
            accessmap.putAll(entry.getKey().checkAccess(entry.getValue(), AccessTypeEnums.SHOW.getAccessType()));
        }
        this.elements.entrySet().removeIf(entry -> accessmap.size() > 0 && (!accessmap.containsKey(entry.getKey())
                        || !accessmap.get(entry.getKey())));
    }

    /**
     * Search.
     *
     * @param _query the query
     * @return the search result
     * @throws EFapsException on error
     */
    public static SearchResult search(final String _query)
        throws EFapsException
    {
        return new Search().search(_query, 1000);
    }
}
