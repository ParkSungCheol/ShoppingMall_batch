package com.example.batch.config;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import lombok.Synchronized;

@Component
@Profile("test") // test 프로파일에서만 사용
public class JobCompletionNotificationListenerTest implements JobExecutionListener {

    private ThreadPoolTaskExecutor taskExecutor;
    private static CountDownLatch latch = null;
	private ConfigurableApplicationContext applicationContext;
	private ThreadLocal<Logger> log = ThreadLocal.withInitial(() -> {
    	return LoggerFactory.getLogger(this.getClass());
    });
    
	@Autowired
    public JobCompletionNotificationListenerTest(ConfigurableApplicationContext applicationContext, ThreadPoolTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
        this.applicationContext = applicationContext;
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
    	// 남은 jobCount
    	synchronized (this) {
    		try {
				Thread.currentThread().sleep(6000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		latch.countDown();
    	}
    	log.get().info("!!!!!!!!!!!!!jobCount : {}!!!!!!!!!!!!!!!", latch.getCount());
    	
    	assertEquals(jobExecution.getStatus(), BatchStatus.COMPLETED);
    	
    	// 모든 job이 완료되었다면
    	if(latch.getCount() == 0) {
        	// 모든 쓰레드가 완료될 때까지 대기
            try {
				latch.await();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    	}
    }
}