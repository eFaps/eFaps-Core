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
package org.efaps.admin.index;

import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.KernelSettings;
import org.efaps.db.Instance;
import org.efaps.util.EFapsException;
import org.efaps.util.cache.InfinispanCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
public final class Queue
{

    /**
     * Name of the Cache for Instances.
     */
    public static final String CACHENAME = Queue.class.getName() + ".Cache";

    private static final Logger LOG = LoggerFactory.getLogger(Queue.class);

    /**
     * Instantiates a new queue.
     */
    private Queue()
    {
    }

    /**
     * Register update.
     *
     * @param instance the _instance
     * @throws EFapsException the e faps exception
     */
    public static void registerUpdate(final Instance instance)
    {
        // check if SystemConfiguration exists, necessary during install
        try {
            if (EFapsSystemConfiguration.get() != null
                            && EFapsSystemConfiguration.get()
                                            .getAttributeValueAsBoolean(KernelSettings.INDEXACTIVATE)) {
                if (instance != null && instance.getType() != null
                                && IndexDefinition.get(instance.getType().getUUID()) != null) {
                    final var cache = InfinispanCache.get().<String, String>getCache(CACHENAME);
                    cache.put(instance.getOid(), instance.getOid());
                }
            }
        } catch (final EFapsException e) {
            LOG.error("Catched", e);
        }
    }
}
