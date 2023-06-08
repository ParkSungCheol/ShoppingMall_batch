package com.example.batch.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.example.batch.Service.BatchScheduleService;
import com.example.batch.Service.SlackService;

@Component
public class JobCompletionNotificationListener implements JobExecutionListener {

    private final TrackedDataSource dataSource;
    private Connection connection;
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private TaskExecutor taskExecutor;
    private static long jobCount = -1;
    private SlackService slackService;
//    private WebDriverManager webDriverManager;
    
    public JobCompletionNotificationListener(TrackedDataSource dataSource, TaskExecutor taskExecutor, BatchScheduleService batchScheduleService, SlackService slackService) {
        this.dataSource = dataSource;
        this.taskExecutor = taskExecutor;
        this.slackService = slackService;
//        this.webDriverManager = webDriverManager;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
    	try {
            if (connection == null || connection.isClosed()) {
                connection = dataSource.getConnection();
                if(jobCount == -1) {
            		long jobCount_param = (long) jobExecution.getJobParameters().getLong("jobCount");
            		jobCount = jobCount_param;
            	}
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
    	String url = (String) jobExecution.getExecutionContext().get("url");
    	String account = (String) jobExecution.getExecutionContext().get("account");
    	log.info("url : {}", url);
    	String msg = "[account] " + account + "\n[url] " + url + "\n[result] : ";
    	int flag = 0;
    	int totalSize = (int) jobExecution.getExecutionContext().get("totalSize");
		Date startTime = jobExecution.getStartTime();
		// 대한민국 표준시(KST)로 변환하기
        TimeZone kstTimeZone = TimeZone.getTimeZone("Asia/Seoul");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(kstTimeZone);
        String startFormattedTime = dateFormat.format(startTime);
        Date endTime = jobExecution.getEndTime();
        long executionTime = endTime.getTime() - startTime.getTime();
        String executionFormattedTime = formatExecutionTime(executionTime);
		if(jobExecution.getStatus() == BatchStatus.FAILED) {
			log.info("############## FAILED ###############");
			msg += "FAILED \n";
			msg += "totalSize : " + totalSize + " ";
			msg += "insertedSize : " + (totalSize) + "\n";
			msg += "startTime : " + startFormattedTime + "\n";
			msg += "runTime : " + executionFormattedTime + "\n";
			msg += "[ errorLog ]\n" + jobExecution.getAllFailureExceptions().get(0).getMessage();
		}
		else if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
			msg += "COMPLETED \n";
			msg += "totalSize : " + totalSize + " ";
			msg += "insertedSize : " + (totalSize) + "\n";
			msg += "startTime : " + startFormattedTime + "\n";
			msg += "runTime : " + executionFormattedTime + "\n";
			flag = 1;
		}
		slackService.call(flag, msg);
        
    	ThreadPoolTaskExecutor tte = (ThreadPoolTaskExecutor) taskExecutor;
    	if(jobCount == 0) {
//    		webDriverManager.quitAllDrivers();
//    		log.info("#### driver END ####");
    		
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