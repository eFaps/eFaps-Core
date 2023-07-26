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
