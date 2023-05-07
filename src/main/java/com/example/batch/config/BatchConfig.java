package com.example.batch.config;

import javax.sql.DataSource;

import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfig implements BatchConfigurer {

	private final DataSource dataSource;
	private TaskExecutor taskExecutor;
	
	public BatchConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }
	
    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(4);
        taskExecutor.setMaxPoolSize(4);
        taskExecutor.setThreadNamePrefix("batch-thread-");
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.setAwaitTerminationSeconds(-1);
        taskExecutor.initialize();
        this.taskExecutor = taskExecutor;
        return taskExecutor;
    }

	@Override
	public JobRepository getJobRepository() throws Exception {
		// TODO Auto-generated method stub
		JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(getTransactionManager());
        factory.setIsolationLevelForCreate("ISOLATION_REPEATABLE_READ"); // 원하는 Isolation Level 설정
        factory.setDatabaseType("MYSQL"); // 데이터베이스 유형 설정
        factory.afterPropertiesSet();
        return factory.getObject();
	}

	@Override
	public PlatformTransactionManager getTransactionManager() throws Exception {
		// TODO Auto-generated method stub
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
//        transactionManager.setTransactionTimeout(0); // timeout 설정
        return transactionManager;
	}

	@Override
	public JobLauncher getJobLauncher() throws Exception {
		// TODO Auto-generated method stub
		SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(getJobRepository());
        jobLauncher.setTaskExecutor(taskExecutor);
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
	}

	@Override
	public JobExplorer getJobExplorer() throws Exception {
		// TODO Auto-generated method stub
		JobExplorerFactoryBean factory = new JobExplorerFactoryBean();
        factory.setDataSource(dataSource);
        factory.afterPropertiesSet();
        return factory.getObject();
	}
    
}