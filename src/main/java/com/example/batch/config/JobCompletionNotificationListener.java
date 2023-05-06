package com.example.batch.config;

import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class JobCompletionNotificationListener implements JobExecutionListener {

    private final DataSource dataSource;

    public JobCompletionNotificationListener(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) { }

    @Override
    public void afterJob(JobExecution jobExecution) {
        try {
            dataSource.getConnection().close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}