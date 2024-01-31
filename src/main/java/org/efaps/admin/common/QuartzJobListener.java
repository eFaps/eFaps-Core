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

import org.efaps.db.Context;
import org.efaps.util.EFapsException;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.listeners.JobListenerSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuartzJobListener
    extends JobListenerSupport
{

    private static final Logger LOG = LoggerFactory.getLogger(QuartzJobListener.class);

    @Override
    public String getName()
    {
        return "eFaps-QuartzJobListener";
    }

    @Override
    public void jobToBeExecuted(final JobExecutionContext context)
    {
        super.jobToBeExecuted(context);
        try {
            //QuartzTrigger
            Context.begin("df2f02a7-c556-49ad-b019-e13db66e1cbf");
        } catch (final EFapsException e) {
            LOG.error("Catched", e);
        }
    }

    @Override
    public void jobWasExecuted(final JobExecutionContext context,
                               final JobExecutionException jobException)
    {
        super.jobWasExecuted(context, jobException);
        try {
            if (jobException != null) {
                Context.rollback();
            } else {
                Context.commit();
            }
        } catch (final EFapsException e) {
            LOG.error("Catched", e);
        }
    }

}
