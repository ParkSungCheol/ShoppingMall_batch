package com.example.batch.config;

import javax.sql.DataSource;

import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Profile("main")
public class BatchConfig implements BatchConfigurer {

	private final DataSource dataSource;
	private TaskExecutor taskExecutor;
	
	@Autowired
	public BatchConfig(@Qualifier(value = "mainDataSource") DataSource dataSource) {
        this.dataSource = dataSource;
    }
	
    @Bean(name = "mainTaskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        // 5개의 쓰레드 사용
        taskExecutor.setCorePoolSize(5);
        taskExecutor.setMaxPoolSize(5);
        taskExecutor.setDaemon(false); // 데몬 스레드가 아닌 일반 스레드로 설정
        taskExecutor.setThreadNamePrefix("batch-thread-");
        taskExecutor.setWaitForTasksToCompleteOnShutdown(false);
        taskExecutor.setAwaitTerminationSeconds(600);
        taskExecutor.initialize();
        this.taskExecutor = taskExecutor;
        return taskExecutor;
    }

	@Override
	@Bean(name = "mainJobRepository")
	public JobRepository getJobRepository() throws Exception {
		JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(getTransactionManager());
        // 원하는 Isolation Level 설정
        factory.setIsolationLevelForCreate("ISOLATION_REPEATABLE_READ");
        // 데이터베이스 유형 설정
        factory.setDatabaseType("MYSQL");
        factory.afterPropertiesSet();
        return factory.getObject();
	}

	@Override
	@Bean(name = "mainPlatformTransactionManager")
	public PlatformTransactionManager getTransactionManager() throws Exception {
		DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        return transactionManager;
	}

	@Override
	@Bean(name = "mainJobLauncher")
	public JobLauncher getJobLauncher() throws Exception {
		SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(getJobRepository());
        // 다중쓰레드 설정
        jobLauncher.setTaskExecutor(taskExecutor);
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
	}

	@Override
	@Bean(name = "mainJobExplorer")
	public JobExplorer getJobExplorer() throws Exception {
		JobExplorerFactoryBean factory = new JobExplorerFactoryBean();
        factory.setDataSource(dataSource);
        factory.afterPropertiesSet();
        return factory.getObject();
	}
    
}