package com.example.batch.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.batch.UniqueRunIdIncrementer;
import com.example.batch.tasklet.TestTasklet;

import lombok.RequiredArgsConstructor;

/*
--job.name=incrementerJob
 */

/**
 * JobParametersIncrementer.java - getNext()
 */
@Configuration
@RequiredArgsConstructor
public class SimpleJobConfiguration {

    // job 생성
	@Autowired
    private JobBuilderFactory jobBuilderFactory;
	@Autowired
    private StepBuilderFactory stepBuilderFactory;
	@Autowired
	private TestTasklet testTasklet;

    @Bean
    public Job incrementerJob() {
        return this.jobBuilderFactory.get("incrementerJob")
                /* step start */
                .start(incrementerStep1())
                // 기존 구현체
                .incrementer(new UniqueRunIdIncrementer())
                .build();
    }

    @Bean
    public Step incrementerStep1() {
        return stepBuilderFactory.get("incrementerStep1")
                .tasklet(testTasklet)
                .build();
    }
}
