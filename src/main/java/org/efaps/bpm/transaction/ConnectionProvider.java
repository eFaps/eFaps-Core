/*
 * Copyright 2003 - 2016 The eFaps Team
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

package org.efaps.bpm.transaction;

import java.sql.Connection;
import java.sql.SQLException;

import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.hibernate.engine.jdbc.connections.internal.UserSuppliedConnectionProviderImpl;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 *
 */
public class ConnectionProvider
    extends UserSuppliedConnectionProviderImpl
{
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Obtains a connection for Hibernate use according to the underlying
     * strategy of this provider.
     *
     * @return The obtained JDBC connection
     *
     * @throws SQLException Indicates a problem opening a connection
     */
    @Override
    public Connection getConnection()
        throws SQLException
    {
        try {
            return Context.getConnection();
        } catch (final EFapsException e) {
            throw new SQLException(e);
        }
    }

    /**
     * Release a connection from Hibernate use.
     *
     * @param _conn The JDBC connection to release
     *
     * @throws SQLException Indicates a problem closing the connection
     */
    @Override
    public void closeConnection(final Connection _conn)
        throws SQLException
    {

    }
}
