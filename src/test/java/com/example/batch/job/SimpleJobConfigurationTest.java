package com.example.batch.job;

import java.util.List;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.batch.Domain.Goods;
import com.example.batch.chunk.DataProcessorTest;
import com.example.batch.chunk.MyBatisItemWriterTest;
import com.example.batch.chunk.WebCrawlingReaderTest;
import com.example.batch.config.JobCompletionNotificationListenerTest;

@Configuration
@EnableBatchProcessing
public class SimpleJobConfigurationTest {

	@Autowired
    private JobBuilderFactory jobBuilderFactory;
	@Autowired
    private StepBuilderFactory stepBuilderFactory;
	@Autowired
	private JobCompletionNotificationListenerTest jobCompletionNotificationListener;
	@Autowired
	@Qualifier(value = "testJobLauncher")
	JobLauncher jobLauncher;
	@Autowired
	@Qualifier(value = "testJobRepository")
	JobRepository jobRepository;
	@Autowired
	WebCrawlingReaderTest webCrawlingReader;
	@Autowired
	DataProcessorTest dataProcessor;
	@Autowired
	MyBatisItemWriterTest myBatisItemWriter;

	@Bean
    public Job myJob() {
    	Step step = myStep();
        return this.jobBuilderFactory.get("myJob")
        		// BeforeJob, AfterJob 호출
        		.listener(jobCompletionNotificationListener)
                /* step start */
        		.start(step)
                /* step end */
                .incrementer(new RunIdIncrementer()) // job이 중복되지 않도록 id 부여
                .build();
    }

	@Bean
    public Step myStep() {
        return stepBuilderFactory.get("myStep")
        		// chunk 1 단위가 끝날때마다 DB에 SQL push
                .<List<Goods>, List<Goods>>chunk(1)
                .reader(webCrawlingReader)
                .processor(dataProcessor)
                .writer(myBatisItemWriter)
                .build();
    }
}
