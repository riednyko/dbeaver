/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2017 Serge Rider (serge@jkiss.org)
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
package org.jkiss.dbeaver.ext.oracle.model;

import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.meta.Property;

import java.sql.ResultSet;

/**
 * Oracle scheduler job
 */
public class OracleSchedulerJob extends OracleSchemaObject {

    private static final String CAT_SETTINGS = "Settings";
    private static final String CAT_STATISTICS = "Statistics";
    private static final String CAT_EVENTS = "Events";
    private static final String CAT_ADVANCED = "Advanced";

    private String jobSubName;
    private String jobStyle;
    private String jobCreator;
    private String clientId;
    private String globalUid;
    private String programOwner;
    private String programName;
    private String jobType;
    private String jobAction;
    private long numberOfArguments;
    private String scheduleOwner;
    private String scheduleName;
    private String scheduleType;

    private String startDate;
    private String repeatInterval;
    private String eventQueueOwner;
    private String eventQueueName;
    private String eventQueueAgent;
    private String eventCondition;
    private String eventRule;
    private String fileWatcherOwner;
    private String fileWatcherName;
    private String endDate;

    private String jobClass;
    private String enabled;
    private String autoDrop;
    private String restartable;
    private String state;
    private int jobPriority;
    private long runCount;
    private long maxRuns;
    private long failureCount;
    private long maxFailures;
    private long retryCount;
    private String lastStartDate;
    private String lastRunDuration;
    private String nextRunDate;
    private String scheduleLimit;
    private String maxRunDuration;
    private String loggingLevel;
    private String stopOnWindowClose;
    private String instanceStickiness;
    private String raiseEvents;
    private String system;
    private String jobWeight;
    private String nlsEnv;
    private String source;
    private String numberOfDestinations;
    private String destinationOwner;
    private String destination;
    private String credentialOwner;
    private String credentialName;
    private String instanceId;
    private String deferredDrop;
    private String allowRunsInRestrictedMode;
    private String comments;

    protected OracleSchedulerJob(OracleSchema schema, ResultSet dbResult) {
        super(schema, JDBCUtils.safeGetString(dbResult, "JOB_NAME"), true);

        jobSubName = JDBCUtils.safeGetString(dbResult, "JOB_SUBNAME");
        jobStyle = JDBCUtils.safeGetString(dbResult, "JOB_STYLE");
        jobCreator = JDBCUtils.safeGetString(dbResult, "JOB_CREATOR");
        clientId = JDBCUtils.safeGetString(dbResult, "CLIENT_ID");
        globalUid = JDBCUtils.safeGetString(dbResult, "GLOBAL_UID");
        programOwner = JDBCUtils.safeGetString(dbResult, "PROGRAM_OWNER");
        programName = JDBCUtils.safeGetString(dbResult, "PROGRAM_NAME");
        jobType = JDBCUtils.safeGetString(dbResult, "JOB_TYPE");
        jobAction = JDBCUtils.safeGetString(dbResult, "JOB_ACTION");
        numberOfArguments = JDBCUtils.safeGetLong(dbResult, "NUMBER_OF_ARGUMENTS");
        scheduleOwner = JDBCUtils.safeGetString(dbResult, "SCHEDULE_OWNER");
        scheduleName = JDBCUtils.safeGetString(dbResult, "SCHEDULE_NAME");
        scheduleType = JDBCUtils.safeGetString(dbResult, "SCHEDULE_TYPE");
        startDate = JDBCUtils.safeGetString(dbResult, "START_DATE");
        repeatInterval = JDBCUtils.safeGetString(dbResult, "REPEAT_INTERVAL");
        eventQueueOwner = JDBCUtils.safeGetString(dbResult, "EVENT_QUEUE_OWNER");
        eventQueueName = JDBCUtils.safeGetString(dbResult, "EVENT_QUEUE_NAME");
        eventQueueAgent = JDBCUtils.safeGetString(dbResult, "EVENT_QUEUE_AGENT");
        eventCondition = JDBCUtils.safeGetString(dbResult, "EVENT_CONDITION");
        eventRule = JDBCUtils.safeGetString(dbResult, "EVENT_RULE");
        fileWatcherOwner = JDBCUtils.safeGetString(dbResult, "FILE_WATCHER_OWNER");
        fileWatcherName = JDBCUtils.safeGetString(dbResult, "FILE_WATCHER_NAME");
        endDate = JDBCUtils.safeGetString(dbResult, "END_DATE");
        jobClass = JDBCUtils.safeGetString(dbResult, "JOB_CLASS");
        enabled = JDBCUtils.safeGetString(dbResult, "ENABLED");
        autoDrop = JDBCUtils.safeGetString(dbResult, "AUTO_DROP");
        restartable = JDBCUtils.safeGetString(dbResult, "RESTARTABLE");
        state = JDBCUtils.safeGetString(dbResult, "STATE");
        jobPriority = JDBCUtils.safeGetInt(dbResult, "JOB_PRIORITY");
        runCount = JDBCUtils.safeGetLong(dbResult, "RUN_COUNT");
        maxRuns = JDBCUtils.safeGetLong(dbResult, "MAX_RUNS");
        failureCount = JDBCUtils.safeGetLong(dbResult, "FAILURE_COUNT");
        maxFailures = JDBCUtils.safeGetLong(dbResult, "MAX_FAILURES");
        retryCount = JDBCUtils.safeGetLong(dbResult, "RETRY_COUNT");
        lastStartDate = JDBCUtils.safeGetString(dbResult, "LAST_START_DATE");
        lastRunDuration = JDBCUtils.safeGetString(dbResult, "LAST_RUN_DURATION");
        nextRunDate = JDBCUtils.safeGetString(dbResult, "NEXT_RUN_DATE");
        scheduleLimit = JDBCUtils.safeGetString(dbResult, "SCHEDULE_LIMIT");
        maxRunDuration = JDBCUtils.safeGetString(dbResult, "MAX_RUN_DURATION");
        loggingLevel = JDBCUtils.safeGetString(dbResult, "LOGGING_LEVEL");
        stopOnWindowClose = JDBCUtils.safeGetString(dbResult, "STOP_ON_WINDOW_CLOSE");
        instanceStickiness = JDBCUtils.safeGetString(dbResult, "INSTANCE_STICKINESS");
        raiseEvents = JDBCUtils.safeGetString(dbResult, "RAISE_EVENTS");
        system = JDBCUtils.safeGetString(dbResult, "SYSTEM");
        jobWeight = JDBCUtils.safeGetString(dbResult, "JOB_WEIGHT");
        nlsEnv = JDBCUtils.safeGetString(dbResult, "NLS_ENV");
        source = JDBCUtils.safeGetString(dbResult, "SOURCE");
        numberOfDestinations = JDBCUtils.safeGetString(dbResult, "NUMBER_OF_DESTINATIONS");
        destinationOwner = JDBCUtils.safeGetString(dbResult, "DESTINATION_OWNER");
        destination = JDBCUtils.safeGetString(dbResult, "DESTINATION");
        credentialOwner = JDBCUtils.safeGetString(dbResult, "CREDENTIAL_OWNER");
        credentialName = JDBCUtils.safeGetString(dbResult, "CREDENTIAL_NAME");
        instanceId = JDBCUtils.safeGetString(dbResult, "INSTANCE_ID");
        deferredDrop = JDBCUtils.safeGetString(dbResult, "DEFERRED_DROP");
        allowRunsInRestrictedMode = JDBCUtils.safeGetString(dbResult, "ALLOW_RUNS_IN_RESTRICTED_MODE");
        comments = JDBCUtils.safeGetString(dbResult, "COMMENTS");
    }

