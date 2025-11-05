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
package org.efaps.admin.common;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.postgresql.ds.PGSimpleDataSource;
import org.quartz.utils.ConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuartzConnectionProvider
    implements ConnectionProvider
{

    private static final Logger LOG = LoggerFactory.getLogger(QuartzConnectionProvider.class);
    private static DataSource DATASOURCE;
    private String url;
    private String user;
    private String password;

    @Override
    public Connection getConnection()
        throws SQLException
    {
        return DATASOURCE.getConnection();
    }

    @Override
    public void shutdown()
        throws SQLException
    {
    }

    @Override
    public void initialize()
        throws SQLException
    {
        LOG.info("initializin datasource");
        final var ds = new PGSimpleDataSource();
        ds.setURL(url);
        ds.setUser(user);
        ds.setPassword(password);
        DATASOURCE = ds;
    }

    public void setURL(String url)
    {
        LOG.info("set url: {}", url);
        this.url = url;
    }

    public void setUser(String user)
    {
        LOG.info("set user: {}", user);
        this.user = user;
    }

    public void setPassword(String password)
    {
        LOG.info("set password");
        this.password = password;
    }

}
