/* 
 * $Id$
 * $URL$
 *
 * ====================================================================
 * Ikasan Enterprise Integration Platform
 * 
 * Distributed under the Modified BSD License.
 * Copyright notice: The copyright for this software and a full listing 
 * of individual contributors are as shown in the packaged copyright.txt 
 * file. 
 * 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *
 *  - Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution.
 *
 *  - Neither the name of the ORGANIZATION nor the names of its contributors may
 *    be used to endorse or promote products derived from this software without 
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 */
package org.ikasan.component.endpoint.quartz.consumer;

import org.ikasan.component.endpoint.quartz.recovery.service.ScheduledJobRecoveryService;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

/**
 * This test class supports the <code>Consumer</code> class.
 *
 * @author Ikasan Development Team
 */
@DisallowConcurrentExecution
@SuppressWarnings("unchecked")
public class CorrelatingScheduledConsumer<T> extends ScheduledConsumer<T> implements Job
{
    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(CorrelatingScheduledConsumer.class);
    public static final String CORRELATION_ID = "correlationId";
    public static final String EMPTY_CORRELATION_ID = "EMPTY_CORRELATION_ID";

    private Set<Trigger> triggers = null;

    /**
     * Constructor
     *
     * @param scheduler the Quartz Scheduler
     */
    public CorrelatingScheduledConsumer(Scheduler scheduler)
    {
        super(scheduler);
    }

    /**
     * Constructor
     * @param scheduler implementing Quartz
     * @param scheduledJobRecoveryService for when the scheduler crashes
     */
    public CorrelatingScheduledConsumer(Scheduler scheduler, ScheduledJobRecoveryService scheduledJobRecoveryService)
    {
        super(scheduler, scheduledJobRecoveryService);
    }

    /**
     * Start the underlying tech
     */
    public void start()
    {
        try
        {
            JobKey jobkey = getJobDetail().getKey();
            String jobName = jobkey.getName();
            if(getConfiguration().getJobName() != null)
            {
                jobName = getConfiguration().getJobName();
            }

            String jobGroupName = jobkey.getGroup();
            if(getConfiguration().getJobGroupName() != null)
            {
                jobGroupName = getConfiguration().getJobGroupName();
            }

            List<String> cronExpressions = consumerConfiguration.getConsolidatedCronExpressions();
            triggers = new HashSet<>(cronExpressions.size());

            List<String> correlatingIdentifiers = ((CorrelatedScheduledConsumerConfiguration)getConfiguration()).getCorrelatingIdentifiers();
            // get all configured business expressions (expression and expressions) as a single list
            // and create uniquely named triggers for each
            for(String cronExpression:cronExpressions) {
                if(correlatingIdentifiers.isEmpty()) {
                    this.populateTrigger(jobName, jobGroupName, EMPTY_CORRELATION_ID, cronExpression);
                }
                else {
                    for (String rootPlanCorrelationId : correlatingIdentifiers) {
                        this.populateTrigger(jobName, jobGroupName, rootPlanCorrelationId, cronExpression);
                    }
                }
            }

            if(getConfiguration().getPassthroughProperties() != null)
            {
                for(Trigger trigger: triggers)
                {
                    for(Map.Entry<String,String> entry:getConfiguration().getPassthroughProperties().entrySet())
                    {
                        trigger.getJobDataMap().put(entry.getKey(), entry.getValue());
                    }
                }
            }

            StringBuilder logStringBuilder = new StringBuilder();
            for(Trigger trigger: triggers)
            {
                logStringBuilder.append("Job [" + trigger.getKey() + " with firetime [" + trigger.getNextFireTime() + "] description [" + trigger.getDescription() + "]; ");
            }

            scheduleJobTriggers(getJobDetail(), triggers, true);
            logger.info("Started scheduled consumer for flow ["
                + jobkey.getName()
                + "] module [" + jobkey.getGroup()
                + "] " + logStringBuilder);
        }
        catch (SchedulerException | ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void populateTrigger(String jobName, String jobGroupName, String rootPlanCorrelationId, String cronExpression) throws ParseException {
        String jobNameIteration = jobName + "_" + rootPlanCorrelationId + "_" + cronExpression.hashCode();
        TriggerBuilder<Trigger> triggerBuilder = newTriggerFor(jobNameIteration, jobGroupName);

        // check if last invocation was successful, if so schedule the business trigger otherwise create a persistent recovery trigger
        Trigger trigger;
        if (consumerConfiguration.isPersistentRecovery() && scheduledJobRecoveryService
            .isRecoveryRequired(jobNameIteration, jobGroupName, consumerConfiguration.getRecoveryTolerance())) {
            // if unsuccessful, schedule a callback immediately if within tolerance of recovery
            trigger = newTriggerFor(jobNameIteration, jobGroupName)
                .startNow()
                .withSchedule(simpleSchedule().withMisfireHandlingInstructionFireNow()).build();
            trigger.getJobDataMap().put(PERSISTENT_RECOVERY, PERSISTENT_RECOVERY);
        } else {
            // if successful then just add business trigger
            trigger = getBusinessTrigger(triggerBuilder, cronExpression);
        }

        trigger.getJobDataMap().put(CRON_EXPRESSION, cronExpression);
        trigger.getJobDataMap().put(CORRELATION_ID, rootPlanCorrelationId);
        triggers.add(trigger);
    }

    @Override
    public String toString() {
        return "CorrelatingScheduledConsumer{" +
            "triggers=" + triggers +
            '}';
    }
}
