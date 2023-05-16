package com.example.batch.config;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class JobCompletionNotificationListener implements JobExecutionListener {

    private final DataSource dataSource;
    private Connection connection;
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private TaskExecutor taskExecutor;
    
    public JobCompletionNotificationListener(DataSource dataSource, TaskExecutor taskExecutor) {
        this.dataSource = dataSource;
        this.taskExecutor = taskExecutor;
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
    	ThreadPoolTaskExecutor tte = (ThreadPoolTaskExecutor) taskExecutor;
    	if(tte.getActiveCount() == 0) {
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