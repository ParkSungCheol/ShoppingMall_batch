package com.example.batch.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.batch.tasklet.TestTasklet;

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

    public Job myJob() {
        return this.jobBuilderFactory.get("myJob")
                /* step start */
                .start(myStep())
                // 기존 구현체
                .incrementer(new RunIdIncrementer())
                .build();
    }

    public Step myStep() {
        return stepBuilderFactory.get("myStep")
                .tasklet(new TestTasklet())
                .build();
    }
}
