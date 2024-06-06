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
package org.efaps.util.cache;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.NearCacheMode;
import org.infinispan.client.hotrod.configuration.RemoteCacheConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.TransactionMode;
import org.infinispan.client.hotrod.transaction.lookup.GenericTransactionManagerLookup;
import org.infinispan.commons.api.BasicCache;
import org.infinispan.commons.api.BasicCacheContainer;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.lifecycle.ComponentStatus;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.query.remote.client.ProtobufMetadataManagerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
public final class InfinispanCache
{

    /**
     * Logger for this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(InfinispanCache.class);

    /**
     * Key to the Counter Cache.
     */
    private static String COUNTERCACHE = InfinispanCache.class.getName() + ".InternalCounter";

    /**
     * The instance used for singelton.
     */
    private static InfinispanCache CACHEINSTANCE;

    /**
     * The manager for Infinspan.
     */
    private BasicCacheContainer container;

    /**
     * Singelton is wanted.
     */
    private InfinispanCache()
    {
    }

    /**
     * init this instance.
     */
    private void init()
    {
        if (true) {
            final var config = new org.infinispan.client.hotrod.configuration.ConfigurationBuilder()
                            .uri("hotrod://admin:secret@localhost:11222")
                            .clientIntelligence(ClientIntelligence.BASIC)
                            .addContextInitializer(new LibraryInitializerImpl())
                            .connectionTimeout(20).batchSize(100)
                            .connectionPool().minIdle(5)
                            .transactionTimeout(1, TimeUnit.HOURS)
                            .build();
            container = new RemoteCacheManager(config);
            registerSchemas((RemoteCacheManager) container);
        } else if (this.container == null) {
            try {
                this.container = new DefaultCacheManager(this.getClass().getResourceAsStream(
                                "/org/efaps/util/cache/infinispan-config.xml"));
                if (this.container instanceof EmbeddedCacheManager) {
                    ((EmbeddedCacheManager) this.container).addListener(new CacheLogListener(InfinispanCache.LOG));
                }
                final var cache = this.container
                                .<String, Integer>getCache(InfinispanCache.COUNTERCACHE);
                cache.put(InfinispanCache.COUNTERCACHE, 1);
            } catch (final IOException e) {
                InfinispanCache.LOG.error("IOException", e);
            }
        } else {
            final var cache = this.container
                            .<String, Integer>getCache(InfinispanCache.COUNTERCACHE);
            Integer count = cache.get(InfinispanCache.COUNTERCACHE);
            if (count == null) {
                count = 1;
            } else {
                count = count + 1;
            }
            cache.put(InfinispanCache.COUNTERCACHE, count);
        }
    }

    private void registerSchemas(RemoteCacheManager remoteCacheManager)
    {

        final var initializer = remoteCacheManager.getConfiguration().getContextInitializers().get(0);
        // Store schemas in the '___protobuf_metadata' cache to register them.
        // Using ProtobufMetadataManagerConstants might require the query
        // dependency.
        final RemoteCache<String, String> protoMetadataCache = remoteCacheManager
                        .getCache(ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME);
        protoMetadataCache.putIfAbsent(initializer.getProtoFileName(), initializer.getProtoFile());
        // Ensure the registered Protobuf schemas do not contain errors.
        // Throw an exception if errors exist.
        final String errors = protoMetadataCache.get(ProtobufMetadataManagerConstants.ERRORS_KEY_SUFFIX);
        if (errors != null) {
            throw new IllegalStateException("Some Protobuf schema files contain errors: " +
                            errors + "\nSchema :\n" + initializer.getProtoFileName());
        }
    }


    /**
     * Terminate the manager.
     */
    private void terminate()
    {
        if (this.container != null) {
            final var cache = this.container
                            .<String, Integer>getCache(InfinispanCache.COUNTERCACHE);
            Integer count = cache.get(InfinispanCache.COUNTERCACHE);
            if (count == null || count < 2) {
                this.container.stop();
            } else {
                count = count - 1;
                cache.put(InfinispanCache.COUNTERCACHE, count);
            }
        }
    }

