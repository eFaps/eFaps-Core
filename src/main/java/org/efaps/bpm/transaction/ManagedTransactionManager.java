/*
 * Copyright 2003 - 2013 The eFaps Team
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
 * Revision:        $Rev$
 * Last Changed:    $Date$
 * Last Changed By: $Author$
 */

package org.efaps.bpm.transaction;

import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.jbpm.persistence.jta.ContainerManagedTransactionManager;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 * @version $Id$
 */
public class ManagedTransactionManager
    extends ContainerManagedTransactionManager
{

    boolean started = false;

    @Override
    public boolean begin()
    {
        try {
            if (!Context.isThreadActive()
                            || (Context.isThreadActive() && !Context.isTMActive())) {
                Context.begin("Administrator", false);
                this.started = true;
            }
        } catch (final EFapsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return this.started;
    }

    @Override
    public void commit(final boolean _owner)
    {
        if (_owner) {
            this.started = false;
            try {
                Context.commit();
                Context.begin("Administrator", false);
            } catch (final EFapsException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
