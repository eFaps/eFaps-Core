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

import java.util.Properties;

import org.eclipse.microprofile.config.ConfigProvider;
import org.efaps.admin.EFapsSystemConfiguration;
import org.efaps.admin.KernelSettings;
import org.efaps.message.MessageStatusHolder;
import org.efaps.util.EFapsException;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.simpl.SimpleJobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quartz for eFaps.
 *
 * @author The eFaps Team
 */
public final class Quartz
{

    /**
     * Quartz Group Name.
     */
    public static final String QUARTZGROUP = "eFapsQuartzGroup";

    /**
     * Logging instance used in this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(Quartz.class);

    /**
     * Contains the instance of this singelton.
     */
    private static Quartz QUARTZ;

    /**
     * Scheduler used.
     */
    private Scheduler scheduler;

    private final boolean active;

    /**
     * Private Constructor.
     */
    private Quartz()
    {
        final var config = ConfigProvider.getConfig();
        active = config.getValue("core.quartz.active", Boolean.class);
    }

    /**
     * Method to initialize the Quartz.
     *
     * @throws EFapsException on error
     */
    public static void initialize()
        throws EFapsException
    {
        Quartz.QUARTZ = new Quartz();
        LOG.info("Quartz activated: {}", Quartz.QUARTZ.active);
        if (Quartz.QUARTZ.active) {
            try {
                // Kernel-Configuration
                final SystemConfiguration config = EFapsSystemConfiguration.get();
                final Properties props = config.getAttributeValueAsProperties(KernelSettings.QUARTZPROPS);

                final StdSchedulerFactory schedFact = new StdSchedulerFactory();
                // props.put(StdSchedulerFactory.PROP_SCHED_WRAP_JOB_IN_USER_TX,
                // "true");
                props.put(StdSchedulerFactory.PROP_THREAD_POOL_CLASS, "org.quartz.simpl.SimpleThreadPool");
                props.put(StdSchedulerFactory.PROP_SCHED_JOB_FACTORY_CLASS, SimpleJobFactory.class.getName());
                props.put("org.quartz.plugin.jobInitializer.class", "org.efaps.admin.common.QuartzSchedulerPlugin");

                if (!props.containsKey(StdSchedulerFactory.PROP_SCHED_MAKE_SCHEDULER_THREAD_DAEMON)) {
                    props.put(StdSchedulerFactory.PROP_SCHED_MAKE_SCHEDULER_THREAD_DAEMON, "true");
                }
                if (!props.containsKey(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME)) {
                    props.put(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME, "eFapsScheduler");
                }
                if (!props.containsKey("org.quartz.threadPool.threadCount")) {
                    props.put("org.quartz.threadPool.threadCount", "2");
                }

                if (!props.containsKey("org.quartz.plugin.triggHistory.class")) {
                    props.put("org.quartz.plugin.triggHistory.class",
                                    "org.quartz.plugins.history.LoggingTriggerHistoryPlugin");
                    props.put("org.quartz.plugin.triggHistory.triggerFiredMessage",
                                    "Trigger {1}.{0} fired job {6}.{5} at: {4, date, HH:mm:ss MM/dd/yyyy}");
                    props.put("org.quartz.plugin.triggHistory.triggerCompleteMessage",
                                    "Trigger {1}.{0} completed firing job {6}.{5} at {4, date, HH:mm:ss MM/dd/yyyy}.");
                }
                Quartz.LOG.info("Sheduling Quartz with properties {}", props);

                schedFact.initialize(props);
                Quartz.QUARTZ.scheduler = schedFact.getScheduler("eFapsScheduler");
                if (Quartz.QUARTZ.scheduler != null) {
                    Quartz.QUARTZ.scheduler.shutdown();
                }
                Quartz.QUARTZ.scheduler = schedFact.getScheduler();

                if (config.getAttributeValueAsBoolean(KernelSettings.MSGTRIGGERACTIVE)) {
                    final int interval = config.getAttributeValueAsInteger(KernelSettings.MSGTRIGGERINTERVAL);
                    final Trigger trigger = TriggerBuilder.newTrigger()
                                    .withIdentity("SystemMessageTrigger")
                                    .withSchedule(SimpleScheduleBuilder
                                                    .repeatMinutelyForever(interval > 0 ? interval : 1))
                                    .build();

                    JobDetail jobDetail = Quartz.QUARTZ.scheduler.getJobDetail(new JobKey("SystemMessage",
                                    Quartz.QUARTZGROUP));
                    if (jobDetail == null) {
                        jobDetail = JobBuilder.newJob(MessageStatusHolder.class)
                                        .withIdentity("SystemMessage", Quartz.QUARTZGROUP).build();
                        Quartz.QUARTZ.scheduler.scheduleJob(jobDetail, trigger);
                    } else {
                        Quartz.QUARTZ.scheduler.rescheduleJob(
                                        new TriggerKey("SystemMessageTrigger", Quartz.QUARTZGROUP),
                                        trigger);
                    }
                }
                Quartz.QUARTZ.scheduler.start();
            } catch (final SchedulerException e) {
                throw new EFapsException(Quartz.class, "Quartz.SchedulerException", e);
            }
        }
    }

    /**
     * Getter method for the instance variable {@link #QUARTZ}.
     *
     * @return value of instance variable {@link #QUARTZ}
     * @throws EFapsException on error
     */
    public static Quartz getQuartz()
        throws EFapsException
    {
        if (Quartz.QUARTZ == null) {
            Quartz.initialize();
        }
        return Quartz.QUARTZ;
    }

    /**
     * ShutDown Quartz.
     */
    public static void shutDown()
    {
        if (Quartz.QUARTZ != null && Quartz.QUARTZ.scheduler != null) {
            try {
                Quartz.QUARTZ.scheduler.shutdown();
            } catch (final SchedulerException e) {
                Quartz.LOG.error("Problems on shutdown of QuartsSheduler", e);
            }
        }
    }

    public static void reset()
    {
        if (Quartz.QUARTZ != null && Quartz.QUARTZ.scheduler != null) {
            try {
                Quartz.QUARTZ.scheduler.clear();
            } catch (final SchedulerException e) {
                Quartz.LOG.error("Problems on clear of QuartsSheduler", e);
            }
        }
    }
}