    /**
     * @param _cacheName cache wanted
     * @param <K> Key
     * @param <V> Value
     * @return a cache from Infinspan
     */
    public synchronized <K, V> BasicCache<K, V> getCache(final String cacheName)
    {
        if (this.container instanceof RemoteCacheManager) {

            final var config = ((RemoteCacheManager) this.container).getConfiguration();
            if (!config.remoteCaches().containsKey(cacheName)) {

                final var tep = """
<replicated-cache name=\"org.efaps.admin.common.SystemConfiguration.UUID\" mode=\"SYNC\" statistics=\"true\">
    <encoding media-type=\"application/x-protostream\"/>
    <locking isolation=\"REPEATABLE_READ\"/>
    <transaction mode=\"NON_XA\" locking=\"OPTIMISTIC\"/>
</replicated-cache>
                            """;
                final var consumer = (Consumer<RemoteCacheConfigurationBuilder>) bldr -> bldr
                                //.templateName(DefaultTemplate.REPL_ASYNC)
                                .configuration(tep)
                                .nearCacheMode(NearCacheMode.DISABLED)
                                .nearCacheMaxEntries(1000)
                                .nearCacheUseBloomFilter(false)
                                .transactionManagerLookup(GenericTransactionManagerLookup.getInstance())
                                .transactionMode(TransactionMode.NON_XA);
                config.addRemoteCache(cacheName, consumer);
            }
        }
        return this.container.getCache(cacheName);
    }

    public <K, V> BasicCache<K, V> getCache(final String _cacheName,
                                            final Logger addListener)
    {
        return this.container.getCache(_cacheName);
    }

    /**
     * Gets the manager for Infinspan.
     *
     * @return the manager for Infinspan
     */
    public BasicCacheContainer getContainer()
    {
        return this.container;
    }

    /**
     * Method to init a Cache using the default definitions.
     *
     * @param _cacheName cache wanted
     * @param <K> Key
     * @param <V> Value
     * @return a cache from Infinspan
     */
    public <K, V> BasicCache<K, V> initCache(final String cacheName)
    {
        return this.initCache(cacheName, null);
    }

    public <K, V> BasicCache<K, V> initCache(final String cacheName,
                                             final Consumer<RemoteCacheConfigurationBuilder> consumer)
    {
        if (this.container instanceof RemoteCacheManager) {
            final var config = ((RemoteCacheManager) this.container).getConfiguration();
            config.addRemoteCache(cacheName, consumer);
        } else if (!exists(cacheName)
                        && ((EmbeddedCacheManager) getContainer()).getCacheConfiguration(cacheName) == null) {
            ((EmbeddedCacheManager) getContainer()).defineConfiguration(cacheName, "eFaps-Default",
                            new ConfigurationBuilder().build());
        }
        return this.container.getCache(cacheName);
    }

    /**
     * @param _cacheName cache wanted
     * @return true if cache exists
     */
    public boolean exists(final String _cacheName)
    {
        final boolean ret;
        if (this.container instanceof EmbeddedCacheManager) {
            ret = ((EmbeddedCacheManager) this.container).cacheExists(_cacheName);
        } else {
            ret = true;
        }
        return ret;
    }

    /**
     * @return the InfinispanCache
     */
    public static InfinispanCache get()
    {
        if (InfinispanCache.CACHEINSTANCE == null) {
            InfinispanCache.CACHEINSTANCE = new InfinispanCache();
            InfinispanCache.CACHEINSTANCE.init();
        }
        if (InfinispanCache.CACHEINSTANCE.container instanceof DefaultCacheManager) {
            final ComponentStatus status = ((DefaultCacheManager) InfinispanCache.CACHEINSTANCE.container).getStatus();
            if (status.isStopping() || status.isTerminated()) {
                InfinispanCache.CACHEINSTANCE = new InfinispanCache();
                InfinispanCache.CACHEINSTANCE.init();
            }
        }
        return InfinispanCache.CACHEINSTANCE;
    }

    /**
     * Stop the manager.
     */
    public static void stop()
    {
        if (InfinispanCache.CACHEINSTANCE != null) {
            InfinispanCache.CACHEINSTANCE.terminate();
        }
    }
}
