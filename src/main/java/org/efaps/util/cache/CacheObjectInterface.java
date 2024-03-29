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

import java.util.UUID;

/**
 * Common interface of cached object.
 *
 * @author The eFaps Team
 *
 */
public interface CacheObjectInterface
{
    /**
     * Returns the name of the cached object.
     *
     * @return name of the cached object
     */
    String getName();

    /**
     * Returns the UUID of the cached object.
     *
     * @return UUID of the cached object
     */
    UUID getUUID();

    /**
     * Returns the id of the cached object.
     *
     * @return id of the cached object
     */
    long getId();
}
