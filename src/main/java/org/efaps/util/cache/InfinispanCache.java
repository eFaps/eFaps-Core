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
import java.util.function.Consumer;

import org.infinispan.Cache;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.RemoteCacheConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.TransactionMode;
import org.infinispan.configuration.cache.CacheMode;
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
    private EmbeddedCacheManager container;

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
        registerSchemas();
        try {
            this.container = new DefaultCacheManager(this.getClass().getResourceAsStream(
                            "/org/efaps/util/cache/infinispan-config-cluster.xml"), false);
            final var remoteCacheManager = getRemoteCacheManager();
            for (final var cacheName : this.container.getCacheNames()) {
                final var cacheConfig = container.getCacheConfiguration(cacheName);
                if (cacheConfig.clustering() != null && cacheConfig.clustering().cacheMode() != CacheMode.LOCAL) {
                    final var xml = cacheConfig.toStringConfiguration(cacheName);
                    System.out.println(xml);
                    final var consumer = (Consumer<RemoteCacheConfigurationBuilder>) bldr -> bldr
                                    .configuration(xml)
                                    .transactionMode(TransactionMode.NONE);

                    final var config = remoteCacheManager.getConfiguration();
                    config.addRemoteCache(cacheName, consumer);
                    remoteCacheManager.getCache(cacheName);

                    // remoteCacheManager.administration().getOrCreateCache(cacheName,
                    // xml);
                }
            }
            remoteCacheManager.close();
            this.container.start();
        } catch (final IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private RemoteCacheManager getRemoteCacheManager()
    {
        final var config = new org.infinispan.client.hotrod.configuration.ConfigurationBuilder()
                        .uri("hotrod://admin:secret@localhost:11222")
                        .addContextInitializer(new LibraryInitializerImpl())
                        .build();
        return new RemoteCacheManager(config);
    }

    private void registerSchemas()
    {
        final var config = new org.infinispan.client.hotrod.configuration.ConfigurationBuilder()
                        .uri("hotrod://admin:secret@localhost:11222")
                        .addContextInitializer(new LibraryInitializerImpl())
                        .build();

        final var remoteCacheManager = new RemoteCacheManager(config);

        final var initializer = remoteCacheManager.getConfiguration().getContextInitializers().get(0);
        final RemoteCache<String, String> protoMetadataCache = remoteCacheManager
                        .getCache(ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME);
        protoMetadataCache.put(initializer.getProtoFileName(), initializer.getProtoFile());
        final String errors = protoMetadataCache.get(ProtobufMetadataManagerConstants.ERRORS_KEY_SUFFIX);
        if (errors != null) {
            throw new IllegalStateException("Some Protobuf schema files contain errors: " +
                            errors + "\nSchema :\n" + initializer.getProtoFileName());
        }
        remoteCacheManager.close();
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
    public synchronized <K, V> Cache<K, V> getCache(final String cacheName)
    {
        return this.container.getCache(cacheName);
    }

    public <K, V> Cache<K, V> getCache(final String _cacheName,
                                            final Logger addListener)
    {
        return this.container.getCache(_cacheName);
    }

    /**
     * Gets the manager for Infinspan.
     *
     * @return the manager for Infinspan
     */
    public EmbeddedCacheManager getContainer()
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
    public <K, V> Cache<K, V> initCache(final String cacheName)
    {
        return this.initCache(cacheName, null);
    }

    public <K, V> Cache<K, V> initCache(final String cacheName,
                                        final Logger logger)
    {
        final var cacheConfig = container.getCacheConfiguration(cacheName);
        if (!exists(cacheName) && cacheConfig == null) {
            getContainer().defineConfiguration(cacheName, "eFaps-Default",
                            new ConfigurationBuilder().build());
        }
        final Cache<K, V> cache = this.container.getCache(cacheName);
        if (logger != null) {
            cache.addListener(new CacheLogListener(logger));
        }
        return cache;
    }

    /**
     * @param _cacheName cache wanted
     * @return true if cache exists
     */
    public boolean exists(final String _cacheName)
    {
        return this.container.cacheExists(_cacheName);
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
        synchronized (InfinispanCache.CACHEINSTANCE) {
            return InfinispanCache.CACHEINSTANCE;
        }
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
