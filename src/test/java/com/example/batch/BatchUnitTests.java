package com.example.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import com.example.batch.Domain.BatchSchedule;
import com.example.batch.Service.BatchScheduleServiceTest;

class BatchUnitTests {

	static int jobCount = -1;
	static boolean isClosed = false;
	
	@Test
	void shutdownAll() {
		
		// given
		
		int MAX_THREADS = 4;
		BatchScheduleServiceTest service = new BatchScheduleServiceTest();
		// 전체 배치대상 검색어리스트 가져온 후
		List<BatchSchedule> batchSchedules = service.getBatchScheduleList(0, 0);
		int numThreads = Math.min(MAX_THREADS, batchSchedules.size());
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        // 4개의 쓰레드 사용
        taskExecutor.setCorePoolSize(4);
        taskExecutor.setMaxPoolSize(4);
        taskExecutor.setThreadNamePrefix("batch-thread-");
        taskExecutor.setWaitForTasksToCompleteOnShutdown(false);
        taskExecutor.setAwaitTerminationSeconds(600);
        taskExecutor.initialize();
		
        // when
        
        // jobCount 초기화
        if(jobCount == -1) {
    		jobCount = batchSchedules.size();
    	}
        
		for (int i = 0; i < numThreads; i++) {
        	List<BatchSchedule> subList = new ArrayList<BatchSchedule>();
        	
        	// 각 쓰레드에 분배한 후
        	for(int j = i; j < batchSchedules.size(); j += MAX_THREADS) {
        		subList.add(batchSchedules.get(j));
        	}

            taskExecutor.execute(() -> {
            	// 각 쓰레드에 할당된 배치대상 검색어 리스트를 가지고 jobLauncher Run
            	for (BatchSchedule batchSchedule : subList) {
                    try {
	                    Thread.sleep(1000);
	                    jobCount--;
                    } catch(Exception e) {
                    	e.printStackTrace();
                    }
                }
            	
            	// 모든 job이 완료되었다면
            	if(jobCount == 0) {
            		
            		// ThreadPoolTaskExecutor 종료 요청
            		taskExecutor.shutdown();

            		// 모든 스레드가 종료될 때까지 대기
            		ExecutorService executorService = taskExecutor.getThreadPoolExecutor();
            		executorService.shutdown();
            		try {
            		    if (!executorService.awaitTermination(20, TimeUnit.MINUTES)) {
            		        // 만약 20분 이내에 스레드들이 종료되지 않으면 강제 종료
            		        executorService.shutdownNow();
            		    }
            		} catch (InterruptedException e) {
            		    executorService.shutdownNow();
            		    Thread.currentThread().interrupt();
            		}

            		isClosed = true;

            	}
            });
		}
	}

}
