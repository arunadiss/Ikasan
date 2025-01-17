package org.ikasan.spec.scheduled.job.service;

import org.ikasan.spec.scheduled.instance.model.GlobalEventJobInstance;

public interface GlobalEventService {

    /**
     * Raise a global event job.
     *
     * @param globalEventJobInstance
     * @param contextInstanceId
     */
    void raiseGlobalEventJob(GlobalEventJobInstance globalEventJobInstance, String contextInstanceId, String userName);

    /**
     * Raise global event to all context instances
     *
     * @param globalJobName
     */
    void raiseGlobalEventJob(String globalJobName);
}
