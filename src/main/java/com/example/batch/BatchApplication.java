package com.example.batch;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.annotation.Transactional;

import com.example.batch.Domain.BatchSchedule;
import com.example.batch.Service.BatchScheduleService;
import com.example.batch.job.SimpleJobConfiguration;

@SpringBootApplication
public class BatchApplication implements CommandLineRunner {

	@Autowired
	JobLauncher jobLauncher;
	@Autowired
	JobRepository jobRepository;
	@Autowired
	SimpleJobConfiguration simpleJobConfiguration; 
	@Autowired
    private TaskExecutor taskExecutor;
	@Autowired
	private BatchScheduleService batchScheduleService;
	private ThreadLocal<Logger> log = ThreadLocal.withInitial(() -> {
    	return LoggerFactory.getLogger(this.getClass());
    });
	private static final int MAX_THREADS = 5;
	private static final int maxRetry = 100;
	  
	public static void main(String[] args) {
		SpringApplication.run(BatchApplication.class, args);
	}
	
	@Override
	@Transactional("txManager")
	  public void run(String... args) throws Exception 
	  {
		List<BatchSchedule> batchSchedules = batchScheduleService.getBatchScheduleList();
		
		int numThreads = Math.min(MAX_THREADS, batchSchedules.size());

        for (int i = 0; i < numThreads; i++) {
        	List<BatchSchedule> subList = new ArrayList<BatchSchedule>();
        	for(int j = i; j < batchSchedules.size(); j += MAX_THREADS) {
        		subList.add(batchSchedules.get(j));
        	}

            taskExecutor.execute(() -> {
            	log.get().info("batchSchedules SIZE : " + subList.size());
            	for (BatchSchedule batchSchedule : subList) {
                	log.get().info("batchSchedules : " + batchSchedule.getUrl());
                    JobParameters jobParameters = new JobParametersBuilder()
                    		.addString("batchNum", Integer.toString(batchSchedule.getBatchNum()))
                    		.addString("batchName", batchSchedule.getBatchName())
                            .addString("url", batchSchedule.getUrl())
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
                            .addString("deliveryFeeLocation", Integer.toString(batchSchedule.getDeliveryFeeLocation() != null? batchSchedule.getDeliveryFeeLocation() : 0))
                            .addString("sellerSelector1", batchSchedule.getSellerSelector1())
                            .addString("sellerSelector2", batchSchedule.getSellerSelector2())
                            .addString("sellerSelector3", batchSchedule.getSellerSelector3())
                            .addString("sellerLocation", Integer.toString(batchSchedule.getSellerLocation() != null? batchSchedule.getSellerLocation() : 0))
                            .addString("urlSelector1", batchSchedule.getUrlSelector1())
                            .addString("urlSelector2", batchSchedule.getUrlSelector2())
                            .addString("urlSelector3", batchSchedule.getUrlSelector3())
                            .addString("nextButtonSelector", batchSchedule.getNextButtonSelector())
                            .addLong("time", System.currentTimeMillis())
                            .toJobParameters();
                    int retry = 0;
                    while (true) {
                    	try {
	                    	JobInstance instance = createJobInstance(batchSchedule.getBatchNum(), jobParameters);
	                    	if(instance == null) {
		                        if (++retry > maxRetry) {
		                            throw new RuntimeException("Failed to create job instance after " + maxRetry + " retries.");
		                        }
		                        Random random = new Random();
		                        int randomNumber = random.nextInt(9) + 1;
		                        Thread.sleep(100L * randomNumber); // 잠시 대기 후 다시 시도
	                    	}
	                    	else {
	                    		jobLauncher.run(simpleJobConfiguration.myJob(), jobParameters);
	                    		break;
	                    	}
                    	} catch(Exception e) {
                    		e.printStackTrace();
                    		continue;
                    	}
                    }
                }
            });
        }

	  }
	
	public JobInstance createJobInstance(int jobId, JobParameters jobParameters) {
        synchronized (jobRepository) {
            try {
            	log.get().info("jobRepository : " + jobId);
            	if(jobRepository.getLastJobExecution(Integer.toString(jobId), jobParameters) != null) {
            		return jobRepository.getLastJobExecution(Integer.toString(jobId), jobParameters).getJobInstance();
            	}
                return jobRepository.createJobInstance(Integer.toString(jobId), jobParameters);
            } catch (Exception ex) {
                return null;
            }
        }
    }

}