    @Property(viewable = true, order = 10)
    public String getJobSubName() {
        return jobSubName;
    }

    @Property(viewable = true, order = 11)
    public String getJobStyle() {
        return jobStyle;
    }

    @Property(viewable = true, order = 12)
    public String getJobCreator() {
        return jobCreator;
    }

    @Property(viewable = true, order = 13)
    public String getClientId() {
        return clientId;
    }

    @Property(viewable = true, order = 14)
    public String getGlobalUid() {
        return globalUid;
    }

    @Property(viewable = true, order = 15)
    public String getProgramOwner() {
        return programOwner;
    }

    @Property(viewable = true, order = 16)
    public String getProgramName() {
        return programName;
    }

    @Property(viewable = true, order = 17)
    public String getJobType() {
        return jobType;
    }

    @Property(category = CAT_SETTINGS, viewable = true, order = 18)
    public String getJobAction() {
        return jobAction;
    }

    @Property(category = CAT_SETTINGS, viewable = true, order = 19)
    public long getNumberOfArguments() {
        return numberOfArguments;
    }

    @Property(viewable = true, order = 20)
    public String getScheduleOwner() {
        return scheduleOwner;
    }

    @Property(viewable = true, order = 21)
    public String getScheduleName() {
        return scheduleName;
    }

    @Property(viewable = true, order = 22)
    public String getScheduleType() {
        return scheduleType;
    }

    @Property(viewable = true, order = 23)
    public String getStartDate() {
        return startDate;
    }

    @Property(viewable = true, order = 24)
    public String getRepeatInterval() {
        return repeatInterval;
    }

    @Property(viewable = true, order = 32)
    public String getEndDate() {
        return endDate;
    }

    @Property(category = CAT_EVENTS, viewable = true, order = 25)
    public String getEventQueueOwner() {
        return eventQueueOwner;
    }

    @Property(category = CAT_EVENTS, viewable = true, order = 26)
    public String getEventQueueName() {
        return eventQueueName;
    }

    @Property(category = CAT_EVENTS, viewable = true, order = 27)
    public String getEventQueueAgent() {
        return eventQueueAgent;
    }

