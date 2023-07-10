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
import org.springframework.context.annotation.Configuration;
import com.example.batch.Domain.Goods;
import com.example.batch.chunk.DataProcessor;
import com.example.batch.chunk.MyBatisItemWriter;
import com.example.batch.chunk.WebCrawlingReader;
import com.example.batch.config.JobCompletionNotificationListener;
import com.example.batch.config.TimeoutDecider;

@Configuration
@EnableBatchProcessing
public class SimpleJobConfiguration {

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
    	Step step = myStep();
        return this.jobBuilderFactory.get("myJob")
        		// BeforeJob, AfterJob 호출
        		.listener(jobCompletionNotificationListener)
                /* step start */
        		.start(step)
                .on("FAILED").to(timeoutDecider) // 실패 시 timeoutDecider 호출
                .on("COMPLETED").end() // 성공 시 step end
                .from(timeoutDecider)
                    .on("RESTART").to(step) // timeoutDecider에서 RESTART 발생 시 step 재실행
                    .on("COMPLETED").end() // timeoutDecider에서 COMPLETE 발생 시 step end
                    .on("*").fail() // timeoutDecider에서 다른 이벤트 발생 시 fail 처리(Job Failed)
                .end()
                /* step end */
                .incrementer(new RunIdIncrementer()) // job이 중복되지 않도록 id 부여
                .build();
    }

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
