package com.example.batch.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class EventHandler implements JobExecutionDecider {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        // 실패 없이 성공한 경우
        if (stepExecution.getFailureExceptions().isEmpty()) {
            log.info("No failure exceptions. Job completed.");
            return new FlowExecutionStatus("COMPLETED");
        } 
        // 실패한 경우
        else {
            log.info("Failure exceptions occurred. Retry this.");
            log.info(jobExecution.getFailureExceptions().toString());
            return new FlowExecutionStatus("FAILED");
        }
    }
}