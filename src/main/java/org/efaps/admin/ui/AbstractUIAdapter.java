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

import java.util.Map;

import org.efaps.admin.AbstractAdminObjectAdapter;
import org.infinispan.protostream.annotations.ProtoField;

public abstract class AbstractUIAdapter
    extends AbstractAdminObjectAdapter
{

    @ProtoField(number = 1, defaultValue = "0")
    long getId(AbstractUserInterfaceObject uiObject)
    {
        return uiObject.getId();
    }

    @ProtoField(number = 2)
    String getUuid(AbstractUserInterfaceObject uiObject)
    {
        return uiObject.getUUID() == null ? null : uiObject.getUUID().toString();
    }

    @ProtoField(number = 3)
    String getName(AbstractUserInterfaceObject uiObject)
    {
        return uiObject.getName();
    }

    @ProtoField(number = 4)
    Map<String, String> getPropertyMap(AbstractUserInterfaceObject uiObject)
    {
        return uiObject.getPropertyMap();
    }
}
