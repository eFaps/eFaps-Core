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
package org.efaps.admin.ui;

import java.util.Set;

import org.efaps.admin.AbstractAdminObjectAdapter;
import org.infinispan.protostream.annotations.ProtoField;

public abstract class AbstractUserInterfaceObjectAdapter
    extends AbstractAdminObjectAdapter
{
    @ProtoField(number = 300)
    Set<Long> getAccess(AbstractUserInterfaceObject abstractUserInterfaceObject)
    {
        return abstractUserInterfaceObject.getAccess();
    }

    public void setAccess(final AbstractUserInterfaceObject abstractUserInterfaceObject,
                          final Set<Long> access)
    {
        abstractUserInterfaceObject.getAccess().addAll(access);
    }
}
