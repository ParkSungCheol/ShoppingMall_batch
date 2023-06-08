//package com.example.batch.config;
//
//import org.openqa.selenium.TimeoutException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.batch.core.BatchStatus;
//import org.springframework.batch.core.ExitStatus;
//import org.springframework.batch.core.JobExecution;
//import org.springframework.batch.core.StepExecution;
//import org.springframework.batch.core.StepExecutionListener;
//import org.springframework.batch.core.job.flow.FlowExecutionStatus;
//import org.springframework.batch.core.job.flow.JobExecutionDecider;
//import org.springframework.stereotype.Component;
//
//@Component
//public class TimeoutDecider implements JobExecutionDecider, StepExecutionListener {
//
//    private Logger log = LoggerFactory.getLogger(this.getClass());
//    private static final ThreadLocal<Integer> retryCount = new ThreadLocal<>();
//    
//    @Override
//	public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
//    	
//    	if(retryCount.get() == null) {
//    		retryCount.set(3);
//    	}
//    	
//    	if(retryCount.get() == 0) {
//    		log.info("timeoutOccurred is 0");
//    		// 이전 스텝을 UNKNOWN 상태로 변경하여 재시작되도록 함
//            stepExecution.setStatus(BatchStatus.FAILED);
//            
//    		return new FlowExecutionStatus("FAILED");
//    	}
//    	else if (stepExecution.getFailureExceptions().stream().anyMatch(ex -> ex instanceof TimeoutException)) {
//        	log.info("timeoutOccurred entered");
//        	retryCount.set(retryCount.get() - 1);
//        	return new FlowExecutionStatus("RESTART");
//        }
//		else {
//			log.info("timeoutOccurred not entered");
//            return new FlowExecutionStatus("COMPLETED");
//        }
//	}
//
//	@Override
//	public void beforeStep(StepExecution stepExecution) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public ExitStatus afterStep(StepExecution stepExecution) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//}