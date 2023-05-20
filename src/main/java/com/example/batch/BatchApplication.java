package com.example.batch;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.annotation.Transactional;
import com.example.batch.Domain.BatchSchedule;
import com.example.batch.Service.BatchScheduleService;
import com.example.batch.job.SimpleJobConfiguration;
import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackField;
import net.gpedro.integrations.slack.SlackMessage;

@SpringBootApplication
public class BatchApplication implements CommandLineRunner {

	@Autowired
	JobLauncher jobLauncher;
	@Autowired 
	SimpleJobConfiguration simpleJobConfiguration; 
	@Autowired
    private TaskExecutor taskExecutor;
	@Autowired
    private SlackApi slackApi;
	@Autowired
	@Qualifier("slackAttachment_failed")
    private SlackAttachment slackAttachment;
    @Autowired
    @Qualifier("slackMessage_failed")
    private SlackMessage slackMessage;
	@Autowired
	private BatchScheduleService batchScheduleService;
	private ThreadLocal<Logger> log = ThreadLocal.withInitial(() -> {
    	return LoggerFactory.getLogger(this.getClass());
    });
	private static final int MAX_THREADS = 3;
	private static ConfigurableApplicationContext context;
	  
	public static void main(String[] args) {
		context = SpringApplication.run(BatchApplication.class, args);
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
                            .addString("imageSelector", batchSchedule.getImageSelector())
                            .addLong("time", System.currentTimeMillis())
                            .toJobParameters();
                    try {
	                    jobLauncher.run(simpleJobConfiguration.myJob(), jobParameters);
                    } catch(Exception e) {
                    	e.printStackTrace();
                    	String msg = "";
                    	msg += "FAILED\n";
                    	msg += Arrays.toString(e.getStackTrace());
                        slackAttachment.setText(msg);

                        // 현재 날짜와 시간 가져오기
                        Date currentDate = new Date();
                        // 대한민국 표준시(KST)로 변환하기
                        TimeZone kstTimeZone = TimeZone.getTimeZone("Asia/Seoul");
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        dateFormat.setTimeZone(kstTimeZone);
                        String kstDateTime = dateFormat.format(currentDate);
                        slackAttachment.setFields(
                                List.of(
                                        new SlackField().setTitle("Request Time").setValue(kstDateTime)
                                )
                        );

                        slackMessage.setAttachments(Collections.singletonList(slackAttachment));

                        slackApi.call(slackMessage);
                    	SpringApplication.exit(context);
//                    	System.exit(1);
                    }
                }
            });
        }

	  }
}
