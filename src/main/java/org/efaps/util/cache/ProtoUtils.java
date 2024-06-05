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

import java.util.HashMap;
import java.util.Map;

public class ProtoUtils
{

    public static Map<String, String> toMap(Map<Long, Long> original)
    {
        final Map<String, String> map = new HashMap<>();
        for (final var entry : original.entrySet()) {
            map.put(toString(entry.getKey()), toString(entry.getValue()));
        }
        return map;
    }

    public static Map<Long, Long> fromMap(Map<String, String> original)
    {
        final Map<Long, Long> map = new HashMap<>();
        for (final var entry : original.entrySet()) {
            map.put(toLong(entry.getKey()), toLong(entry.getValue()));
        }
        return map;
    }

    public static String toString(Long original)
    {
        return original == null ? null : original.toString();
    }

    public static Long toLong(String original)
    {
        return original == null ? null : Long.valueOf(original);
    }

    public static Long toNullLong(Long original)
    {
        return original == null ? null : original == 0 ? null : original;
    }
}
