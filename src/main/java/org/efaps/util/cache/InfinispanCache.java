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
import java.io.StringWriter;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.ConfigProvider;
import org.infinispan.Cache;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.RemoteCacheConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.TransactionMode;
import org.infinispan.commons.configuration.io.xml.XmlConfigurationWriter;
import org.infinispan.commons.dataconversion.MediaType;
import org.infinispan.commons.io.ByteBuffer;
import org.infinispan.commons.marshall.AbstractMarshaller;
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

    private String prefix = "";

    private String hotrodUrl = "";

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
        final var config = ConfigProvider.getConfig();
        final boolean clustered = config.getOptionalValue("core.cache.cluster.active", Boolean.class).orElse(false);
        LOG.info("Cache clustering active: {}", clustered);

        if (clustered) {
            prefix = config.getOptionalValue("core.cache.cluster.prefix", String.class).orElse("");
            hotrodUrl = config.getOptionalValue("core.cache.cluster.hotrodUrl", String.class).orElse("");
            System.getProperties().put("hotrodUrl", hotrodUrl);
            registerSchemas();
        }
        try {
            if (StringUtils.isNotEmpty(this.prefix)) {
                System.getProperties().put("prefix", prefix + "-");
            }

            this.container = new DefaultCacheManager(this.getClass()
                            .getResourceAsStream(clustered ? "/org/efaps/util/cache/infinispan-config-cluster.xml"
                                            : "/org/efaps/util/cache/infinispan-config.xml"),
                            false);

            new AbstractMarshaller() {

                @Override
                public Object objectFromByteBuffer(byte[] buf,
                                                   int offset,
                                                   int length)
                    throws IOException, ClassNotFoundException
                {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public boolean isMarshallable(Object o)
                    throws Exception
                {
                    // TODO Auto-generated method stub
                    return false;
                }

                @Override
                public MediaType mediaType()
                {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                protected ByteBuffer objectToBuffer(Object o,
                                                    int estimatedSize)
                    throws IOException, InterruptedException
                {
                    // TODO Auto-generated method stub
                    return null;
                }

            };


            if (clustered) {
                final var remoteCacheManager = getRemoteCacheManager();
                for (final var cacheName : this.container.getCacheNames()) {
                    final var cacheConfig = container.getCacheConfiguration(cacheName);

                    if (cacheConfig.clustering() != null && cacheConfig.clustering().cacheMode() != CacheMode.LOCAL) {
                        final var xml = cacheConfig.toStringConfiguration(cacheName);
                        LOG.info("Registering clustered cache: {}", xml);
                        final var consumer = (Consumer<RemoteCacheConfigurationBuilder>) bldr -> bldr
                                        .configuration(xml)
                                        .transactionMode(TransactionMode.NONE);

                        final var remoteConfig = remoteCacheManager.getConfiguration();
                        remoteConfig.addRemoteCache(cacheName, consumer);
                        remoteCacheManager.getCache(cacheName);
                    }
                    if (cacheConfig.persistence() != null && cacheConfig.persistence().usingStores()) {
                        final var storeConfig = cacheConfig.persistence().stores().get(0);
                        final var persitenceCacheName = storeConfig.attributes().<String>attribute("cache").get();
                        LOG.info("Registering persistence cache: {}", persitenceCacheName);
                        final var encodingWriter = new StringWriter();
                        cacheConfig.encoding().write(new XmlConfigurationWriter(encodingWriter, true, true));
                        final var template = """
                                        <?xml version=\"1.0\"?>
                                        <replicated-cache mode=\"ASYNC\" statistics=\"true\">
                                        """
                                        +
                                        encodingWriter.toString()
                                        +
                                        """
                                            <locking concurrency-level=\"1000\" acquire-timeout=\"15000\" striping=\"false\"/>
                                            <state-transfer timeout=\"60000\"/>
                                        </replicated-cache>
                                                                                """;

                        final var consumer = (Consumer<RemoteCacheConfigurationBuilder>) bldr -> bldr
                                        .configuration(template)
                                        .transactionMode(TransactionMode.NONE);

                        final var remoteConfig = remoteCacheManager.getConfiguration();
                        remoteConfig.addRemoteCache(persitenceCacheName, consumer);
                        remoteCacheManager.getCache(persitenceCacheName);
                    }

                }
                remoteCacheManager.close();
            }
            this.container.start();
        } catch (final IOException e) {
            LOG.error("Catched", e);
        }
    }

    private RemoteCacheManager getRemoteCacheManager()
    {
        final var config = new org.infinispan.client.hotrod.configuration.ConfigurationBuilder()
                        .uri(hotrodUrl)
                        .addContextInitializer(new LibraryInitializerImpl())
                        .build();
        return new RemoteCacheManager(config);
    }

    private void registerSchemas()
    {
        final var remoteCacheManager = getRemoteCacheManager();

        final var initializer = remoteCacheManager.getConfiguration().getContextInitializers().get(0);
        final RemoteCache<String, String> protoMetadataCache = remoteCacheManager
                        .getCache(ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME);
        if (!protoMetadataCache.containsKey(initializer.getProtoFileName())) {
            protoMetadataCache.put(initializer.getProtoFileName(), initializer.getProtoFile());
            final String errors = protoMetadataCache.get(ProtobufMetadataManagerConstants.ERRORS_KEY_SUFFIX);
            if (errors != null) {
                throw new IllegalStateException("Some Protobuf schema files contain errors: " +
                                errors + "\nSchema :\n" + initializer.getProtoFileName());
            }
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

    private void clearAll()
    {
        for (final var cacheName : this.container.getCacheNames()) {
            final var cache = this.container.getCache(cacheName, false);
            if (cache != null) {
                cache.clear();
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
        return this.container.getCache(getCacheName(cacheName));
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
        final var name = getCacheName(cacheName);
        LOG.info("Initializing Cache {}", name);
        final var cacheConfig = container.getCacheConfiguration(name);
        if (!exists(cacheName) && cacheConfig == null) {
            getContainer().defineConfiguration(name, "eFaps-Default",
                            new ConfigurationBuilder().build());
        }
        final Cache<K, V> cache = this.container.getCache(name);
        if (logger != null) {
            cache.addListener(new CacheLogListener(logger));
        }
        return cache;
    }

    /**
     * @param _cacheName cache wanted
     * @return true if cache exists
     */
    public boolean exists(final String cacheName)
    {
        return this.container.cacheExists(getCacheName(cacheName));
    }

    protected String getCacheName(String cacheName)
    {
        String ret = cacheName;
        if (StringUtils.isNotEmpty(this.prefix)) {
            ret = prefix + "-" + ret;
        }
        return ret;
    }

    public static void clear()
    {
        if (InfinispanCache.CACHEINSTANCE != null) {
            InfinispanCache.CACHEINSTANCE.clearAll();
        }
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
