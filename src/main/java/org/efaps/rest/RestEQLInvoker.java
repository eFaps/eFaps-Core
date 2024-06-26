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
package org.efaps.rest;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.efaps.db.Insert;
import org.efaps.eql.EQLInvoker;
import org.efaps.eql.InvokerUtil;
import org.efaps.eql.JSONCI;
import org.efaps.eql.JSONData;
import org.efaps.eql.stmt.ICIPrintStmt;
import org.efaps.eql.stmt.IDeleteStmt;
import org.efaps.eql.stmt.IEQLStmt;
import org.efaps.eql.stmt.IExecStmt;
import org.efaps.eql.stmt.IInsertStmt;
import org.efaps.eql.stmt.IPrintStmt;
import org.efaps.eql.stmt.IUpdateStmt;
import org.efaps.json.ci.AbstractCI;
import org.efaps.json.data.DataList;
import org.efaps.json.reply.DeleteEQLReply;
import org.efaps.json.reply.ErrorReply;
import org.efaps.json.reply.ExecuteEQLReply;
import org.efaps.json.reply.InsertEQLReply;
import org.efaps.json.reply.UpdateEQLReply;
import org.efaps.util.EFapsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * TODO comment!
 *
 * @author The eFaps Team
 */
@Path("/eql")
public class RestEQLInvoker
    extends AbstractRest
{

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RestEQLInvoker.class);

    /**
     * Prints the.
     *
     * @param _origin the origin
     * @param _stmt the stmt
     * @return not implemented.
     */
    @Path("print")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    public Response print(@QueryParam("origin") final String origin,
                          @QueryParam("stmt") final String stmt)
    {
        Response ret = null;
        if (StringUtils.isEmpty(origin) || StringUtils.isEmpty(stmt)) {
            final ErrorReply reply = new ErrorReply()
                            .setError("Missing query parameter")
                            .setMessage("query parameters origin and stmt are required");
            ret = Response.status(Response.Status.PRECONDITION_FAILED).type(MediaType.APPLICATION_JSON)
                            .entity(getJSONReply(reply)).build();
        } else {
            // only permit print on this url
            try {
                final EQLInvoker invoker = InvokerUtil.getInvoker();
                final IEQLStmt eqlStmt = invoker.invoke(stmt);
                if (invoker.getSyntaxErrors().isEmpty() && eqlStmt instanceof IPrintStmt) {
                    registerEQLStmt(origin, stmt);
                    final DataList datalist = JSONData.getDataList((IPrintStmt) eqlStmt);
                    ret = Response.ok().type(MediaType.APPLICATION_JSON).entity(getJSONReply(datalist)).build();
                } else if (invoker.getSyntaxErrors().isEmpty() && eqlStmt instanceof ICIPrintStmt) {
                    registerEQLStmt(origin, stmt);
                    final AbstractCI<?> ci = JSONCI.getCI((ICIPrintStmt) eqlStmt);
                    ret = Response.ok().type(MediaType.APPLICATION_JSON).entity(getJSONReply(ci)).build();
                } else {
                    final StringBuilder error = new StringBuilder();
                    for (final String syntaxError : invoker.getSyntaxErrors()) {
                        LOG.warn(syntaxError);
                        error.append(syntaxError).append("\n");
                    }
                    final ErrorReply reply = new ErrorReply()
                                    .setError("EQL Syntax Error")
                                    .setMessage(error.toString());
                    ret = Response.serverError().type(MediaType.APPLICATION_JSON).entity(getJSONReply(reply)).build();
                }
            } catch (final Exception e) {
                LOG.error("Error processing data.", e);
                final ErrorReply reply = new ErrorReply()
                                .setError(e.getClass().getName())
                                .setMessage(e.getMessage())
                                .setStacktrace(ExceptionUtils.getStackTrace(e));
                ret = Response.serverError().type(MediaType.APPLICATION_JSON).entity(getJSONReply(reply)).build();
            }
        }
        return ret;
    }

    /**
     * Update.
     *
     * @param _origin the origin
     * @param _stmt the stmt
     * @return not implemented.
     */
    @Path("update")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    public Response update(@QueryParam("origin") final String origin,
                           @QueryParam("stmt") final String stmt)
    {
        Response ret = null;
        if (StringUtils.isEmpty(origin) || StringUtils.isEmpty(stmt)) {
            final ErrorReply reply = new ErrorReply()
                            .setError("Missing query parameter")
                            .setMessage("query parameters origin and stmt are required");
            ret = Response.status(Response.Status.PRECONDITION_FAILED).type(MediaType.APPLICATION_JSON)
                            .entity(getJSONReply(reply)).build();
        } else {
            // only permit updates on this url
            try {
                final EQLInvoker invoker = InvokerUtil.getInvoker();
                final IEQLStmt eqlStmt = invoker.invoke(stmt);
                if (invoker.getSyntaxErrors().isEmpty() && eqlStmt instanceof IUpdateStmt) {
                    registerEQLStmt(origin, stmt);
                    final int modified = ((IUpdateStmt) eqlStmt).execute();
                    ret = Response.ok().type(MediaType.APPLICATION_JSON).entity(
                                    getJSONReply(new UpdateEQLReply().setModified(modified))).build();
                } else {
                    final StringBuilder error = new StringBuilder();
                    for (final String syntaxError : invoker.getSyntaxErrors()) {
                        LOG.warn(syntaxError);
                        error.append(syntaxError).append("\n");
                    }
                    final ErrorReply reply = new ErrorReply()
                                    .setError("EQL Syntax Error")
                                    .setMessage(error.toString());
                    ret = Response.serverError().type(MediaType.APPLICATION_JSON).entity(getJSONReply(reply)).build();
                }
            } catch (final Exception e) {
                LOG.error("Error processing data.", e);
                final ErrorReply reply = new ErrorReply()
                                .setError(e.getClass().getName())
                                .setMessage(e.getMessage())
                                .setStacktrace(ExceptionUtils.getStackTrace(e));
                ret = Response.serverError().type(MediaType.APPLICATION_JSON).entity(getJSONReply(reply)).build();
            }
        }
        return ret;
    }

    /**
     * Insert.
     *
     * @param _origin the origin
     * @param _stmt the stmt
     * @return not implemented.
     */
    @Path("insert")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    public Response insert(@QueryParam("origin") final String origin,
                           @QueryParam("stmt") final String stmt)
    {
        Response ret = null;
        if (StringUtils.isEmpty(origin) || StringUtils.isEmpty(stmt)) {
            final ErrorReply reply = new ErrorReply()
                            .setError("Missing query parameter")
                            .setMessage("query parameters origin and stmt are required");
            ret = Response.status(Response.Status.PRECONDITION_FAILED).type(MediaType.APPLICATION_JSON)
                            .entity(getJSONReply(reply)).build();
        } else {
            // only permit insert on this url
            try {
                final EQLInvoker invoker = InvokerUtil.getInvoker();
                final IEQLStmt eqlStmt = invoker.invoke(stmt);
                if (invoker.getSyntaxErrors().isEmpty() && eqlStmt instanceof IInsertStmt) {
                    registerEQLStmt(origin, stmt);
                    final int modified = ((IInsertStmt) eqlStmt).execute();
                    final String instance = ((IInsertStmt) eqlStmt).getInstance();
                    ret = Response.ok().type(MediaType.APPLICATION_JSON).entity(
                                    getJSONReply(new InsertEQLReply().setInstance(instance).setModified(modified)))
                                    .build();
                } else {
                    final StringBuilder error = new StringBuilder();
                    for (final String syntaxError : invoker.getSyntaxErrors()) {
                        LOG.warn(syntaxError);
                        error.append(syntaxError).append("\n");
                    }
                    final ErrorReply reply = new ErrorReply()
                                    .setError("EQL Syntax Error")
                                    .setMessage(error.toString());
                    ret = Response.serverError().type(MediaType.APPLICATION_JSON).entity(getJSONReply(reply)).build();
                }
            } catch (final Exception e) {
                LOG.error("Error processing data.", e);
                final ErrorReply reply = new ErrorReply()
                                .setError(e.getClass().getName())
                                .setMessage(e.getMessage())
                                .setStacktrace(ExceptionUtils.getStackTrace(e));
                ret = Response.serverError().type(MediaType.APPLICATION_JSON).entity(getJSONReply(reply)).build();
            }
        }
        return ret;
    }

    /**
     * Delete.
     *
     * @param _origin the origin
     * @param _stmt the stmt
     * @return not implemented.
     */
    @Path("delete")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    public Response delete(@QueryParam("origin") final String origin,
                           @QueryParam("stmt") final String stmt)
    {
        Response ret = null;
        if (StringUtils.isEmpty(origin) || StringUtils.isEmpty(stmt)) {
            final ErrorReply reply = new ErrorReply()
                            .setError("Missing query parameter")
                            .setMessage("query parameters origin and stmt are required");
            ret = Response.status(Response.Status.PRECONDITION_FAILED).type(MediaType.APPLICATION_JSON)
                            .entity(getJSONReply(reply)).build();
        } else {
            // only permit delete on this url
            try {
                final EQLInvoker invoker = InvokerUtil.getInvoker();
                final IEQLStmt eqlStmt = invoker.invoke(stmt);
                if (invoker.getSyntaxErrors().isEmpty() && eqlStmt instanceof IDeleteStmt) {
                    registerEQLStmt(origin, stmt);
                    final int modified = ((IDeleteStmt) eqlStmt).execute();
                    ret = Response.ok().type(MediaType.APPLICATION_JSON).entity(
                                    getJSONReply(new DeleteEQLReply().setModified(modified))).build();
                } else {
                    final StringBuilder error = new StringBuilder();
                    for (final String syntaxError : invoker.getSyntaxErrors()) {
                        LOG.warn(syntaxError);
                        error.append(syntaxError).append("\n");
                    }
                    final ErrorReply reply = new ErrorReply()
                                    .setError("EQL Syntax Error")
                                    .setMessage(error.toString());
                    ret = Response.serverError().type(MediaType.APPLICATION_JSON).entity(getJSONReply(reply)).build();
                }
            } catch (final Exception e) {
                LOG.error("Error processing data.", e);
                final ErrorReply reply = new ErrorReply()
                                .setError(e.getClass().getName())
                                .setMessage(e.getMessage())
                                .setStacktrace(ExceptionUtils.getStackTrace(e));
                ret = Response.serverError().type(MediaType.APPLICATION_JSON).entity(getJSONReply(reply)).build();
            }
        }
        return ret;
    }

    /**
     * Execute.
     *
     * @param _origin the origin
     * @param _stmt the stmt
     * @return not implemented.
     */
    @Path("execute")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    public Response execute(@QueryParam("origin") final String origin,
                            @QueryParam("stmt") final String stmt)
    {
        Response ret = null;
        if (StringUtils.isEmpty(origin) || StringUtils.isEmpty(stmt)) {
            final ErrorReply reply = new ErrorReply()
                            .setError("Missing query parameter")
                            .setMessage("query parameters origin and stmt are required");
            ret = Response.status(Response.Status.PRECONDITION_FAILED).type(MediaType.APPLICATION_JSON)
                            .entity(getJSONReply(reply)).build();
        } else {
            // only permit execute on this url
            try {
                final EQLInvoker invoker = InvokerUtil.getInvoker();
                final IEQLStmt eqlStmt = invoker.invoke(stmt);
                if (invoker.getSyntaxErrors().isEmpty() && eqlStmt instanceof IExecStmt) {
                    registerEQLStmt(origin, stmt);
                    ret = Response.ok().type(MediaType.APPLICATION_JSON)
                                    .entity(getJSONReply(new ExecuteEQLReply())).build();
                } else {
                    final StringBuilder error = new StringBuilder();
                    for (final String syntaxError : invoker.getSyntaxErrors()) {
                        LOG.warn(syntaxError);
                        error.append(syntaxError).append("\n");
                    }
                    final ErrorReply reply = new ErrorReply()
                                    .setError("EQL Syntax Error")
                                    .setMessage(error.toString());
                    ret = Response.serverError().type(MediaType.APPLICATION_JSON).entity(getJSONReply(reply)).build();
                }
            } catch (final Exception e) {
                LOG.error("Error processing data.", e);
                final ErrorReply reply = new ErrorReply()
                                .setError(e.getClass().getName())
                                .setMessage(e.getMessage())
                                .setStacktrace(ExceptionUtils.getStackTrace(e));
                ret = Response.serverError().type(MediaType.APPLICATION_JSON).entity(getJSONReply(reply)).build();
            }
        }
        return ret;
    }

    /**
     * Register eql stmt.
     *
     * @param _origin the origin
     * @param _stmt the stmt
     * @throws EFapsException on error
     */
    protected void registerEQLStmt(final String _origin,
                                   final String _stmt)
        throws EFapsException
    {
        // Common_HistoryEQL
        final Insert insert = new Insert(UUID.fromString("c96c63b5-2d4c-4bf9-9627-f335fd9c7a84"));
        insert.add("Origin", "REST: " + (_origin == null ? "" : _origin));
        insert.add("EQLStatement", _stmt);
        insert.execute();
    }
}
