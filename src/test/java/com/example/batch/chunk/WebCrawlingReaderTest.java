package com.example.batch.chunk;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import com.example.batch.Domain.BatchSchedule;
import com.example.batch.Domain.Goods;

@Component
@Profile("test") // test 프로파일에서만 사용
public class WebCrawlingReaderTest implements ItemReader<List<Goods>>, StepExecutionListener {

    private static final ThreadLocal<BatchSchedule> BatchSchedule = new ThreadLocal<>();
    private static final ThreadLocal<JobExecution> jobExecution = new ThreadLocal<>();
    private static final ThreadLocal<StepExecution> stepEx = new ThreadLocal<>();
    private ThreadLocal<Logger> log = ThreadLocal.withInitial(() -> {
    	return LoggerFactory.getLogger(this.getClass());
    });
    
	@Override
	public List<Goods> read(){
		log.get().info("############ BatchName : {} ##############", BatchSchedule.get().getBatchName());
		return null;
	}

	@Override
	// read 메서드 시작하기 전 호출
	public void beforeStep(StepExecution stepExecution) {
		// StepExecution에서 String으로 받아온 BatchSchedule 정보로 객체구현
		BatchSchedule.set(new BatchSchedule());
    	BatchSchedule.get().setBatchName((String) stepExecution.getJobExecution().getJobParameters().getString("batchName"));
    	BatchSchedule.get().setUrl((String) stepExecution.getJobExecution().getJobParameters().getString("url"));
    	BatchSchedule.get().setTarget((String) stepExecution.getJobExecution().getJobParameters().getString("target"));
    	BatchSchedule.get().setTotalSelector((String) stepExecution.getJobExecution().getJobParameters().getString("totalSelector"));
    	BatchSchedule.get().setTitleSelector1((String) stepExecution.getJobExecution().getJobParameters().getString("titleSelector1"));
    	BatchSchedule.get().setTitleSelector2((String) stepExecution.getJobExecution().getJobParameters().getString("titleSelector2"));
    	BatchSchedule.get().setTitleSelector3((String) stepExecution.getJobExecution().getJobParameters().getString("titleSelector3"));
    	BatchSchedule.get().setTitleLocation(stepExecution.getJobExecution().getJobParameters().getString("titleLocation") != null? Integer.parseInt((String) stepExecution.getJobExecution().getJobParameters().getString("titleLocation")) : 0);
    	BatchSchedule.get().setPriceSelector1((String) stepExecution.getJobExecution().getJobParameters().getString("priceSelector1"));
    	BatchSchedule.get().setPriceSelector2((String) stepExecution.getJobExecution().getJobParameters().getString("priceSelector2"));
    	BatchSchedule.get().setPriceSelector3((String) stepExecution.getJobExecution().getJobParameters().getString("priceSelector3"));
    	BatchSchedule.get().setPriceLocation(stepExecution.getJobExecution().getJobParameters().getString("priceLocation") != null? Integer.parseInt((String) stepExecution.getJobExecution().getJobParameters().getString("priceLocation")) : 0);
    	BatchSchedule.get().setDeliveryFeeSelector1((String) stepExecution.getJobExecution().getJobParameters().getString("deliveryFeeSelector1"));
    	BatchSchedule.get().setDeliveryFeeSelector2((String) stepExecution.getJobExecution().getJobParameters().getString("deliveryFeeSelector2"));
    	BatchSchedule.get().setDeliveryFeeSelector3((String) stepExecution.getJobExecution().getJobParameters().getString("deliveryFeeSelector3"));
    	BatchSchedule.get().setDeliveryFeeSelector4((String) stepExecution.getJobExecution().getJobParameters().getString("deliveryFeeSelector4"));
    	BatchSchedule.get().setDeliveryFeeLocation(stepExecution.getJobExecution().getJobParameters().getString("deliveryFeeLocation") != null? Integer.parseInt((String) stepExecution.getJobExecution().getJobParameters().getString("deliveryFeeLocation")) : 0);
    	BatchSchedule.get().setSellerSelector1((String) stepExecution.getJobExecution().getJobParameters().getString("sellerSelector1"));
    	BatchSchedule.get().setSellerSelector2((String) stepExecution.getJobExecution().getJobParameters().getString("sellerSelector2"));
    	BatchSchedule.get().setSellerSelector3((String) stepExecution.getJobExecution().getJobParameters().getString("sellerSelector3"));
    	BatchSchedule.get().setSellerLocation(stepExecution.getJobExecution().getJobParameters().getString("sellerLocation") != null? Integer.parseInt((String) stepExecution.getJobExecution().getJobParameters().getString("sellerLocation")) : 0);
    	BatchSchedule.get().setUrlSelector1((String) stepExecution.getJobExecution().getJobParameters().getString("urlSelector1"));
    	BatchSchedule.get().setUrlSelector2((String) stepExecution.getJobExecution().getJobParameters().getString("urlSelector2"));
    	BatchSchedule.get().setUrlSelector3((String) stepExecution.getJobExecution().getJobParameters().getString("urlSelector3"));
    	BatchSchedule.get().setNextButtonSelector((String) stepExecution.getJobExecution().getJobParameters().getString("nextButtonSelector"));
    	BatchSchedule.get().setImageSelector((String) stepExecution.getJobExecution().getJobParameters().getString("imageSelector"));
    	stepEx.set(stepExecution);
    	jobExecution.set(stepExecution.getJobExecution());
	}

	@Override
	// read 메서드 종료 후 호출
	public ExitStatus afterStep(StepExecution stepExecution) {
		ExecutionContext executionContext = stepExecution.getJobExecution().getExecutionContext();
		executionContext.put("target", BatchSchedule.get().getTarget());
		return ExitStatus.COMPLETED;
	}
}
