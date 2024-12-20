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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.efaps.admin.datamodel.Type;
import org.efaps.admin.program.esjp.EFapsClassLoader;
import org.efaps.ci.CIAdminCommon;
import org.efaps.db.MultiPrintQuery;
import org.efaps.db.QueryBuilder;
import org.efaps.db.SelectBuilder;
import org.efaps.util.EFapsException;
import org.joda.time.DateTime;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.SchedulerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scheduler for quartz used to get the triggers and esjps for the jobs from the
 * eFaps Database.
 *
 * @author The eFaps Team
 */
public class QuartzSchedulerPlugin
    implements SchedulerPlugin
{

    private static final Logger LOG = LoggerFactory.getLogger(QuartzJobListener.class);

    private final List<Pair<JobDetail, Trigger>> jobs = new ArrayList<>();

    private Scheduler scheduler;

    /**
     * On initialization the triggers and related esjp are loaded from the eFaps
     * Database.
     *
     * @see org.quartz.spi.SchedulerPlugin#initialize(java.lang.String,
     *      org.quartz.Scheduler)
     * @param _name Name of the scheduler
     * @param _scheduler scheduler
     * @param _loadHelper The classLoadHelper the <code>SchedulerFactory</code>
     *            is actually using
     * @throws SchedulerException on error
     */
    @Override
    public void initialize(final String _name,
                           final Scheduler _scheduler,
                           final ClassLoadHelper _loadHelper)
        throws SchedulerException
    {
        this.scheduler = _scheduler;
        _scheduler.getListenerManager().addJobListener(new QuartzJobListener());
        try {
            final QueryBuilder queryBldr = new QueryBuilder(CIAdminCommon.QuartzTriggerAbstract);
            final MultiPrintQuery multi = queryBldr.getPrint();
            multi.addAttribute(CIAdminCommon.QuartzTriggerAbstract.Type,
                            CIAdminCommon.QuartzTriggerAbstract.Name,
                            CIAdminCommon.QuartzTriggerAbstract.Parameter1,
                            CIAdminCommon.QuartzTriggerAbstract.Parameter2,
                            CIAdminCommon.QuartzTriggerAbstract.Parameter3);
            final SelectBuilder sel = new SelectBuilder().linkto(CIAdminCommon.QuartzTriggerAbstract.ESJPLink)
                            .file().label();
            multi.addSelect(sel);
            multi.execute();
            while (multi.next()) {
                final Type type = multi.<Type>getAttribute(CIAdminCommon.QuartzTriggerAbstract.Type);
                final String name = multi.<String>getAttribute(CIAdminCommon.QuartzTriggerAbstract.Name);
                final Integer para1 = multi.<Integer>getAttribute(CIAdminCommon.QuartzTriggerAbstract.Parameter1);
                final Integer para2 = multi.<Integer>getAttribute(CIAdminCommon.QuartzTriggerAbstract.Parameter2);
                final Integer para3 = multi.<Integer>getAttribute(CIAdminCommon.QuartzTriggerAbstract.Parameter3);
                final String esjp = multi.<String>getSelect(sel);
                Trigger trigger = null;
                if (type.isKindOf(CIAdminCommon.QuartzTriggerSecondly.getType())) {
                    trigger = TriggerBuilder.newTrigger()
                                    .withIdentity(name)
                                    .withSchedule(para2 > 0 ? SimpleScheduleBuilder.simpleSchedule()
                                                    .withIntervalInSeconds(para1)
                                                    .withRepeatCount(para2)
                                                    : SimpleScheduleBuilder.repeatSecondlyForever(para1))
                                    .build();
                } else if (type.isKindOf(CIAdminCommon.QuartzTriggerMinutely.getType())) {
                    trigger = TriggerBuilder.newTrigger()
                                    .withIdentity(name)
                                    .withSchedule(para2 > 0 ? SimpleScheduleBuilder.simpleSchedule()
                                                    .withIntervalInMinutes(para1)
                                                    .withRepeatCount(para2)
                                                    : SimpleScheduleBuilder.repeatMinutelyForever(para1))
                                    .startAt(new DateTime().plusMinutes(para3).toDate()).build();
                } else if (type.isKindOf(CIAdminCommon.QuartzTriggerHourly.getType())) {
                    trigger = TriggerBuilder.newTrigger()
                                    .withIdentity(name)
                                    .withSchedule(para2 > 0 ? SimpleScheduleBuilder.simpleSchedule()
                                                    .withIntervalInHours(para1)
                                                    .withRepeatCount(para2)
                                                    : SimpleScheduleBuilder.repeatHourlyForever(para1))
                                    .build();
                } else if (type.isKindOf(CIAdminCommon.QuartzTriggerDaily.getType())) {
                    trigger = TriggerBuilder.newTrigger()
                                    .withIdentity(name)
                                    .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(para1, para2))
                                    .build();
                } else if (type.isKindOf(CIAdminCommon.QuartzTriggerWeekly.getType())) {
                    trigger = TriggerBuilder.newTrigger()
                                    .withIdentity(name)
                                    .withSchedule(CronScheduleBuilder.weeklyOnDayAndHourAndMinute(para1, para2, para3))
                                    .build();
                } else if (type.isKindOf(CIAdminCommon.QuartzTriggerMonthly.getType())) {
                    trigger = TriggerBuilder.newTrigger()
                                    .withIdentity(name)
                                    .withSchedule(CronScheduleBuilder.monthlyOnDayAndHourAndMinute(para1, para2, para3))
                                    .build();
                }
                @SuppressWarnings("unchecked") final Class<? extends Job> clazz = (Class<? extends Job>) Class.forName(
                                esjp, false,
                                EFapsClassLoader.getInstance());
                // class must be instantiated to force that related esjps are
                // also loaded here
                clazz.getConstructor().newInstance();

                final JobDetail jobDetail = JobBuilder.newJob(clazz)
                                .withIdentity(name + "_" + esjp, Quartz.QUARTZGROUP).build();
                if (trigger != null) {
                    jobs.add(Pair.of(jobDetail, trigger));
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                        | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
                        | SecurityException e) {
            throw new SchedulerException(e);
        } catch (final EFapsException e) {
            throw new SchedulerException(e);
        }
    }

    /**
     * @see org.quartz.spi.SchedulerPlugin#shutdown()
     */
    @Override
    public void shutdown()
    {
        // nothing must be done here
    }

    /**
     * @see org.quartz.spi.SchedulerPlugin#start()
     */
    @Override
    public void start()
    {
        for (final var job : jobs) {
            try {
                if (!scheduler.checkExists(job.getLeft().getKey())) {
                    scheduler.scheduleJob(job.getLeft(), job.getRight());
                }
            } catch (final SchedulerException e) {
                LOG.error("Catched error on adding jobs to scheduler", e);
            }
        }

    }
}
