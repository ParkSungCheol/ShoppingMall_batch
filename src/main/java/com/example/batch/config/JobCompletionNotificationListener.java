package com.example.batch.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

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

@Component
public class JobCompletionNotificationListener implements JobExecutionListener {

    private final DataSource dataSource;
    private Connection connection;
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private TaskExecutor taskExecutor;
    private static int jobCount;
    
    public JobCompletionNotificationListener(DataSource dataSource, TaskExecutor taskExecutor, BatchScheduleService batchScheduleService) {
        this.dataSource = dataSource;
        this.taskExecutor = taskExecutor;
        List<BatchSchedule> batchSchedules = batchScheduleService.getBatchScheduleList();
        jobCount = batchSchedules.size();
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
    	try {
            if (connection == null || connection.isClosed()) {
                connection = dataSource.getConnection();
                log.info("db connection opend");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
    	jobCount--;
    	log.info("jobCount : {}", jobCount);
    	ThreadPoolTaskExecutor tte = (ThreadPoolTaskExecutor) taskExecutor;
    	if(jobCount == 0) {
    		try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    log.info("db connection closed");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
    		tte.shutdown();
    		
    		BatchStatus batchStatus = jobExecution.getStatus();
    		if(batchStatus.isGreaterThan(BatchStatus.STOPPING)) {
    			System.exit(0);
    		}
    		else {
    			System.exit(1);
    		}
    	}
    }
}