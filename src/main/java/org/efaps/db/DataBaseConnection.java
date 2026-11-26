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
package org.efaps.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBaseConnection
{
    protected final Connection connection;

    public DataBaseConnection(Connection con)
    {
        connection = con;
    }

    public PreparedStatement prepareStatement(final String sql)
        throws SQLException
    {
        return connection.prepareStatement(sql);
    }

    public PreparedStatement prepareStatement(final String sql,
                                              final String[] columns)
        throws SQLException
    {
        return connection.prepareStatement(sql, new String[] { "ID" });
    }

    public Statement createStatement()
        throws SQLException
    {
        return connection.createStatement();
    }

    public DatabaseMetaData getMetaData()
        throws SQLException
    {
        return connection.getMetaData();
    }

    public PreparedStatement prepareStatement(final String sql,
                                              final int flag)
        throws SQLException
    {
        return connection.prepareStatement(sql, flag);
    }


}
