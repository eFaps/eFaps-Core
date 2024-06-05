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

import org.infinispan.client.hotrod.DefaultTemplate;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.RemoteCacheConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.TransactionMode;
import org.infinispan.client.hotrod.near.DefaultNearCacheFactory;
import org.infinispan.commons.api.BasicCache;
import org.infinispan.commons.api.BasicCacheContainer;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.lifecycle.ComponentStatus;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
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
                            .build();
            container = new RemoteCacheManager(config);
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
    public <K, V> BasicCache<K, V> getCache(final String cacheName)
    {
        BasicCache<K, V> cache = this.container.getCache(cacheName);
        if (cache == null) {
            if (this.container instanceof RemoteCacheManager) {
                final var config = ((RemoteCacheManager) this.container).getConfiguration();
                final var consumer = (Consumer<RemoteCacheConfigurationBuilder>) bldr -> bldr
                                .templateName(DefaultTemplate.REPL_ASYNC)
                                .nearCacheFactory(new DefaultNearCacheFactory())
                                .transactionMode(TransactionMode.NONE);
                config.addRemoteCache(cacheName, consumer);
            }
            cache = this.container.getCache(cacheName);
        }
        return cache;
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
            ret = this.container.getCache(_cacheName) != null;
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
