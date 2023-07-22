/*
 * Copyright 2003 - 2023 The eFaps Team
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

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.efaps.admin.user.Company;
import org.efaps.db.Context;
import org.efaps.json.reply.ContextReply;
import org.efaps.json.reply.ErrorReply;
import org.efaps.util.EFapsException;
import org.efaps.util.UUIDUtil;
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
@Path("/context")
public class RestContext
    extends AbstractRest
{
    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RestContext.class);

    /**
     * Confirm.
     *
     * @return the response
     */
    @Path("confirm")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    @SuppressWarnings("checkstyle:illegalcatch")
    public Response confirm()
    {
        Response ret = null;
        try {
            final Context context = Context.getThreadContext();
            final ContextReply reply = new ContextReply()
                            .setUserName(context.getPerson().getName())
                            .setUserLastName(context.getPerson().getLastName())
                            .setUserFirstName(context.getPerson().getFirstName())
                            .setCompanyName(context.getCompany().getName())
                            .setLocale(context.getLocale().toString());

            ret = Response.ok().type(MediaType.APPLICATION_JSON).entity(getJSONReply(reply)).build();
        } catch (final Exception e) {
            RestContext.LOG.error("Error processing data.", e);
            final ErrorReply reply = new ErrorReply()
                            .setError(e.getClass().getName())
                            .setMessage(e.getMessage())
                            .setStacktrace(ExceptionUtils.getStackTrace(e));
            ret = Response.serverError().type(MediaType.APPLICATION_JSON).entity(getJSONReply(reply)).build();
        }
        return ret;
    }

    /**
     * Sets the company.
     *
     * @param _companyStr the _company str
     * @return the response
     */
    @Path("setCompany")
    @GET
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    @SuppressWarnings("checkstyle:illegalcatch")
    public Response setCompany(@QueryParam("company") final String _companyStr)
    {
        try {
            final Company company;
            if (UUIDUtil.isUUID(_companyStr)) {
                company = Company.get(UUID.fromString(_companyStr));
            } else if (StringUtils.isNumeric(_companyStr)) {
                company = Company.get(Long.parseLong(_companyStr));
            } else {
                company = Company.get(_companyStr);
            }
            if (company != null && company.hasChildPerson(Context.getThreadContext().getPerson())) {
                Context.getThreadContext().setUserAttribute(Context.CURRENTCOMPANY, String.valueOf(company.getId()));
                Context.getThreadContext().getUserAttributes().storeInDb();
                Context.getThreadContext().setCompany(company);
            }
        } catch (final NumberFormatException | EFapsException e) {
            RestContext.LOG.error("Catched error", e);
        }
        return confirm();
    }
}
