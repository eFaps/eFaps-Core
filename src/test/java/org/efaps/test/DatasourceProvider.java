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
package org.efaps.test;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.glassfish.hk2.api.Factory;

public class DatasourceProvider implements Factory<DataSource>
{
    public static final String JDBCURL = "jdbc:acolyte:anything-you-want?handler=my-handler-id";

    private final acolyte.jdbc.Driver driver = new acolyte.jdbc.Driver();

    @Override
    public DataSource provide() {
        final var ds = new DataSource() {

            @Override
            public Logger getParentLogger()
                throws SQLFeatureNotSupportedException
            {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean isWrapperFor(Class<?> arg0)
                throws SQLException
            {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public <T> T unwrap(Class<T> arg0)
                throws SQLException
            {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Connection getConnection()
                throws SQLException
            {
                return driver.connect(JDBCURL);
            }

            @Override
            public Connection getConnection(String arg0,
                                            String arg1)
                throws SQLException
            {
                return driver.connect(JDBCURL);
            }

            @Override
            public PrintWriter getLogWriter()
                throws SQLException
            {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int getLoginTimeout()
                throws SQLException
            {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public void setLogWriter(PrintWriter arg0)
                throws SQLException
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void setLoginTimeout(int arg0)
                throws SQLException
            {
                // TODO Auto-generated method stub

            }
        };

        return ds;
    }

    @Override
    public void dispose(DataSource instance)
    {
        // TODO Auto-generated method stub
    }
}
