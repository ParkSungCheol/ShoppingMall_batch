package com.example.batch.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
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
import com.example.batch.Service.SlackService;

@Component
public class JobCompletionNotificationListener implements JobExecutionListener {

    private final TrackedDataSource dataSource;
    private Connection connection;
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private TaskExecutor taskExecutor;
    private static int jobCount;
    private SlackService slackService;
    
    public JobCompletionNotificationListener(TrackedDataSource dataSource, TaskExecutor taskExecutor, BatchScheduleService batchScheduleService, SlackService slackService) {
        this.dataSource = dataSource;
        this.taskExecutor = taskExecutor;
        List<BatchSchedule> batchSchedules = batchScheduleService.getBatchScheduleList();
        jobCount = batchSchedules.size();
        this.slackService = slackService;
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
    	int flag = 0;
    	int totalSize = (int) jobExecution.getExecutionContext().get("totalSize");
		int totalSkippedSize = (int) jobExecution.getExecutionContext().get("totalSkippedSize");
		Date startTime = jobExecution.getStartTime();
        Date endTime = jobExecution.getEndTime();
        long executionTime = endTime.getTime() - startTime.getTime();
        String formattedTime = formatExecutionTime(executionTime);
		if(jobExecution.getStatus() == BatchStatus.FAILED) {
			log.info("############## FAILED ###############");
			msg += "FAILED\n";
			msg += "totalSize : " + totalSize + "\n";
			msg += "insertedSize : " + (totalSize - totalSkippedSize) + "\n";
			msg += "totalSkippedSize : " + totalSkippedSize + "\n";
			msg += "runTime : " + formattedTime + "\n";
			msg += "[ errorLog ]\n" + jobExecution.getAllFailureExceptions().get(0).getMessage();
		}
		else if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
			msg += "COMPLETED\n";
			msg += "totalSize : " + totalSize + "\n";
			msg += "insertedSize : " + (totalSize - totalSkippedSize) + "\n";
			msg += "totalSkippedSize : " + totalSkippedSize;
			msg += "runTime : " + formattedTime + "\n";
			flag = 1;
		}
		slackService.call(flag, msg);
        
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
    
    public String formatExecutionTime(long executionTime) {
        long seconds = executionTime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        long remainingSeconds = seconds % 60;
        long remainingMinutes = minutes % 60;
        
        String formattedTime = String.format("%02d:%02d:%02d", hours, remainingMinutes, remainingSeconds);
        
        return formattedTime;
    }
}