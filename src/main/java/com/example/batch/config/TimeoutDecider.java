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
        String msg = "[account] " + jobExecution.getExecutionContext().get("account") + "\n";
        msg += "[target] " + jobExecution.getExecutionContext().get("target") + "\n";
        msg += "[startPageNum] " + jobExecution.getExecutionContext().get("startPageNum") + "\n";
        retryCounts.put(jobExecutionId, currentRetryCount - 1);
        msg += "[remainCount] " + retryCounts.get(jobExecutionId) + "번 남았습니다.\n";

        if (currentRetryCount == 0) {
            return new FlowExecutionStatus("FAILED");
        } else if (stepExecution.getFailureExceptions().stream().anyMatch(ex -> ex instanceof TimeoutException)) {
            log.info("timeoutOccurred entered");
            msg += "[errorLog] " + stepExecution.getFailureExceptions();
            slackService.call(0, msg);
            return new FlowExecutionStatus("RESTART");
        } else if (stepExecution.getFailureExceptions().isEmpty()) {
            log.info("No failure exceptions. Job completed.");
            return new FlowExecutionStatus("COMPLETED");
        } else {
            log.info("Failure exceptions other than TimeoutException occurred. Retry this.");
            msg += "[errorLog] " + stepExecution.getFailureExceptions();
            slackService.call(0, msg);
            return new FlowExecutionStatus("RESTART");
        }
    }
}