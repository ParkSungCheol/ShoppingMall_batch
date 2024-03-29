package com.example.batch.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test") // test 프로파일에서만 사용
public class JobCompletionNotificationListenerTest implements JobExecutionListener {

    private static CountDownLatch latch = null;
	private ThreadLocal<Logger> log = ThreadLocal.withInitial(() -> {
    	return LoggerFactory.getLogger(this.getClass());
    });
	@Autowired
    public JobCompletionNotificationListenerTest() {
    }

    @Override
    // job 시작 전 호출
    public void beforeJob(JobExecution jobExecution) {
		// CountDownLatch 초기화
        if(latch == null) {
        	latch = new CountDownLatch((int) (long) jobExecution.getJobParameters().getLong("jobCount"));
        }
    }

    @Override
    // job 종료 후 호출
    public void afterJob(JobExecution jobExecution){
    	jobExecution.setExitStatus(ExitStatus.UNKNOWN);
    	// 남은 jobCount
    	latch.countDown();
    	log.get().info("!!!!!!!!!!!!!jobCount : {}!!!!!!!!!!!!!!!", latch.getCount());
    	
    	// jobExecution이 COMPLETED 상태가 아닌 경우에만 ExitStatus를 FAILED로 설정
    	if (!jobExecution.getStatus().equals(BatchStatus.COMPLETED)) {
    	    jobExecution.setExitStatus(ExitStatus.FAILED);
    	}
    	assertEquals(jobExecution.getStatus(), BatchStatus.COMPLETED);
    	
    	synchronized (this) {
    		// 모든 job이 완료되었다면
        	if(latch != null && latch.getCount() == 0) {
            	// 모든 쓰레드가 완료될 때까지 대기
                try {
    				latch.await();
    			} catch (Exception e1) {
    				e1.printStackTrace();
    				jobExecution.setStatus(BatchStatus.FAILED);
    			}
                latch = null;
        	}
		}
    	
    	// jobExecution이 COMPLETED 상태가 아닌 경우에만 ExitStatus를 FAILED로 설정
    	if (!jobExecution.getStatus().equals(BatchStatus.COMPLETED)) {
    	    jobExecution.setExitStatus(ExitStatus.FAILED);
    	}
    	else if(jobExecution.getExitStatus().equals(ExitStatus.UNKNOWN)) jobExecution.setExitStatus(ExitStatus.COMPLETED);
    	assertEquals(jobExecution.getStatus(), BatchStatus.COMPLETED);
    }
}