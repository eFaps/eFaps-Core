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
package org.efaps.eql.builder;

import org.efaps.db.stmt.DeleteStmt;
import org.efaps.eql2.IDeleteStatement;
import org.efaps.eql2.bldr.AbstractDeleteEQLBuilder;
import org.efaps.util.EFapsException;

public class Delete
    extends AbstractDeleteEQLBuilder<Delete>
{
    /**
     * Stmt.
     *
     * @return the prints the stmt
     */
    public DeleteStmt stmt()
    {
        return DeleteStmt.get((IDeleteStatement<?>) getStmt());
    }

    public Delete execute()
        throws EFapsException
    {
        stmt().execute();
        return getThis();
    }

    @Override
    protected Delete getThis()
    {
        return this;
    }
}
