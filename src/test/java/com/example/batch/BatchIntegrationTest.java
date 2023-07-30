package com.example.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;
import com.example.batch.Domain.BatchSchedule;
import com.example.batch.Mapper.BatchScheduleMapperTest;
import com.example.batch.Service.BatchScheduleServiceDBTest;
import com.example.batch.Service.BatchScheduleServiceTest;
import com.example.batch.Service.GoodsServiceTest;
import com.example.batch.chunk.DataProcessorTest;
import com.example.batch.chunk.MyBatisItemWriterInsertTest;
import com.example.batch.chunk.MyBatisItemWriterTest;
import com.example.batch.chunk.WebCrawlingReaderTest;
import com.example.batch.config.BatchConfigTest;
import com.example.batch.config.DbConfigTest;
import com.example.batch.config.EventHandler;
import com.example.batch.config.JobCompletionNotificationListenerTest;
import com.example.batch.job.SimpleJobConfigurationTest;

@SpringBootTest(classes = {
	    DataProcessorTest.class,
	    MyBatisItemWriterTest.class,
	    WebCrawlingReaderTest.class,
	    BatchConfigTest.class,
	    DbConfigTest.class,
	    EventHandler.class,
	    JobCompletionNotificationListenerTest.class,
	    SimpleJobConfigurationTest.class,
	    BatchScheduleMapperTest.class,
	    BatchScheduleServiceTest.class,
	    BatchScheduleServiceDBTest.class,
	    BatchSchedule.class,
	    MyBatisItemWriterInsertTest.class,
	    GoodsServiceTest.class
	})
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BatchIntegrationTest {

	private ThreadLocal<Logger> log = ThreadLocal.withInitial(() -> {
    	return LoggerFactory.getLogger(this.getClass());
    });
	static boolean isClosed = false;
	static String account = "1";
	@Autowired
	BatchScheduleServiceTest service;
	@Autowired
	BatchScheduleServiceDBTest DBservice;
	@Autowired
	@Qualifier(value = "testTaskExecutor")
	ThreadPoolTaskExecutor taskExecutor;
	@Autowired 
	SimpleJobConfigurationTest simpleJobConfiguration; 
	JobExecution execution;
	@Autowired
	private SimpleJobLauncher jobLauncher;
	@Autowired
	private JobRepository jobRepository;
	@Autowired
	@Qualifier(value = "jobNotInsert")
	private Job jobNotInsert;
	@Autowired
	@Qualifier(value = "jobInsert")
	private Job jobInsert;
	private JobLauncherTestUtils jobLauncherTestUtils;

	@BeforeEach
    public void setup() {
		jobLauncherTestUtils = new JobLauncherTestUtils();
        // JobLauncherTestUtils 초기화 및 설정
		jobLauncher.setTaskExecutor(taskExecutor);
		jobLauncherTestUtils.setJobLauncher(jobLauncher); // jobLauncher는 테스트용 JobLauncher 빈을 주입받아야 합니다.
		jobLauncherTestUtils.setJobRepository(jobRepository); // jobRegistry는 테스트용 JobRegistry 빈을 주입받아야 합니다.
    }
	
	@Test
	@DisplayName("IntegrationTest_NOT_DB")
	public void simpleTotalTest1() throws Exception {
		
		// [ before SetUp ]
		jobLauncherTestUtils.setJob(jobNotInsert); // 테스트할 Job을 설정합니다.
		
		// [ given ]
		int MAX_THREADS = 4;
		// 전체 배치대상 검색어리스트 가져온 후
		List<BatchSchedule> BatchSchedules = service.getBatchScheduleList(9041, 9048);
		int numThreads = Math.min(MAX_THREADS, BatchSchedules.size());
		
		// [ when ]
		for (int i = 0; i < BatchSchedules.size(); i++) {
    		
			BatchSchedule BatchSchedule = BatchSchedules.get(i);
			
    		log.get().info("###### BatchIntegrationTests BatchSchedules.size() : {} #######", BatchSchedules.size());
    		log.get().info("###### BatchIntegrationTests batchName : {} #######", BatchSchedule.getBatchName());
    		
    		JobParameters jobParameters = new JobParametersBuilder()
            		.addString("batchNum", Integer.toString(BatchSchedule.getBatchNum()))
            		.addString("batchName", BatchSchedule.getBatchName())
                    .addString("url", BatchSchedule.getUrl())
                    .addString("target", BatchSchedule.getTarget())
                    .addString("totalSelector", BatchSchedule.getTotalSelector())
                    .addString("titleSelector1", BatchSchedule.getTitleSelector1())
                    .addString("titleSelector2", BatchSchedule.getTitleSelector2())
                    .addString("titleSelector3", BatchSchedule.getTitleSelector3())
                    .addString("titleLocation", Integer.toString(BatchSchedule.getTitleLocation() != null? BatchSchedule.getTitleLocation() : 0))
                    .addString("priceSelector1", BatchSchedule.getPriceSelector1())
                    .addString("priceSelector2", BatchSchedule.getPriceSelector2())
                    .addString("priceSelector3", BatchSchedule.getPriceSelector3())
                    .addString("priceLocation", Integer.toString(BatchSchedule.getPriceLocation() != null? BatchSchedule.getPriceLocation() : 0))
                    .addString("deliveryFeeSelector1", BatchSchedule.getDeliveryFeeSelector1())
                    .addString("deliveryFeeSelector2", BatchSchedule.getDeliveryFeeSelector2())
                    .addString("deliveryFeeSelector3", BatchSchedule.getDeliveryFeeSelector3())
                    .addString("deliveryFeeSelector4", BatchSchedule.getDeliveryFeeSelector4())
                    .addString("deliveryFeeLocation", Integer.toString(BatchSchedule.getDeliveryFeeLocation() != null? BatchSchedule.getDeliveryFeeLocation() : 0))
                    .addString("sellerSelector1", BatchSchedule.getSellerSelector1())
                    .addString("sellerSelector2", BatchSchedule.getSellerSelector2())
                    .addString("sellerSelector3", BatchSchedule.getSellerSelector3())
                    .addString("sellerLocation", Integer.toString(BatchSchedule.getSellerLocation() != null? BatchSchedule.getSellerLocation() : 0))
                    .addString("urlSelector1", BatchSchedule.getUrlSelector1())
                    .addString("urlSelector2", BatchSchedule.getUrlSelector2())
                    .addString("urlSelector3", BatchSchedule.getUrlSelector3())
                    .addString("nextButtonSelector", BatchSchedule.getNextButtonSelector())
                    .addString("imageSelector", BatchSchedule.getImageSelector())
                    // 추후 Slack에 보낼 계정 이름
                    .addString("account",  account)
                    // 추후 모든 job이 완료되었는지를 판별할 때 사용할 전체 job 개수
                    .addLong("jobCount", (long) BatchSchedules.size())
                    // 각 job을 구별할 구분자
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
    		JobExecution execution = jobLauncherTestUtils.launchJob(jobParameters);
			while(i == BatchSchedules.size() - 1 || (i+1) % MAX_THREADS == 0) {
				if(execution.getStatus() == BatchStatus.UNKNOWN || execution.getStatus() == BatchStatus.FAILED) break;
				else {
					Thread.sleep(6000);
					log.get().info("i : {}", i);
					log.get().info("execution.getStatus() : {}", execution.getStatus());
				}
			}
			if(execution.getStatus() == BatchStatus.FAILED) throw new JobExecutionException("JOB Failed");
        }
		
		log.get().info("IntegrationTest_NOT_DB Test is Ended");
	}
	
	@Test
	@DisplayName("IntegrationTest_DB")
	public void simpleTotalTest2() throws Exception {
		
		// [ before SetUp ]
		jobLauncherTestUtils.setJob(jobNotInsert); // 테스트할 Job을 설정합니다.
				
		// [ given ]
		int MAX_THREADS = 4;
		// 전체 배치대상 검색어리스트 가져온 후
		List<BatchSchedule> BatchSchedules = DBservice.getBatchScheduleList(9041, 9048);
		int numThreads = Math.min(MAX_THREADS, BatchSchedules.size());
		
		// [ when ]
		for (int i = 0; i < BatchSchedules.size(); i++) {
    		
			BatchSchedule BatchSchedule = BatchSchedules.get(i);
			
    		log.get().info("###### BatchIntegrationTests BatchSchedules.size() : {} #######", BatchSchedules.size());
    		log.get().info("###### BatchIntegrationTests batchName : {} #######", BatchSchedule.getBatchName());
    		
    		JobParameters jobParameters = new JobParametersBuilder()
            		.addString("batchNum", Integer.toString(BatchSchedule.getBatchNum()))
            		.addString("batchName", BatchSchedule.getBatchName())
                    .addString("url", BatchSchedule.getUrl())
                    .addString("target", BatchSchedule.getTarget())
                    .addString("totalSelector", BatchSchedule.getTotalSelector())
                    .addString("titleSelector1", BatchSchedule.getTitleSelector1())
                    .addString("titleSelector2", BatchSchedule.getTitleSelector2())
                    .addString("titleSelector3", BatchSchedule.getTitleSelector3())
                    .addString("titleLocation", Integer.toString(BatchSchedule.getTitleLocation() != null? BatchSchedule.getTitleLocation() : 0))
                    .addString("priceSelector1", BatchSchedule.getPriceSelector1())
                    .addString("priceSelector2", BatchSchedule.getPriceSelector2())
                    .addString("priceSelector3", BatchSchedule.getPriceSelector3())
                    .addString("priceLocation", Integer.toString(BatchSchedule.getPriceLocation() != null? BatchSchedule.getPriceLocation() : 0))
                    .addString("deliveryFeeSelector1", BatchSchedule.getDeliveryFeeSelector1())
                    .addString("deliveryFeeSelector2", BatchSchedule.getDeliveryFeeSelector2())
                    .addString("deliveryFeeSelector3", BatchSchedule.getDeliveryFeeSelector3())
                    .addString("deliveryFeeSelector4", BatchSchedule.getDeliveryFeeSelector4())
                    .addString("deliveryFeeLocation", Integer.toString(BatchSchedule.getDeliveryFeeLocation() != null? BatchSchedule.getDeliveryFeeLocation() : 0))
                    .addString("sellerSelector1", BatchSchedule.getSellerSelector1())
                    .addString("sellerSelector2", BatchSchedule.getSellerSelector2())
                    .addString("sellerSelector3", BatchSchedule.getSellerSelector3())
                    .addString("sellerLocation", Integer.toString(BatchSchedule.getSellerLocation() != null? BatchSchedule.getSellerLocation() : 0))
                    .addString("urlSelector1", BatchSchedule.getUrlSelector1())
                    .addString("urlSelector2", BatchSchedule.getUrlSelector2())
                    .addString("urlSelector3", BatchSchedule.getUrlSelector3())
                    .addString("nextButtonSelector", BatchSchedule.getNextButtonSelector())
                    .addString("imageSelector", BatchSchedule.getImageSelector())
                    // 추후 Slack에 보낼 계정 이름
                    .addString("account",  account)
                    // 추후 모든 job이 완료되었는지를 판별할 때 사용할 전체 job 개수
                    .addLong("jobCount", (long) BatchSchedules.size())
                    // 각 job을 구별할 구분자
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
    		JobExecution execution = jobLauncherTestUtils.launchJob(jobParameters);
			while(i == BatchSchedules.size() - 1 || (i+1) % MAX_THREADS == 0) {
				if(execution.getStatus() == BatchStatus.UNKNOWN || execution.getStatus() == BatchStatus.FAILED) break;
				else {
					Thread.sleep(6000);
					log.get().info("i : {}", i);
					log.get().info("execution.getStatus() : {}", execution.getStatus());
				}
			}
			if(execution.getStatus() == BatchStatus.FAILED) throw new JobExecutionException("JOB Failed");
        }

		log.get().info("IntegrationTest_NOT_DB Test is Ended");
	}
	
	@Test
	@DisplayName("IntegrationTest_Insert_DB")
	public void simpleTotalTest3() throws Exception {
		
		// [ before SetUp ]
		jobLauncherTestUtils.setJob(jobInsert); // 테스트할 Job을 설정합니다.
		
		// [ given ]
		int MAX_THREADS = 4;
		// 전체 배치대상 검색어리스트 가져온 후
		List<BatchSchedule> BatchSchedules = service.getBatchScheduleList(9041, 9048);
		int numThreads = Math.min(MAX_THREADS, BatchSchedules.size());
		
		// [ when ]
		for (int i = 0; i < BatchSchedules.size(); i++) {
    		
			BatchSchedule BatchSchedule = BatchSchedules.get(i);
			
    		log.get().info("###### BatchIntegrationTests BatchSchedules.size() : {} #######", BatchSchedules.size());
    		log.get().info("###### BatchIntegrationTests batchName : {} #######", BatchSchedule.getBatchName());
    		
    		JobParameters jobParameters = new JobParametersBuilder()
            		.addString("batchNum", Integer.toString(BatchSchedule.getBatchNum()))
            		.addString("batchName", BatchSchedule.getBatchName())
                    .addString("url", BatchSchedule.getUrl())
                    .addString("target", BatchSchedule.getTarget())
                    .addString("totalSelector", BatchSchedule.getTotalSelector())
                    .addString("titleSelector1", BatchSchedule.getTitleSelector1())
                    .addString("titleSelector2", BatchSchedule.getTitleSelector2())
                    .addString("titleSelector3", BatchSchedule.getTitleSelector3())
                    .addString("titleLocation", Integer.toString(BatchSchedule.getTitleLocation() != null? BatchSchedule.getTitleLocation() : 0))
                    .addString("priceSelector1", BatchSchedule.getPriceSelector1())
                    .addString("priceSelector2", BatchSchedule.getPriceSelector2())
                    .addString("priceSelector3", BatchSchedule.getPriceSelector3())
                    .addString("priceLocation", Integer.toString(BatchSchedule.getPriceLocation() != null? BatchSchedule.getPriceLocation() : 0))
                    .addString("deliveryFeeSelector1", BatchSchedule.getDeliveryFeeSelector1())
                    .addString("deliveryFeeSelector2", BatchSchedule.getDeliveryFeeSelector2())
                    .addString("deliveryFeeSelector3", BatchSchedule.getDeliveryFeeSelector3())
                    .addString("deliveryFeeSelector4", BatchSchedule.getDeliveryFeeSelector4())
                    .addString("deliveryFeeLocation", Integer.toString(BatchSchedule.getDeliveryFeeLocation() != null? BatchSchedule.getDeliveryFeeLocation() : 0))
                    .addString("sellerSelector1", BatchSchedule.getSellerSelector1())
                    .addString("sellerSelector2", BatchSchedule.getSellerSelector2())
                    .addString("sellerSelector3", BatchSchedule.getSellerSelector3())
                    .addString("sellerLocation", Integer.toString(BatchSchedule.getSellerLocation() != null? BatchSchedule.getSellerLocation() : 0))
                    .addString("urlSelector1", BatchSchedule.getUrlSelector1())
                    .addString("urlSelector2", BatchSchedule.getUrlSelector2())
                    .addString("urlSelector3", BatchSchedule.getUrlSelector3())
                    .addString("nextButtonSelector", BatchSchedule.getNextButtonSelector())
                    .addString("imageSelector", BatchSchedule.getImageSelector())
                    // 추후 Slack에 보낼 계정 이름
                    .addString("account",  account)
                    // 추후 모든 job이 완료되었는지를 판별할 때 사용할 전체 job 개수
                    .addLong("jobCount", (long) BatchSchedules.size())
                    // 각 job을 구별할 구분자
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
			JobExecution execution = jobLauncherTestUtils.launchJob(jobParameters);
			while(i == BatchSchedules.size() - 1 || (i+1) % MAX_THREADS == 0) {
				if(execution.getStatus() == BatchStatus.UNKNOWN || execution.getStatus() == BatchStatus.FAILED) break;
				else {
					Thread.sleep(6000);
					log.get().info("i : {}", i);
					log.get().info("execution.getStatus() : {}", execution.getStatus());
				}
			}
			if(execution.getStatus() == BatchStatus.FAILED) throw new JobExecutionException("JOB Failed");
//			Thread.sleep(9000);
        }
		
		log.get().info("IntegrationTest_Insert_DB Test is Ended");
	}
	
	@AfterAll
	public void afterAllCallBack() {
		// 모든 job이 완료되었다면
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

		// [ then ]
		assertEquals(taskExecutor.getActiveCount(), 0);
	}

}
