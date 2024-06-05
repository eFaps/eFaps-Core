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

import org.infinispan.protostream.annotations.ProtoField;

public class AbstractCommandAdapter
    extends AbstractUIAdapter
{

    @ProtoField(number = 200)
    Long getTargetFormId(AbstractCommand abstractCommand)
    {
        return abstractCommand.getTargetFormId();
    }

    @ProtoField(number = 201)
    Long getTargetMenuId(AbstractCommand abstractCommand)
    {
        return abstractCommand.getTargetMenuId();
    }

    @ProtoField(number = 202)
    Long getTargetTableId(AbstractCommand abstractCommand)
    {
        return abstractCommand.getTargetTableId();
    }

    @ProtoField(number = 203)
    Long getTargetSearchId(AbstractCommand abstractCommand)
    {
        return abstractCommand.getTargetSearchId();
    }

    @ProtoField(number = 204)
    Long getTargetCommandId(AbstractCommand abstractCommand)
    {
        return abstractCommand.getTargetCommandId();
    }
}
