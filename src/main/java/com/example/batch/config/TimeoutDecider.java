package com.example.batch.config;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.batch.Service.SlackService;

@Component
public class TimeoutDecider implements JobExecutionDecider {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private final Map<Long, Integer> retryCounts = new HashMap<>();
    @Autowired
    private SlackService slackService;

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        long jobExecutionId = jobExecution.getId();
        
        if (!retryCounts.containsKey(jobExecutionId)) {
            retryCounts.put(jobExecutionId, 3);
        }

        int currentRetryCount = retryCounts.get(jobExecutionId);

        if (currentRetryCount == 0) {
            return new FlowExecutionStatus("FAILED");
        } else if (stepExecution.getFailureExceptions().stream().anyMatch(ex -> ex instanceof TimeoutException)) {
            log.info("timeoutOccurred entered");
            retryCounts.put(jobExecutionId, currentRetryCount - 1);
            slackService.call(0, retryCounts + "번 남았습니다.\n[ errorLog ] " + stepExecution.getFailureExceptions());
            return new FlowExecutionStatus("RESTART");
        } else if (stepExecution.getFailureExceptions().isEmpty()) {
            log.info("No failure exceptions. Job completed.");
            return new FlowExecutionStatus("COMPLETED");
        } else {
            log.info("Failure exceptions other than TimeoutException occurred. Retry this.");
            retryCounts.put(jobExecutionId, currentRetryCount - 1);
            slackService.call(0, retryCounts + "번 남았습니다.\n[ errorLog ] " + stepExecution.getFailureExceptions());
            return new FlowExecutionStatus("RESTART");
        }
    }
}