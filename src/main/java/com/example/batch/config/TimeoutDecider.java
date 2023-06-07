package com.example.batch.config;

import org.openqa.selenium.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

public class TimeoutDecider implements JobExecutionDecider, StepExecutionListener {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
	public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
    	if (stepExecution.getFailureExceptions().stream().anyMatch(ex -> ex instanceof TimeoutException)) {
        	log.info("timeoutOccurred entered");
        	return new FlowExecutionStatus("RESTART");
        }
		else {
			log.info("timeoutOccurred not entered");
            return new FlowExecutionStatus("COMPLETED");
        }
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		// TODO Auto-generated method stub
		return null;
	}
}