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
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import com.example.batch.Domain.Goods;
import com.example.batch.chunk.DataProcessorTest;
import com.example.batch.chunk.MyBatisItemWriterTest;
import com.example.batch.chunk.WebCrawlingReaderTest;
import com.example.batch.config.EventHandler;
import com.example.batch.config.JobCompletionNotificationListenerTest;

@Configuration
@EnableBatchProcessing
@Profile("test") // test 프로파일에서만 사용
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
	@Autowired
	EventHandler eventHandler;

	@Bean
    public Job myJob() {
    	Step step = myStep();
        return this.jobBuilderFactory.get("myJob")
        		// BeforeJob, AfterJob 호출
        		.listener(jobCompletionNotificationListener)
        		/* step start */
        		.start(step)
//                .on("FAILED").to(eventHandler) // 실패 시 timeoutDecider 호출
//                .on("COMPLETED").end() // 성공 시 step end
//                .from(eventHandler)
//                    .on("COMPLETED").end() // timeoutDecider에서 COMPLETE 발생 시 step end
//                    .on("*").fail() // timeoutDecider에서 다른 이벤트 발생 시 fail 처리(Job Failed)
//                .end()
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
