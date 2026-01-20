package com.example.monew.global.util.batch;

public enum JobStatus {

    USER_DELETE("userDeleteJob","userDeleteStep"),
    NOTIFICATION_DELETE("notificationDeleteJob","notificationDeleteStep"),
    ARTICLE_COLLECT("articleCollectJob","articleCollectStep");

    private final String jobName;
    private final String stepName;

    JobStatus(String jobName, String stepName) {
        this.jobName = jobName;
        this.stepName = stepName;
    }
    public String getJobName() {
        return jobName;
    }
    public String getStepName() {
        return stepName;
    }

}
