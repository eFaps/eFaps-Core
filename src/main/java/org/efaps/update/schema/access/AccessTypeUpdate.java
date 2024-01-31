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
package org.efaps.update.schema.access;

import org.efaps.update.AbstractUpdate;
import org.efaps.update.Install.InstallFile;

/**
 * @author The eFaps Team
 */
public class AccessTypeUpdate
    extends AbstractUpdate
{
    /**
     * Instantiates a new access type update.
     *
     * @param _installFile the install file
     */
    public AccessTypeUpdate(final InstallFile _installFile)
    {
        super(_installFile, "Admin_Access_AccessType");
    }

    /**
     * Creates new instance of class {@link AccessTypeUpdate.Definition}.
     *
     * @return new definition instance
     * @see AccessTypeUpdate.Definition
     */
    @Override
    protected AbstractDefinition newDefinition()
    {
        return new Definition();
    }

    /**
     * Defines the access type.
     */
    private class Definition
        extends AbstractDefinition
    {
    }
}
