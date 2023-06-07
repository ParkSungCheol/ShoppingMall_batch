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
        		.<List<Goods>, List<Goods>>chunk(1) // Chunk 사이즈 설정
                .reader(webCrawlingReader)
                .processor(dataProcessor)
                .writer(myBatisItemWriter)
                .faultTolerant()
                .retryLimit(3) // 재시도 횟수 설정
                .retry(TimeoutException.class) // 재시도할 예외 타입 설정
                .build();
    }
}
