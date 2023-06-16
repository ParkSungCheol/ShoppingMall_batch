package com.example.batch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.annotation.Transactional;
import com.example.batch.Domain.BatchSchedule;
import com.example.batch.Domain.NaverShoppingItem;
import com.example.batch.Domain.NaverShoppingResult;
import com.example.batch.Service.BatchScheduleService;
import com.example.batch.Service.GoodsService;
import com.example.batch.job.SimpleJobConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
public class BatchApplication implements CommandLineRunner {

	@Autowired
	JobLauncher jobLauncher;
	@Autowired 
	SimpleJobConfiguration simpleJobConfiguration; 
	@Autowired
    private TaskExecutor taskExecutor;
	@Autowired
	private GoodsService goodsService;
	@Autowired
	private RestHighLevelClient client;
	private static final String INDEX_NAME = "goods";
    private static final int BATCH_SIZE = 800;
	@Autowired
	private BatchScheduleService batchScheduleService;
	private ThreadLocal<Logger> log = ThreadLocal.withInitial(() -> {
    	return LoggerFactory.getLogger(this.getClass());
    });
	private static final int MAX_THREADS = 5;
	private static ConfigurableApplicationContext context;
	private static int driver_num = -1;
	private static String account;
	private static int startBatchNum;
	private static int endBatchNum;
	
	public static void main(String[] args) {
		context = SpringApplication.run(BatchApplication.class, args);
	}
	
	@Override
	@Transactional("txManager")
	  public void run(String... args) throws Exception 
	  {
		// TODO Auto-generated method stub
		if (args.length > 0) {
            account = args[0];
            startBatchNum = Integer.parseInt(args[1]);
            endBatchNum = Integer.parseInt(args[2]);
        }
		
		//삭제로직
		deleteDocumentsByQuery(client);
        client.close();
		goodsService.deleteGoodsList();
		
		List<BatchSchedule> batchSchedules = batchScheduleService.getBatchScheduleList(startBatchNum, endBatchNum);
		
		int numThreads = Math.min(MAX_THREADS, batchSchedules.size());

        for (int i = 0; i < numThreads; i++) {
        	List<BatchSchedule> subList = new ArrayList<BatchSchedule>();
        	for(int j = i; j < batchSchedules.size(); j += MAX_THREADS) {
        		subList.add(batchSchedules.get(j));
        	}

            taskExecutor.execute(() -> {
            	log.get().info("batchSchedules SIZE : " + subList.size());
            	driver_num++;
            	for (BatchSchedule batchSchedule : subList) {
                	log.get().info("batchSchedules : " + batchSchedule.getUrl());
                    JobParameters jobParameters = new JobParametersBuilder()
                    		.addString("batchNum", Integer.toString(batchSchedule.getBatchNum()))
                    		.addString("batchName", batchSchedule.getBatchName())
                            .addString("url", batchSchedule.getUrl())
                            .addString("target", batchSchedule.getTarget())
                            .addString("totalSelector", batchSchedule.getTotalSelector())
                            .addString("titleSelector1", batchSchedule.getTitleSelector1())
                            .addString("titleSelector2", batchSchedule.getTitleSelector2())
                            .addString("titleSelector3", batchSchedule.getTitleSelector3())
                            .addString("titleLocation", Integer.toString(batchSchedule.getTitleLocation() != null? batchSchedule.getTitleLocation() : 0))
                            .addString("priceSelector1", batchSchedule.getPriceSelector1())
                            .addString("priceSelector2", batchSchedule.getPriceSelector2())
                            .addString("priceSelector3", batchSchedule.getPriceSelector3())
                            .addString("priceLocation", Integer.toString(batchSchedule.getPriceLocation() != null? batchSchedule.getPriceLocation() : 0))
                            .addString("deliveryFeeSelector1", batchSchedule.getDeliveryFeeSelector1())
                            .addString("deliveryFeeSelector2", batchSchedule.getDeliveryFeeSelector2())
                            .addString("deliveryFeeSelector3", batchSchedule.getDeliveryFeeSelector3())
                            .addString("deliveryFeeLocation", Integer.toString(batchSchedule.getDeliveryFeeLocation() != null? batchSchedule.getDeliveryFeeLocation() : 0))
                            .addString("sellerSelector1", batchSchedule.getSellerSelector1())
                            .addString("sellerSelector2", batchSchedule.getSellerSelector2())
                            .addString("sellerSelector3", batchSchedule.getSellerSelector3())
                            .addString("sellerLocation", Integer.toString(batchSchedule.getSellerLocation() != null? batchSchedule.getSellerLocation() : 0))
                            .addString("urlSelector1", batchSchedule.getUrlSelector1())
                            .addString("urlSelector2", batchSchedule.getUrlSelector2())
                            .addString("urlSelector3", batchSchedule.getUrlSelector3())
                            .addString("nextButtonSelector", batchSchedule.getNextButtonSelector())
                            .addString("imageSelector", batchSchedule.getImageSelector())
                            .addString("account",  account)
                            .addLong("jobCount", (long) batchSchedules.size())
                            .addLong("time", System.currentTimeMillis())
                            .addLong("driver_num", (long) driver_num)
                            .toJobParameters();
                    try {
	                    jobLauncher.run(simpleJobConfiguration.myJob(), jobParameters);
                    } catch(Exception e) {
                    	e.printStackTrace();
                    	SpringApplication.exit(context);
//                    	System.exit(1);
                    }
                }
            });
        }
	  }
	
	private void deleteDocumentsByQuery(RestHighLevelClient client) throws IOException {
		// Create a search request
        SearchRequest searchRequest = new SearchRequest(INDEX_NAME);

        // Set the search query
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.termQuery("is_deleted", 1));
        searchSourceBuilder.size(BATCH_SIZE);
        searchRequest.source(searchSourceBuilder);

        // Set the scroll timeout
        Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
        searchRequest.scroll(scroll);

        // Execute the search request
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        String scrollId = searchResponse.getScrollId();

        // Process and delete documents in batches
        BulkRequest bulkRequest = new BulkRequest();
        while (searchResponse.getHits().getHits().length > 0) {
            for (SearchHit hit : searchResponse.getHits().getHits()) {
                String documentId = hit.getId();
                DeleteRequest deleteRequest = new DeleteRequest(INDEX_NAME, documentId);
                bulkRequest.add(deleteRequest);
            }

            // Delete documents in bulk
            BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
            if (bulkResponse.hasFailures()) {
                // Handle bulk delete failures
            	log.get().info(bulkResponse.buildFailureMessage());
            	break;
            }

            // Clear the bulk request
            bulkRequest.requests().clear();

            // Scroll to the next batch
            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);
            searchResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);
            scrollId = searchResponse.getScrollId();
        }

        // Clear the scroll
        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);
        ClearScrollResponse clearScrollResponse = client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
	}
}
