package com.example.batch.job;

import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import com.example.batch.Domain.Goods;
import com.example.batch.chunk.DataProcessor;
import com.example.batch.chunk.MyBatisItemWriter;
import com.example.batch.chunk.WebCrawlingReader;
import com.example.batch.config.JobCompletionNotificationListener;
import com.example.batch.config.TimeoutDecider;

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
	@Autowired
	TimeoutDecider timeoutDecider;

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
                .listener(timeoutDecider) // TimeoutDecider를 Step의 리스너로 등록
                .build();
    }
    
    public Step timeoutStep() {
        return this.stepBuilderFactory.get("timeoutStep")
                .tasklet((contribution, chunkContext) -> {
                    // TimeoutDecider 호출
                    FlowExecutionStatus status = timeoutDecider.decide(chunkContext.getStepContext().getStepExecution().getJobExecution(),
                            chunkContext.getStepContext().getStepExecution());
                    return status.equals(new FlowExecutionStatus("RESTART")) ? RepeatStatus.CONTINUABLE : RepeatStatus.FINISHED;
                })
                .build();
    }
}
