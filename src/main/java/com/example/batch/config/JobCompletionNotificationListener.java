package com.example.batch.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import com.example.batch.Domain.BatchSchedule;
import com.example.batch.Service.BatchScheduleService;
import net.gpedro.integrations.slack.SlackApi;
import net.gpedro.integrations.slack.SlackAttachment;
import net.gpedro.integrations.slack.SlackField;
import net.gpedro.integrations.slack.SlackMessage;

@Component
public class JobCompletionNotificationListener implements JobExecutionListener {

    private final TrackedDataSource dataSource;
    private Connection connection;
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private TaskExecutor taskExecutor;
    private static int jobCount;
    private SlackApi slackApi;
    private SlackAttachment slackAttachment;
    private SlackMessage slackMessage;
    
    public JobCompletionNotificationListener(TrackedDataSource dataSource, TaskExecutor taskExecutor, BatchScheduleService batchScheduleService, SlackApi slackApi, @Qualifier("slackAttachment_completed") SlackAttachment slackAttachment, @Qualifier("slackMessage_completed") SlackMessage slackMessage) {
        this.dataSource = dataSource;
        this.taskExecutor = taskExecutor;
        List<BatchSchedule> batchSchedules = batchScheduleService.getBatchScheduleList();
        jobCount = batchSchedules.size();
        this.slackApi = slackApi;
        this.slackAttachment = slackAttachment;
        this.slackMessage = slackMessage;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
    	try {
            if (connection == null || connection.isClosed()) {
                connection = dataSource.getConnection();
                log.info("db connection opened");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterJob(JobExecution jobExecution){
    	jobCount--;
    	log.info("jobCount : {}", jobCount);
    	String msg = "result : ";
		if(jobExecution.getStatus() == BatchStatus.FAILED) {
			log.info("############## FAILED ###############");
			msg += "FAILED\n";
			msg += jobExecution.getAllFailureExceptions().get(0).getMessage();
		}
		else if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
			int totalSize = (int) jobExecution.getExecutionContext().get("totalSize");
			int totalSkippedSize = (int) jobExecution.getExecutionContext().get("totalSkippedSize");
			msg += "COMPLETED\n";
			msg += "totalSize : " + totalSize + "\n";
			msg += "insertedSize : " + (totalSize - totalSkippedSize) + "\n";
			msg += "totalSkippedSize : " + totalSkippedSize;
		}
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
        
    	ThreadPoolTaskExecutor tte = (ThreadPoolTaskExecutor) taskExecutor;
    	if(jobCount == 0) {
    		List<Connection> connections = dataSource.getAllConnections();
    		for(Connection connection : connections) {
    			try {
                    connection.close();
                    log.info("db connection closed");
                } catch (SQLException i) {
                    i.printStackTrace();
                }
    		}
    		tte.shutdown();
    	}
    }
}