    @Property(category = CAT_EVENTS, viewable = true, order = 28)
    public String getEventCondition() {
        return eventCondition;
    }

    @Property(category = CAT_EVENTS, viewable = true, order = 29)
    public String getEventRule() {
        return eventRule;
    }

    @Property(category = CAT_EVENTS, viewable = true, order = 30)
    public String getFileWatcherOwner() {
        return fileWatcherOwner;
    }

    @Property(category = CAT_EVENTS, viewable = true, order = 31)
    public String getFileWatcherName() {
        return fileWatcherName;
    }

    @Property(viewable = true, order = 33)
    public String getJobClass() {
        return jobClass;
    }

    @Property(category = CAT_SETTINGS, viewable = true, order = 34)
    public String getEnabled() {
        return enabled;
    }

    @Property(category = CAT_SETTINGS, viewable = true, order = 35)
    public String getAutoDrop() {
        return autoDrop;
    }

    @Property(category = CAT_SETTINGS, viewable = true, order = 36)
    public String getRestartable() {
        return restartable;
    }

    @Property(viewable = true, order = 37)
    public String getState() {
        return state;
    }

    @Property(category = CAT_SETTINGS, viewable = true, order = 38)
    public int getJobPriority() {
        return jobPriority;
    }

    @Property(category = CAT_STATISTICS, viewable = true, order = 39)
    public long getRunCount() {
        return runCount;
    }

    @Property(category = CAT_STATISTICS, viewable = true, order = 40)
    public long getMaxRuns() {
        return maxRuns;
    }

    @Property(category = CAT_STATISTICS, viewable = true, order = 41)
    public long getFailureCount() {
        return failureCount;
    }

    @Property(category = CAT_STATISTICS, viewable = true, order = 42)
    public long getMaxFailures() {
        return maxFailures;
    }

    @Property(category = CAT_STATISTICS, viewable = true, order = 43)
    public long getRetryCount() {
        return retryCount;
    }

    @Property(category = CAT_STATISTICS, viewable = true, order = 44)
    public String getLastStartDate() {
        return lastStartDate;
    }

    @Property(category = CAT_STATISTICS, viewable = true, order = 45)
    public String getLastRunDuration() {
        return lastRunDuration;
    }

    @Property(category = CAT_SETTINGS, viewable = true, order = 46)
    public String getNextRunDate() {
        return nextRunDate;
    }

    @Property(category = CAT_SETTINGS, viewable = true, order = 47)
    public String getScheduleLimit() {
        return scheduleLimit;
    }

    //@Property(viewable = true, order = 48)
    public String getMaxRunDuration() {
        return maxRunDuration;
    }

    @Property(category = CAT_SETTINGS, viewable = true, order = 49)
    public String getLoggingLevel() {
        return loggingLevel;
    }

    @Property(category = CAT_SETTINGS, viewable = true, order = 50)
    public String getStopOnWindowClose() {
        return stopOnWindowClose;
    }

    @Property(category = CAT_ADVANCED, viewable = true, order = 51)
    public String getInstanceStickiness() {
        return instanceStickiness;
    }

    @Property(category = CAT_ADVANCED, viewable = true, order = 52)
    public String getRaiseEvents() {
        return raiseEvents;
    }

    @Property(category = CAT_SETTINGS, viewable = true, order = 53)
    public String getSystem() {
        return system;
    }

    @Property(category = CAT_ADVANCED, viewable = true, order = 54)
    public String getJobWeight() {
        return jobWeight;
    }

    @Property(category = CAT_ADVANCED, viewable = true, order = 55)
    public String getNlsEnv() {
        return nlsEnv;
    }

    @Property(category = CAT_ADVANCED, viewable = true, order = 56)
    public String getSource() {
        return source;
    }

    //@Property(viewable = true, order = 57)
    public String getNumberOfDestinations() {
        return numberOfDestinations;
    }

    @Property(category = CAT_ADVANCED, viewable = true, order = 58)
    public String getDestinationOwner() {
        return destinationOwner;
    }

    @Property(category = CAT_ADVANCED, viewable = true, order = 59)
    public String getDestination() {
        return destination;
    }

    @Property(category = CAT_ADVANCED, viewable = true, order = 60)
    public String getCredentialOwner() {
        return credentialOwner;
    }

    @Property(category = CAT_ADVANCED, viewable = true, order = 61)
    public String getCredentialName() {
        return credentialName;
    }

    @Property(category = CAT_ADVANCED, viewable = true, order = 62)
    public String getInstanceId() {
        return instanceId;
    }

    @Property(viewable = true, order = 63)
    public String getDeferredDrop() {
        return deferredDrop;
    }

    @Property(category = CAT_ADVANCED, viewable = true, order = 64)
    public String getAllowRunsInRestrictedMode() {
        return allowRunsInRestrictedMode;
    }

    @Property(viewable = true, order = 200)
    @Nullable
    @Override
    public String getDescription() {
        return comments;
    }
}
