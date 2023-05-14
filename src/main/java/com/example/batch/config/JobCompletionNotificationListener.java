package com.example.batch.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class JobCompletionNotificationListener implements JobExecutionListener {

    private final DataSource dataSource;
    private Connection connection;
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private final Map<Long, BatchStatus> jobStatusMap = new HashMap<>();

    public JobCompletionNotificationListener(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
    	try {
    		jobStatusMap.put(jobExecution.getJobId(), jobExecution.getStatus());
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
    	jobStatusMap.put(jobExecution.getJobId(), jobExecution.getStatus());
    	if (isAllJobsCompleted()) {
    		// 데이터베이스 연결 해제
    		try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                    log.info("db connection closed");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
    		// 모든 Job이 완료되었다면 애플리케이션 종료
//            SpringApplication.exit(jobExecution.getJobInstance().get(), () -> 0);
        };
    }
    
    private boolean isAllJobsCompleted() {
    	Boolean flag = true;
        // 모든 Job의 상태를 확인하여 COMPLETED가 아닌 경우가 있는지 확인
        for (BatchStatus status : jobStatusMap.values()) {
            if (status != BatchStatus.COMPLETED) {
            	flag = false;
            }
        }
        return flag;
    }
}