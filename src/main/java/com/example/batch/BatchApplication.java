package com.example.batch;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.batch.Domain.BatchSchedule;
import com.example.batch.Service.BatchScheduleService;
import com.example.batch.job.SimpleJobConfiguration;

@SpringBootApplication
public class BatchApplication implements CommandLineRunner {

	@Autowired
	JobLauncher jobLauncher;
	@Autowired
	SimpleJobConfiguration simpleJobConfiguration; 
	@Autowired
	private BatchScheduleService batchScheduleService;
	private Logger log = LoggerFactory.getLogger(this.getClass());
	  
	public static void main(String[] args) {
		SpringApplication.run(BatchApplication.class, args);
	}
	
	@Override
	  public void run(String... args) throws Exception 
	  {
		List<BatchSchedule> batchSchedules = batchScheduleService.getBatchScheduleList();
    	for (BatchSchedule batchSchedule : batchSchedules) {
    		log.info("batchSchedules : " + batchSchedule.getUrl());
            JobParameters jobParameters = new JobParametersBuilder()
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
            jobLauncher.run(simpleJobConfiguration.myJob(), jobParameters);
        }
	  }

}
