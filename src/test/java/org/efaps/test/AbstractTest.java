/*
 * Copyright 2003 - 2017 The eFaps Team
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

package org.efaps.test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang3.ArrayUtils;
import org.efaps.db.Context;
import org.efaps.db.databases.AbstractDatabase;
import org.efaps.init.StartupDatabaseConnection;
import org.efaps.init.StartupException;
import org.efaps.jaas.AppAccessHandler;
import org.efaps.mock.Mocks;
import org.efaps.mock.datamodel.IDataModel;
import org.efaps.mock.datamodel.Person;
import org.efaps.mock.db.MockDatabase;
import org.efaps.mock.esjp.AccessCheck;
import org.efaps.mock.esjp.TriggerEvent;
import org.efaps.util.EFapsException;
import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.glassfish.hk2.utilities.FactoryDescriptorsImpl;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple;
import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple;
import com.zaxxer.hikari.HikariJNDIFactory;

import acolyte.jdbc.CompositeHandler;
import acolyte.jdbc.StatementHandler;
import acolyte.jdbc.UpdateResult;
import jakarta.transaction.TransactionManager;
/**
 * The Class AbstractTest.
 *
 * @author The eFaps Team
 */
public abstract class AbstractTest
{

    /** The Constant JDBCURL. */
    public static final String JDBCURL = "jdbc:acolyte:anything-you-want?handler=my-handler-id";

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTest.class);

    /**
     * Prepare the Test Suite.
     *
     * @throws StartupException the startup exception
     */
    @BeforeSuite
    public void prepareSuite()
        throws StartupException
    {
        final var factory = ServiceLocatorFactory.getInstance();
        final var locator = factory.create("eFaps-Core");
        final var dcs = locator.getService(DynamicConfigurationService.class);
        final var dynConfig = dcs.createDynamicConfiguration();

        final DescriptorImpl retVal = new DescriptorImpl();

        retVal.addAdvertisedContract(AbstractDatabase.class.getName());
        retVal.setImplementation(MockDatabase.class.getName());
        retVal.setScope("jakarta.inject.Singleton");
        dynConfig.bind(retVal);

        final DescriptorImpl retVal2 = new DescriptorImpl();
        retVal2.addAdvertisedContract(TransactionManager.class.getName());
        retVal2.setImplementation(TransactionManagerImple.class.getName());
       // retVal.setScope("org.glassfish.api.PerLookup");
        dynConfig.bind(retVal2);


        /**
        final DescriptorImpl retVal3 = new DescriptorImpl();
        retVal3.addAdvertisedContract(DataSource.class.getName());
        retVal3.setImplementation(DatasourceProvider.class.getName());
        retVal3.setDescriptorType(DescriptorType.PROVIDE_METHOD);
        retVal3.setScope("jakarta.inject.Singleton");
        dynConfig.bind(retVal3);
**/

        final DescriptorImpl retVal4 = new DescriptorImpl();
        retVal4.addAdvertisedContract(Factory.class.getName());
        retVal4.setImplementation(DatasourceProvider.class.getName());

        final DescriptorImpl retVal5 = new DescriptorImpl();
        retVal5.addAdvertisedContract(DataSource.class.getName());
        retVal5.setImplementation(DatasourceProvider.class.getName());
        retVal5.setDescriptorType(DescriptorType.PROVIDE_METHOD);
        final var fac = new FactoryDescriptorsImpl(retVal4, retVal5);

        dynConfig.bind(fac);



        new IOCMockRestModule().bind(dynConfig);
        dynConfig.commit();
        Person.builder()
            .withId(1L)
            .withName("Administrator")
            .build();

        Field[] fields = IDataModel.class.getDeclaredFields();
        fields = ArrayUtils.addAll(fields, Mocks.class.getDeclaredFields());
        for (final Field f : fields) {
            if (Modifier.isStatic(f.getModifiers())) {
                try {
                    f.get(null);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    LOG.error("Catched", e);
                }
            }
        }

        final StatementHandler handler = new CompositeHandler().withQueryDetection("^ select ")
                        .withQueryHandler(EFapsQueryHandler.get())
                        .withUpdateHandler((_sql, _parameters) -> {
                            EFapsQueryHandler.get().apply(_sql, _parameters);
                            return UpdateResult.One;
                        });
        acolyte.jdbc.Driver.register("my-handler-id", handler);

        final Map<String, String> connectionProperties = new HashMap<>();
        connectionProperties.put("jdbcUrl", JDBCURL);
        final Map<String, String> eFapsProperties = new HashMap<>();
        StartupDatabaseConnection.startup(MockDatabase.class.getName(), HikariJNDIFactory.class.getName(),
                        connectionProperties, TransactionManagerImple.class.getName(),
                        TransactionSynchronizationRegistryImple.class.getName(), eFapsProperties);

        AppAccessHandler.init(null, Collections.emptySet());
    }

    /**
     * Open context.
     *
     * @throws EFapsException the eFaps exception
     */
    @BeforeMethod
    public void openContext()
        throws EFapsException
    {
        Context.begin("Administrator");
    }

    /**
     * Close context.
     *
     * @throws EFapsException the eFaps exception
     */
    @AfterMethod
    public void closeContext()
        throws EFapsException
    {
        Context.commit();
        EFapsQueryHandler.get().cleanUp();
        AccessCheck.RESULTS.clear();
        TriggerEvent.RESULTS.clear();
    }

    public static class IOCMockRestModule extends AbstractBinder {

        @Override
        protected void configure()
        {
            bind(20000).to(Integer.class).named("transactionManagerTimeOut");

        }
    }
}
