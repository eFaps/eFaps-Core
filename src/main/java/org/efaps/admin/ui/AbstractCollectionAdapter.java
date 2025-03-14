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

import java.util.List;

import org.infinispan.protostream.annotations.ProtoField;

public class AbstractCollectionAdapter
    extends AbstractUserInterfaceObjectAdapter
{

    @ProtoField(number = 100)
    List<Long> getFieldIds(AbstractCollection abstractCollection)
    {
        return abstractCollection.getFieldIds();
    }

    protected void setFields(AbstractCollection abstractCollection,
                             List<Long> fieldIds)
    {
        abstractCollection.setFieldIds(fieldIds);
    }
}
