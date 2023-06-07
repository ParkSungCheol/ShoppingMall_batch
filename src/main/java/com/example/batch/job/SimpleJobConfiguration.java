package com.example.batch.job;

import java.util.List;
import org.openqa.selenium.TimeoutException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.listener.RetryListenerSupport;
import org.springframework.retry.policy.SimpleRetryPolicy;
import com.example.batch.Domain.Goods;
import com.example.batch.chunk.DataProcessor;
import com.example.batch.chunk.MyBatisItemWriter;
import com.example.batch.chunk.WebCrawlingReader;
import com.example.batch.config.JobCompletionNotificationListener;

/*
--job.name=incrementerJob
 */

/**
 * JobParametersIncrementer.java - getNext()
 */
@Configuration
@EnableBatchProcessing
public class SimpleJobConfiguration {

    // job 생성
	@Autowired
    private JobBuilderFactory jobBuilderFactory;
	@Autowired
    private StepBuilderFactory stepBuilderFactory;
	@Autowired
	private JobCompletionNotificationListener jobCompletionNotificationListener;
	@Autowired
	JobLauncher jobLauncher;
	@Autowired
	JobRepository jobRepository;
	@Autowired
	WebCrawlingReader webCrawlingReader;
	@Autowired
	DataProcessor dataProcessor;
	@Autowired
	MyBatisItemWriter myBatisItemWriter;

    public Job myJob() {
        return this.jobBuilderFactory.get("myJob")
        		.listener(jobCompletionNotificationListener)
                /* step start */
                .start(myStep())
                // 기존 구현체
                .incrementer(new RunIdIncrementer())
                .build();
    }

    public Step myStep() {
        return stepBuilderFactory.get("myStep")
                .<List<Goods>, List<Goods>>chunk(1)
                .reader(webCrawlingReader)
                .processor(dataProcessor)
                .writer(myBatisItemWriter)
                .faultTolerant()
                .skip(TimeoutException.class) // TimeoutException을 스킵
                .retryLimit(3)
                .retry(TimeoutException.class)
                .retryPolicy(customRetryPolicy()) // 커스텀 RetryPolicy 설정
                .listener(customRetryListener()) // 커스텀 RetryListener 설정
                .build();
    }

    private RetryPolicy customRetryPolicy() {
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(3);
        return retryPolicy;
    }

    private RetryListener customRetryListener() {
        return new RetryListenerSupport() {
            @Override
            public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
                // 재시도 전에 pageNum 등을 초기화하는 작업 수행 가능
                return super.open(context, callback);
            }
            
            @Override
            public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
                // 재시도 시도 후에 pageNum 등을 업데이트하는 작업 수행 가능
                super.onError(context, callback, throwable);
            }
        };
    }
}
