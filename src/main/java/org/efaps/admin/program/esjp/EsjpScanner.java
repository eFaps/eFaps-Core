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
package org.efaps.admin.program.esjp;

import static org.reflections.scanners.Scanners.SubTypes;
import static org.reflections.scanners.Scanners.TypesAnnotated;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.microprofile.config.ConfigProvider;
import org.efaps.ci.CIAdminProgram;
import org.efaps.db.Checkout;
import org.efaps.db.Context;
import org.efaps.db.Instance;
import org.efaps.db.InstanceQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.util.EFapsException;
import org.reflections.Reflections;
import org.reflections.scanners.Scanner;
import org.reflections.scanners.Scanners;
import org.reflections.serializers.XmlSerializer;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.vfs.Vfs;
import org.reflections.vfs.Vfs.Dir;
import org.reflections.vfs.Vfs.File;
import org.reflections.vfs.Vfs.UrlType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to scan esjps for annotations.
 */
public class EsjpScanner
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EsjpScanner.class);

    /** The reflections store. */
    private static java.io.File REFLECTIONS;

    static {
        Vfs.setDefaultURLTypes(Collections.singletonList(new UrlType()
        {

            @Override
            public boolean matches(final URL _url)
                throws Exception
            {
                return true;
            }

            @Override
            public Dir createDir(final URL _url)
                throws Exception
            {
                return new EsjpDir();
            }
        }));
        final var config = ConfigProvider.getConfig();
        final var fileOpt = config.getOptionalValue("core.reflections.storeFile", java.io.File.class);
        if (fileOpt.isPresent()) {
            final var xmlFile = fileOpt.get();
            LOG.info("Reflection store file definition: {} --> exists: {}", xmlFile, xmlFile.exists());
            if (xmlFile.exists()) {
                REFLECTIONS = fileOpt.get();
            }
        }
    }

    @SafeVarargs
    public final Set<Class<?>> scan(final Class<? extends Annotation>... annotations)
        throws EFapsException
    {
        LOG.info("Scanning for annotations for: {}", Arrays.asList(annotations));
        final Set<Class<?>> ret = new HashSet<>();
        try {
            final Reflections reflections = evalReflections();
            for (final Class<? extends Annotation> annotation : annotations) {
                return reflections.get(
                                SubTypes.of(TypesAnnotated.with(annotation)).asClass(EFapsClassLoader.getInstance()));
            }
        } catch (final MalformedURLException e) {
            LOG.error("Catched MalformedURLException", e);
        } catch (final IOException e) {
            LOG.error("Catched IOException", e);
        }
        return ret;
    }

    @SafeVarargs
    public final Set<Class<?>> scan4SubTypes(final Class<?>... parentTypes)
        throws EFapsException
    {
        LOG.info("Scanning for subtypes of: {}", Arrays.asList(parentTypes));
        final Set<Class<?>> ret = new HashSet<>();
        try {
            final Reflections reflections = evalReflections();
            for (final Class<?> parentType : parentTypes) {
                ret.addAll(reflections.get(Scanners.SubTypes.of(parentType).asClass(EFapsClassLoader.getInstance())));
            }
            if (ret.isEmpty()) {
                LOG.warn("No subtypes found for: {}", Arrays.asList(parentTypes));
            }
        } catch (final MalformedURLException e) {
            LOG.error("Catched MalformedURLException", e);
        } catch (final IOException e) {
            LOG.error("Catched IOException", e);
         }
        return ret;
    }

    private Reflections evalReflections()
        throws EFapsException, IOException
    {
        final Reflections reflections;

        if (REFLECTIONS == null) {
            LOG.info("Scanning esjps for annotations.");
            final ConfigurationBuilder configuration = new ConfigurationBuilder()
                            .setUrls(new URL("file://"))
                            .setScanners(Scanners.SubTypes, Scanners.TypesAnnotated, Scanners.FieldsAnnotated,
                                            Scanners.MethodsAnnotated)
                            .setExpandSuperTypes(false)
                            .setParallel(false);
                            //.setClassLoaders(new ClassLoader[] { EFapsClassLoader.getInstance() });
            // in case of jboss the transaction filter is not executed
            // before the method is called therefore a Context must be
            // opened
            boolean contextStarted = false;
            if (!Context.isThreadActive()) {
                Context.begin(null, Context.Inheritance.Local);
                contextStarted = true;
            }
            reflections = new Reflections(configuration);
            save(reflections);
            if (contextStarted) {
                Context.rollback();
            }
        } else {
            LOG.info("Loading refelections result from: {}", REFLECTIONS);
            final ConfigurationBuilder configuration = new ConfigurationBuilder().setScanners(new Scanner[] {});
           // configuration.setClassLoaders(new ClassLoader[] { EFapsClassLoader.getInstance() });
            reflections = new Reflections(configuration);
            reflections.collect(REFLECTIONS, new XmlSerializer());
        }

        return reflections;
    }

    private void save(final Reflections reflections)
        throws IOException
    {
        final var config = ConfigProvider.getConfig();
        final var file = config.getOptionalValue("core.reflections.storeFile", java.io.File.class)
                        .orElse(java.io.File.createTempFile("eFapsReflections-", ".xml"));
        REFLECTIONS = reflections.save(file.getAbsolutePath(), new XmlSerializer());
    }

    public static void reset()
    {
        if (REFLECTIONS != null) {
            REFLECTIONS.delete();
            REFLECTIONS = null;
        }
    }

    public static class EsjpDir
        implements Vfs.Dir
    {

        @Override
        public String getPath()
        {
            return "";
        }

        @Override
        public Iterable<File> getFiles()
        {
            final Set<File> files = new HashSet<>();
            try {
                // check required during source-install maven target
                if (CIAdminProgram.Javaclass.getType() != null) {
                    final QueryBuilder queryBldr = new QueryBuilder(CIAdminProgram.Javaclass);
                    final InstanceQuery query = queryBldr.getQuery();
                    query.executeWithoutAccessCheck();
                    while (query.next()) {
                        final var file = new EsjpFile(query.getCurrentValue());
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Adding esjp to be scanned: {}", file.getName());
                        }
                        files.add(file);
                    }
                }
            } catch (final EFapsException e) {
                e.printStackTrace();
            }
            return files;
        }

        @Override
        public void close()
        {
            // not needed
        }
    }

    public static class EsjpFile
        implements Vfs.File
    {

        private final Instance instance;
        private InputStream in;
        private String name;

        public EsjpFile(final Instance _instance)
        {
            instance = _instance;
        }

        private void init()
        {
            if (name == null) {
                try {
                    final Checkout checkout = new Checkout(instance);
                    in = checkout.execute();
                    name = checkout.getFileName() + ".class";
                    LOG.debug("Scanned: {}", name);
                } catch (final EFapsException e) {
                    LOG.error("Catchec EFapsException", e);
                }
            }
        }

        @Override
        public String getName()
        {
            init();
            return name;
        }

        @Override
        public String getRelativePath()
        {
            init();
            return name;
        }

        @Override
        public InputStream openInputStream()
            throws IOException
        {
            init();
            return in;
        }
    }
}
