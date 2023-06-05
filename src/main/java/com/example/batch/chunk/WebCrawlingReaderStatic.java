package com.example.batch.chunk;

import java.io.IOException;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Component;
import com.example.batch.Domain.BatchSchedule;
import com.example.batch.Domain.Goods;

@Component
public class WebCrawlingReaderStatic implements ItemReader<List<Goods>>, StepExecutionListener {

	private ThreadLocal<Logger> log = ThreadLocal.withInitial(() -> {
    	return LoggerFactory.getLogger(this.getClass());
    });
    private static final ThreadLocal<Integer> totalSize = new ThreadLocal<>();
    private static final ThreadLocal<Integer> totalSkippedSize = new ThreadLocal<>();
    private static final ThreadLocal<BatchSchedule> batchSchedule = new ThreadLocal<>();
    private static final ThreadLocal<Integer> pageNumber = new ThreadLocal<>();
    private static final ThreadLocal<JobExecution> jobExecution = new ThreadLocal<>();
    private static final ThreadLocal<Integer> driver_num = new ThreadLocal<>();
    private static String account;
    
	@Override
	public List<Goods> read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		String URL = batchSchedule.get().getUrl();
		int pageNum = 1;
		Document doc;

		while(true) {
			try {
				URL += ("&p=" + pageNum);
				doc = Jsoup.connect(URL).get();
				Elements elems = doc.select(batchSchedule.get().getTotalSelector());
				if(elems.size() == 0) break;
				for(Element elem : elems) {
					String title = elem.select(batchSchedule.get().getTitleSelector1()).get(batchSchedule.get().getTitleLocation()).text();
					log.get().info("title : " + title);
					String price = elem.select(batchSchedule.get().getPriceSelector1()).get(batchSchedule.get().getPriceLocation()).text();
					log.get().info("price : " + price);
					String deliveryFee = elem.select(batchSchedule.get().getDeliveryFeeSelector1()).get(batchSchedule.get().getDeliveryFeeLocation()).text();
					log.get().info("deliveryFee : " + deliveryFee);
					String seller = elem.select(batchSchedule.get().getSellerSelector1()).get(batchSchedule.get().getSellerLocation()).text();
					log.get().info("seller : " + seller);
					String url = elem.select(batchSchedule.get().getUrlSelector1()).get(0).text();
					log.get().info("url : " + url);
					String image = elem.select(batchSchedule.get().getImageSelector()).get(0).text();
					log.get().info("image : " + image);
				}
				pageNum++;
				if(pageNum > 50) break;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return null;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		// TODO Auto-generated method stub
		batchSchedule.set(new BatchSchedule());
    	batchSchedule.get().setBatchName((String) stepExecution.getJobExecution().getJobParameters().getString("batchName"));
    	batchSchedule.get().setUrl((String) stepExecution.getJobExecution().getJobParameters().getString("url"));
    	batchSchedule.get().setTotalSelector((String) stepExecution.getJobExecution().getJobParameters().getString("totalSelector"));
    	batchSchedule.get().setTitleSelector1((String) stepExecution.getJobExecution().getJobParameters().getString("titleSelector1"));
    	batchSchedule.get().setTitleSelector2((String) stepExecution.getJobExecution().getJobParameters().getString("titleSelector2"));
    	batchSchedule.get().setTitleSelector3((String) stepExecution.getJobExecution().getJobParameters().getString("titleSelector3"));
    	batchSchedule.get().setTitleLocation(stepExecution.getJobExecution().getJobParameters().getString("titleLocation") != null? Integer.parseInt((String) stepExecution.getJobExecution().getJobParameters().getString("titleLocation")) : 0);
    	batchSchedule.get().setPriceSelector1((String) stepExecution.getJobExecution().getJobParameters().getString("priceSelector1"));
    	batchSchedule.get().setPriceSelector2((String) stepExecution.getJobExecution().getJobParameters().getString("priceSelector2"));
    	batchSchedule.get().setPriceSelector3((String) stepExecution.getJobExecution().getJobParameters().getString("priceSelector3"));
    	batchSchedule.get().setPriceLocation(stepExecution.getJobExecution().getJobParameters().getString("priceLocation") != null? Integer.parseInt((String) stepExecution.getJobExecution().getJobParameters().getString("priceLocation")) : 0);
    	batchSchedule.get().setDeliveryFeeSelector1((String) stepExecution.getJobExecution().getJobParameters().getString("deliveryFeeSelector1"));
    	batchSchedule.get().setDeliveryFeeSelector2((String) stepExecution.getJobExecution().getJobParameters().getString("deliveryFeeSelector2"));
    	batchSchedule.get().setDeliveryFeeSelector3((String) stepExecution.getJobExecution().getJobParameters().getString("deliveryFeeSelector3"));
    	batchSchedule.get().setDeliveryFeeLocation(stepExecution.getJobExecution().getJobParameters().getString("deliveryFeeLocation") != null? Integer.parseInt((String) stepExecution.getJobExecution().getJobParameters().getString("deliveryFeeLocation")) : 0);
    	batchSchedule.get().setSellerSelector1((String) stepExecution.getJobExecution().getJobParameters().getString("sellerSelector1"));
    	batchSchedule.get().setSellerSelector2((String) stepExecution.getJobExecution().getJobParameters().getString("sellerSelector2"));
    	batchSchedule.get().setSellerSelector3((String) stepExecution.getJobExecution().getJobParameters().getString("sellerSelector3"));
    	batchSchedule.get().setSellerLocation(stepExecution.getJobExecution().getJobParameters().getString("sellerLocation") != null? Integer.parseInt((String) stepExecution.getJobExecution().getJobParameters().getString("sellerLocation")) : 0);
    	batchSchedule.get().setUrlSelector1((String) stepExecution.getJobExecution().getJobParameters().getString("urlSelector1"));
    	batchSchedule.get().setUrlSelector2((String) stepExecution.getJobExecution().getJobParameters().getString("urlSelector2"));
    	batchSchedule.get().setUrlSelector3((String) stepExecution.getJobExecution().getJobParameters().getString("urlSelector3"));
    	batchSchedule.get().setNextButtonSelector((String) stepExecution.getJobExecution().getJobParameters().getString("nextButtonSelector"));
    	batchSchedule.get().setImageSelector((String) stepExecution.getJobExecution().getJobParameters().getString("imageSelector"));
    	account = (String) stepExecution.getJobExecution().getJobParameters().getString("account");
    	driver_num.set(stepExecution.getJobExecution().getJobParameters().getLong("driver_num").intValue());
    	log.get().info("url : " + batchSchedule.get().getUrl());
    	totalSize.set(0);
		totalSkippedSize.set(0);
		pageNumber.set(1);
		jobExecution.set(stepExecution.getJobExecution());
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		// TODO Auto-generated method stub
		JobExecution jobExecution = stepExecution.getJobExecution();
        jobExecution.getExecutionContext().put("totalSize", totalSize.get());
        jobExecution.getExecutionContext().put("totalSkippedSize", totalSkippedSize.get());
        jobExecution.getExecutionContext().put("url", batchSchedule.get().getUrl());
        jobExecution.getExecutionContext().put("account", account);
		return ExitStatus.COMPLETED;
	}
    
    public static String removeSpecialCharacters(String text) {
        // 정규 표현식을 사용하여 특수 문자 제거
        String pattern = "[^a-zA-Z0-9가-힣\\s]";
        text = text.replaceAll(pattern, "");
        return text;
    }

}
