package com.example.batch.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.example.batch.Domain.BatchSchedule;
import com.example.batch.Service.BatchScheduleService;
import com.example.batch.Service.ChatBotService;

@Component
public class JobCompletionNotificationListener implements JobExecutionListener {

    private final TrackedDataSource dataSource;
    private Connection connection;
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private TaskExecutor taskExecutor;
    private ChatBotService botService;
    private static int jobCount;
    
    public JobCompletionNotificationListener(TrackedDataSource dataSource, TaskExecutor taskExecutor, BatchScheduleService batchScheduleService, ChatBotService botService) {
        this.dataSource = dataSource;
        this.taskExecutor = taskExecutor;
        List<BatchSchedule> batchSchedules = batchScheduleService.getBatchScheduleList();
        jobCount = batchSchedules.size();
        this.botService = botService;
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
			int totalSize = Integer.parseInt((String) jobExecution.getExecutionContext().get("totalSize"));
			int totalSkippedSize = Integer.parseInt((String) jobExecution.getExecutionContext().get("totalSkippedSize"));
			msg += "COMPLETED\n";
			msg += "totalSize : " + totalSize + "\n";
			msg += "insertedSize : " + (totalSize - totalSkippedSize) + "\n";
			msg += "totalSkippedSize : " + totalSkippedSize;
		}
		try {
			botService.sendSlack(msg);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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