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

package org.efaps.rest;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.KernelSettings;
import org.efaps.admin.common.SystemConfiguration;
import org.efaps.db.Context;
import org.efaps.update.schema.program.esjp.ESJPCompiler;
import org.efaps.update.schema.program.jasperreport.JasperReportCompiler;
import org.efaps.update.schema.program.staticsource.CSSCompiler;
import org.efaps.update.schema.program.staticsource.JavaScriptCompiler;
import org.efaps.update.schema.program.staticsource.WikiCompiler;
import org.efaps.update.util.InstallationException;
import org.efaps.util.EFapsException;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

/**
 * Rest API to compile the different program froms eFaps.
 *
 * @author The eFaps Team
 *
 */
@Path("/compile")
public class Compile
    extends AbstractRest
{

    /**
     * Called to compile java, css etc.
     *
     * @param _type type tobe compiled
     * @return Response
     */
    @GET
    public Response compile(@QueryParam("type") final String _type)
    {
        boolean success = false;
        try {
            if (hasAccess()) {
                AbstractRest.LOG.info("===Starting Compiler via REST===");
                if ("java".equalsIgnoreCase(_type)) {
                    AbstractRest.LOG.info("==Compiling Java==");
                    new ESJPCompiler(getClassPathElements()).compile(null, false);
                } else if ("css".equalsIgnoreCase(_type)) {
                    AbstractRest.LOG.info("==Compiling CSS==");
                    new CSSCompiler().compile();
                } else if ("js".equalsIgnoreCase(_type)) {
                    AbstractRest.LOG.info("==Compiling Javascript==");
                    new JavaScriptCompiler().compile();
                } else if ("wiki".equalsIgnoreCase(_type)) {
                    AbstractRest.LOG.info("==Compiling Wiki==");
                    new WikiCompiler().compile();
                } else if ("jasper".equalsIgnoreCase(_type)) {
                    AbstractRest.LOG.info("==Compiling JasperReports==");
                    new JasperReportCompiler(getClassPathElements()).compile();
                }
                success = true;
                AbstractRest.LOG.info("===Ending Compiler via REST===");
            } else {
                AbstractRest.LOG.error("Compile Request from User without necessary accessrights {}",
                                Context.getThreadContext().getPerson().getName());
            }
        } catch (final InstallationException e) {
            AbstractRest.LOG.error("InstallationException", e);
        } catch (final EFapsException e) {
            AbstractRest.LOG.error("EFapsException", e);
        }
        return success ? Response.ok().build() : Response.noContent().build();
    }

    /**
     * @return list of classpath elements
     * @throws EFapsException on error
     */
    public static List<String> getClassPathElements()
        throws EFapsException
    {
        final List<String> ret = new ArrayList<>();
        final SystemConfiguration config = EFapsSystemConfiguration.get();
        final String paths = config.getAttributeValue(KernelSettings.CLASSPATH);

        if (paths != null) {
            final File folder = new File(paths);
            File[] files = null;
            if (folder.isDirectory()) {
                files = folder.listFiles((FilenameFilter) (_dir,
                                                           _name) -> {
                    final boolean ret1;
                    if (new File(_dir, _name).isDirectory()) {
                        ret1 = false;
                    } else {
                        final String name = _name.toLowerCase();
                        ret1 = name.endsWith(".jar");
                    }
                    return ret1;
                });
                for (final File file : files) {
                    ret.add(file.getAbsolutePath());
                }
            }
        } else {
            AbstractRest.LOG.info("==ClassPath is not configurated==");
        }
        return ret;
    }
}
