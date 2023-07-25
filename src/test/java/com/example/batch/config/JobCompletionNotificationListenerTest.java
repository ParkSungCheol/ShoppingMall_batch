package com.example.batch.config;

import static org.junit.Assert.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class JobCompletionNotificationListenerTest implements JobExecutionListener {

    private TaskExecutor taskExecutor;
    private static long jobCount = -1;
	private ConfigurableApplicationContext applicationContext;
	private ThreadLocal<Logger> log = ThreadLocal.withInitial(() -> {
    	return LoggerFactory.getLogger(this.getClass());
    });
    
    public JobCompletionNotificationListenerTest(ConfigurableApplicationContext applicationContext, TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
        this.applicationContext = applicationContext;
    }

    @Override
    // job 시작 전 호출
    public void beforeJob(JobExecution jobExecution) {
		// jobCount 초기화
        if(jobCount == -1) {
    		long jobCount_param = (long) jobExecution.getJobParameters().getLong("jobCount");
    		jobCount = jobCount_param;
    	}
    }

    @Override
    // job 종료 후 호출
    public void afterJob(JobExecution jobExecution){
    	// 남은 jobCount
    	jobCount--;
    	ThreadPoolTaskExecutor tte = (ThreadPoolTaskExecutor) taskExecutor;
    	log.get().info("!!!!!!!!!!!!!jobCount : {}!!!!!!!!!!!!!!!", jobCount);
    	
    	// 모든 job이 완료되었다면
    	if(jobCount == 0) {
    		
    		// ThreadPoolTaskExecutor 종료 요청
//    		tte.shutdown();

    		// 모든 스레드가 종료될 때까지 대기
//    		ExecutorService executorService = tte.getThreadPoolExecutor();
//    		executorService.shutdown();
//    		try {
//    		    if (!executorService.awaitTermination(20, TimeUnit.MINUTES)) {
//    		        // 만약 20분 이내에 스레드들이 종료되지 않으면 강제 종료
//    		        executorService.shutdownNow();
//    		    }
//    		} catch (InterruptedException e) {
//    		    executorService.shutdownNow();
//    		    Thread.currentThread().interrupt();
//    		}
//
//    		// SpringApplication 종료
//    		applicationContext.close();

    	}
    }
}