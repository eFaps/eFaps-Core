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

import javax.sql.ConnectionEventListener;
import javax.sql.StatementEventListener;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.glassfish.hk2.api.Factory;

public class XADatasourceProvider
    implements Factory<XADataSource>
{

    public static final String JDBCURL = "jdbc:acolyte:anything-you-want?handler=my-handler-id";

    private final acolyte.jdbc.Driver driver = new acolyte.jdbc.Driver();

    private final XAConnectionWrapper xaConnection = new XAConnectionWrapper();

    private final XAResourceWrapper xaResource = new XAResourceWrapper();

    @Override
    public XADataSource provide()
    {
        final var ds = new XADataSource()
        {

            @Override
            public Logger getParentLogger()
                throws SQLFeatureNotSupportedException
            {

                return null;
            }

            @Override
            public PrintWriter getLogWriter()
                throws SQLException
            {

                return null;
            }

            @Override
            public int getLoginTimeout()
                throws SQLException
            {

                return 0;
            }

            @Override
            public void setLogWriter(PrintWriter arg0)
                throws SQLException
            {

            }

            @Override
            public void setLoginTimeout(int arg0)
                throws SQLException
            {

            }

            @Override
            public XAConnection getXAConnection()
                throws SQLException
            {
                return xaConnection;
            }

            @Override
            public XAConnection getXAConnection(String arg0,
                                                String arg1)
                throws SQLException
            {
                return xaConnection;
            }
        };

        return ds;
    }

    @Override
    public void dispose(XADataSource instance)
    {

    }

    public class XAConnectionWrapper
        implements XAConnection
    {

        @Override
        public void addConnectionEventListener(ConnectionEventListener arg0)
        {

        }

        @Override
        public void addStatementEventListener(StatementEventListener arg0)
        {

        }

        @Override
        public void close()
            throws SQLException
        {

        }

        @Override
        public Connection getConnection()
            throws SQLException
        {
            return driver.connect(JDBCURL);
        }

        @Override
        public void removeConnectionEventListener(ConnectionEventListener arg0)
        {

        }

        @Override
        public void removeStatementEventListener(StatementEventListener arg0)
        {

        }

        @Override
        public XAResource getXAResource()
            throws SQLException
        {
            return xaResource;
        }
    }

    public class XAResourceWrapper
        implements XAResource
    {

        @Override
        public void commit(Xid arg0,
                           boolean arg1)
            throws XAException
        {

        }

        @Override
        public void end(Xid arg0,
                        int arg1)
            throws XAException
        {

        }

        @Override
        public void forget(Xid arg0)
            throws XAException
        {

        }

        @Override
        public int getTransactionTimeout()
            throws XAException
        {

            return 0;
        }

        @Override
        public boolean isSameRM(XAResource arg0)
            throws XAException
        {

            return false;
        }

        @Override
        public int prepare(Xid arg0)
            throws XAException
        {

            return 0;
        }

        @Override
        public Xid[] recover(int arg0)
            throws XAException
        {

            return null;
        }

        @Override
        public void rollback(Xid arg0)
            throws XAException
        {

        }

        @Override
        public boolean setTransactionTimeout(int arg0)
            throws XAException
        {

            return false;
        }

        @Override
        public void start(Xid arg0,
                          int arg1)
            throws XAException
        {

        }

    }
}
