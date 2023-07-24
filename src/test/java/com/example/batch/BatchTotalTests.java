package com.example.batch;

import java.util.ArrayList;
import java.util.List;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.annotation.Transactional;
import com.example.batch.Domain.BatchSchedule;
import com.example.batch.Service.BatchScheduleService;
import com.example.batch.job.SimpleJobConfigurationTest;

@SpringBootApplication
@SpringBatchTest
@SpringBootTest
@Transactional(value="txManager")
class BatchTotalTests implements CommandLineRunner {

	static int jobCount = -1;
	static boolean isClosed = false;
	static String account = "1";
	private static ConfigurableApplicationContext context;
	@Autowired
	BatchScheduleService service;
	@Autowired
	TaskExecutor taskExecutor;
	@Autowired
	JobLauncher jobLauncher;
	@Autowired 
	SimpleJobConfigurationTest simpleJobConfiguration; 

	public static void main(String[] args) {
		context = SpringApplication.run(BatchTotalTests.class, args);
	}
	
	@Override
	public void run(String... args) throws Exception {
		// given

		int MAX_THREADS = 4;
		// 전체 배치대상 검색어리스트 가져온 후
		List<BatchSchedule> batchSchedules = service.getBatchScheduleList(9041, 9044);
		int numThreads = Math.min(MAX_THREADS, batchSchedules.size());
		
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
            		JobParameters jobParameters = new JobParametersBuilder()
                    		.addString("batchNum", Integer.toString(batchSchedule.getBatchNum()))
                    		.addString("batchName", batchSchedule.getBatchName())
                            .addString("url", batchSchedule.getUrl())
                            .addString("target", batchSchedule.getTarget())
                            .addString("totalSelector", batchSchedule.getTotalSelector())
                            .addString("titleSelector1", batchSchedule.getTitleSelector1())
                            .addString("titleSelector2", batchSchedule.getTitleSelector2())
                            .addString("titleSelector3", batchSchedule.getTitleSelector3())
                            .addString("titleLocation", Integer.toString(batchSchedule.getTitleLocation() != null? batchSchedule.getTitleLocation() : 0))
                            .addString("priceSelector1", batchSchedule.getPriceSelector1())
                            .addString("priceSelector2", batchSchedule.getPriceSelector2())
                            .addString("priceSelector3", batchSchedule.getPriceSelector3())
                            .addString("priceLocation", Integer.toString(batchSchedule.getPriceLocation() != null? batchSchedule.getPriceLocation() : 0))
                            .addString("deliveryFeeSelector1", batchSchedule.getDeliveryFeeSelector1())
                            .addString("deliveryFeeSelector2", batchSchedule.getDeliveryFeeSelector2())
                            .addString("deliveryFeeSelector3", batchSchedule.getDeliveryFeeSelector3())
                            .addString("deliveryFeeSelector4", batchSchedule.getDeliveryFeeSelector4())
                            .addString("deliveryFeeLocation", Integer.toString(batchSchedule.getDeliveryFeeLocation() != null? batchSchedule.getDeliveryFeeLocation() : 0))
                            .addString("sellerSelector1", batchSchedule.getSellerSelector1())
                            .addString("sellerSelector2", batchSchedule.getSellerSelector2())
                            .addString("sellerSelector3", batchSchedule.getSellerSelector3())
                            .addString("sellerLocation", Integer.toString(batchSchedule.getSellerLocation() != null? batchSchedule.getSellerLocation() : 0))
                            .addString("urlSelector1", batchSchedule.getUrlSelector1())
                            .addString("urlSelector2", batchSchedule.getUrlSelector2())
                            .addString("urlSelector3", batchSchedule.getUrlSelector3())
                            .addString("nextButtonSelector", batchSchedule.getNextButtonSelector())
                            .addString("imageSelector", batchSchedule.getImageSelector())
                            // 추후 Slack에 보낼 계정 이름
                            .addString("account",  account)
                            // 추후 모든 job이 완료되었는지를 판별할 때 사용할 전체 job 개수
                            .addLong("jobCount", (long) batchSchedules.size())
                            // 각 job을 구별할 구분자
                            .addLong("time", System.currentTimeMillis())
                            .toJobParameters();
            		try {
	                    jobLauncher.run(simpleJobConfiguration.myJob(), jobParameters);
                    } catch(Exception e) {
                    	e.printStackTrace();
                    	// 오류 발생시 SpringApplication 종료
                    	SpringApplication.exit(context);
                    }
                }
            });
		}
	}

}
