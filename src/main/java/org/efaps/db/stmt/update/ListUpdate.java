/*
 * Copyright 2003 - 2018 The eFaps Team
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

package org.efaps.db.stmt.update;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.efaps.admin.access.user.AccessCache;
import org.efaps.db.Instance;
import org.efaps.eql2.IUpdateListStatement;

public class ListUpdate
    extends AbstractUpdate
{
    final List<Instance> instances;

    public ListUpdate(final IUpdateListStatement _eqlStmt)
    {
        super(_eqlStmt);
        this.instances = _eqlStmt.getOidsList().stream()
                        .map(oid -> Instance.get(oid))
                        .collect(Collectors.toList());
        this.instances.stream().collect(Collectors.groupingBy(Instance::getType));
        this.instances.forEach(instance -> AccessCache.registerUpdate(instance));
    }

    public List<Instance> getInstances()
    {
        return Collections.unmodifiableList(this.instances);
    }
}
