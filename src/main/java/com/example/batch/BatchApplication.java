package com.example.batch;

import java.io.IOException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.annotation.Transactional;
import com.example.batch.Domain.BatchSchedule;
import com.example.batch.Domain.JobStatus;
import com.example.batch.Service.BatchScheduleService;
import com.example.batch.Service.JobStatusService;
import com.example.batch.job.SimpleJobConfiguration;

@SpringBootApplication
public class BatchApplication implements CommandLineRunner { 

	@Autowired
	JobLauncher jobLauncher;
	@Autowired 
	SimpleJobConfiguration simpleJobConfiguration; 
	@Autowired
    private TaskExecutor taskExecutor;
	@Autowired
	private JobStatusService jobStatusService;
	@Autowired
	private RestHighLevelClient client;
	@Autowired
	private BatchScheduleService batchScheduleService;
	private ThreadLocal<Logger> log = ThreadLocal.withInitial(() -> {
    	return LoggerFactory.getLogger(this.getClass());
    });
	private static ConfigurableApplicationContext context;
	private static String account;
	private static int startBatchNum;
	private static int endBatchNum;
	// ES 인덱스 Name
	private static final String INDEX_NAME = "goods";
	// ES Size
    private static final int BATCH_SIZE = 800;
    // 다중쓰레드 수
    private static final int MAX_THREADS = 4;
	
	public static void main(String[] args) {
		context = SpringApplication.run(BatchApplication.class, args);
	}
	
	@Override
	@Transactional("txManager")
	  public void run(String... args) throws Exception 
	  {
		if (args.length > 0) {
            account = args[0];
            startBatchNum = Integer.parseInt(args[1]);
            endBatchNum = Integer.parseInt(args[2]);
        }
		
		// 삭제로직 (is_deleted = 1인 데이터)
		deleteDocumentsByQuery(client);
        client.close();
        // MYSQL은 DELETE 속도가 너무 느려서 주석처리
		// goodsService.deleteGoodsList();
		
        // 전체 배치대상 검색어리스트 가져온 후
		List<BatchSchedule> batchSchedules = batchScheduleService.getBatchScheduleList(startBatchNum, endBatchNum);
		
		int numThreads = Math.min(MAX_THREADS, batchSchedules.size());

		// 모든 계정의 job이 시작되었음을 DB에 적재 (추후 모든 job 종료판별 시 사용)
		JobStatus jobStatus = new JobStatus();
		jobStatus.setBatchId(Integer.parseInt(account));
		jobStatusService.startJobStatus(jobStatus);
		
        for (int i = 0; i < numThreads; i++) {
        	List<BatchSchedule> subList = new ArrayList<BatchSchedule>();
        	
        	// 각 쓰레드에 분배한 후
        	for(int j = i; j < batchSchedules.size(); j += MAX_THREADS) {
        		subList.add(batchSchedules.get(j));
        	}

            taskExecutor.execute(() -> {
            	// 각 쓰레드에 할당된 배치대상 검색어 리스트를 가지고 jobLauncher Run
            	for (BatchSchedule batchSchedule : subList) {
                	log.get().info("batchSchedules : " + batchSchedule.getTarget());
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
                            .addString("deliveryFeeSelector4", batchSchedule.getDeliveryFeeSelector4())
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
                            // 추후 Slack에 보낼 계정 이름
                            .addString("account",  account)
                            // 추후 모든 job이 완료되었는지를 판별할 때 사용할 전체 job 개수
                            .addLong("jobCount", (long) batchSchedules.size())
                            // 각 job을 구별할 구분자
                            .addLong("time", System.currentTimeMillis())
                            .toJobParameters();
                    try {
	                    jobLauncher.run(simpleJobConfiguration.myJob(), jobParameters);
                    } catch(Exception e) {
                    	e.printStackTrace();
                    	// 오류 발생시 SpringApplication 종료
                    	SpringApplication.exit(context);
                    }
                }
            });
        }
	  }
	
	// ES 삭제로직
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

            // Scroll to the next batch(삭제해야할 데이터가 많은 경우 스크롤링 적용하여 데이터 가져옴)
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
