package com.example.batch.config;

import org.openqa.selenium.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.stereotype.Component;

@Component
public class TimeoutDecider implements JobExecutionDecider {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private final ThreadLocal<Integer> retryCount = new ThreadLocal<>();
    
    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
    	if(this.retryCount.get() == null) {
    		this.retryCount.set(3);
    	}
    	
    	if(this.retryCount.get() == 0) {
    		return new FlowExecutionStatus("FAILED");
    	}
    	else if (stepExecution.getFailureExceptions().stream().anyMatch(ex -> ex instanceof TimeoutException)) {
            log.info("timeoutOccurred entered");
            this.retryCount.set(this.retryCount.get() - 1);
            return new FlowExecutionStatus("RESTART");
        } else {
            log.info("timeoutOccurred not entered");
            return new FlowExecutionStatus("COMPLETED");
        }
    }
}