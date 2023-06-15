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
import com.example.batch.Service.BatchScheduleService;
import com.example.batch.Service.GoodsService;
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
	private static final int MAX_THREADS = 1;
	private static ConfigurableApplicationContext context;
	private static int driver_num = -1;
	private static String account;
	private static int startBatchNum;
	private static int endBatchNum;
	
	@Value("${naver.api-url}")
    private String API_URL;
	@Value("${naver.client-id}")
    private String CLIENT_ID;
	@Value("${naver.client-secret}")
    private String CLIENT_SECRET;
	  
	public static void main(String[] args) {
		context = SpringApplication.run(BatchApplication.class, args);
	}
	
	@Override
	@Transactional("txManager")
	  public void run(String... args) throws Exception 
	  {
		// TODO Auto-generated method stub
	      String query = "신발";
	        int display = 100;
	        int start = 999;
	        String sort = "sim";

	        try {
	            // 쿼리를 UTF-8로 인코딩
	            String encodedQuery = URLEncoder.encode(query, "UTF-8");

	            // API 요청 URL 생성
	            String apiUrl = API_URL + "?query=" + encodedQuery + "&display=" + display + "&start=" + start + "&sort=" + sort;

	            // API 요청을 위한 HttpURLConnection 객체 생성
	            URL url = new URL(apiUrl);
	            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	            connection.setRequestMethod("GET");

	            // 요청 헤더 설정
	            connection.setRequestProperty("X-Naver-Client-Id", CLIENT_ID);
	            connection.setRequestProperty("X-Naver-Client-Secret", CLIENT_SECRET);

	            // API 응답 확인
	            int responseCode = connection.getResponseCode();
	            if (responseCode == HttpURLConnection.HTTP_OK) {
	                // API 응답 데이터 읽기
	                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	                String line;
	                StringBuilder response = new StringBuilder();
	                while ((line = in.readLine()) != null) {
	                    response.append(line);
	                }
	                in.close();
	                
	                System.out.println(response.toString());

	                // 응답 데이터 출력
//	                ObjectMapper objectMapper = new ObjectMapper();
//	                NaverShoppingResult result = objectMapper.readValue(response.toString(), NaverShoppingResult.class);

//	                Document doc;
//	                
//	                // items 출력
//	                for (NaverShoppingItem item : result.getItems()) {
//	                   String title = item.getTitle().replaceAll("<b>", "").replaceAll("</b>", "");
//	                    System.out.println("Title: " + title);
//	                    System.out.println("Link: " + item.getLink());
//	                doc = Jsoup.connect(item.getLink()).get();
//	                Elements elems = doc.select("#wrap");
//	                String price = null;
//	                
//	                if(elems.size() == 0) new Exception("elem size is 0");
//	                for(Element elem : elems) {
//	                   price = elems.select("div > p").get(0).text().replaceAll("[^0-9]", "");
//	                }
//	                
//	                    System.out.println("Image: " + item.getImage());
//	                    System.out.println("Before Lowest Price: " + item.getLprice());
//	                    System.out.println("After Lowest Price: " + price);
//	                    String titleUrl = "";
//	                    if(!item.getMallName().equals("") && !item.getMallName().equals("네이버")) {
//	                       titleUrl += item.getMallName() + " ";
//	                    }
//	                    String deliveryUrl = "https://search.shopping.naver.com/search/all?maxPrice="
//	                                   + item.getLprice()
//	                                   + "&minPrice="
//	                                   + item.getLprice()
//	                                   + "&query="
//	                                   + URLEncoder.encode("\"" + titleUrl + title + "\"", "UTF-8");
//	                    doc = Jsoup.connect(deliveryUrl).get();
//	                elems = doc.select("div#content > div:nth-child(1) > div:nth-child(2) > div:nth-child(1)");
//	                Integer deliveryFee = null;
//	                for(Element elem : elems) {
//	                   String mallName = elem.select("div:nth-child(1) > div:nth-child(1) > div:nth-child(3) > div:nth-child(1) > a:nth-child(1)").get(0).text();
//	                   if(mallName.equals("쇼핑몰별 최저가")) mallName = "네이버";
//	                   if(item.getMallName().equals(mallName)) {
//	                      if(mallName.equals("네이버")) break;
//	                      Elements elemTarget = elem.select("div:nth-child(1) > div:nth-child(1) > div:nth-child(2) > div:nth-child(2) > strong:nth-child(2)");
//	                      if(elemTarget.size() > 0) {
//	                         deliveryFee = makeDeliveryFee(elemTarget.get(0).text());
//	                         break;
//	                      }
//	                      elemTarget = elem.select("div:nth-child(1) > div:nth-child(1) > div:nth-child(2) > div:nth-child(2) > strong:nth-child(1)");
//	                      if(elemTarget.size() > 0) {
//	                         deliveryFee = makeDeliveryFee(elemTarget.get(0).text());
//	                         break;
//	                      }
//	                      elemTarget = elem.select("div:nth-child(1) > div:nth-child(1) > div:nth-child(2) > div:nth-child(3) > strong:nth-child(2)");
//	                      if(elemTarget.size() > 0) {
//	                         deliveryFee = makeDeliveryFee(elemTarget.get(0).text());
//	                         break;
//	                      }
//	                      throw new Exception("deliveryFee is not detected");
//	                   }
//	                }
//	                System.out.println("DeliveryFee: " + deliveryFee);
//	                    System.out.println("Mall Name: " + item.getMallName());
//	                    System.out.println("Category: " + item.getCategory1() + " > " + item.getCategory2() + " > " + item.getCategory3());
//	                    System.out.println();
//	                }
	            } else {
	               throw new Exception("API 요청에 실패했습니다. 응답 코드: " + responseCode);
	            }

	            // 연결 해제
	            connection.disconnect();
	        } catch (IOException e) {
	            e.printStackTrace();
	            throw e;
	        }
		
//		if (args.length > 0) {
//            account = args[0];
//            startBatchNum = Integer.parseInt(args[1]);
//            endBatchNum = Integer.parseInt(args[2]);
//        }
//		
//		//삭제로직
//		deleteDocumentsByQuery(client);
//        client.close();
//		goodsService.deleteGoodsList();
//		
//		List<BatchSchedule> batchSchedules = batchScheduleService.getBatchScheduleList(startBatchNum, endBatchNum);
//		
//		int numThreads = Math.min(MAX_THREADS, batchSchedules.size());
//
//        for (int i = 0; i < numThreads; i++) {
//        	List<BatchSchedule> subList = new ArrayList<BatchSchedule>();
//        	for(int j = i; j < batchSchedules.size(); j += MAX_THREADS) {
//        		subList.add(batchSchedules.get(j));
//        	}
//
//            taskExecutor.execute(() -> {
//            	log.get().info("batchSchedules SIZE : " + subList.size());
//            	driver_num++;
//            	for (BatchSchedule batchSchedule : subList) {
//                	log.get().info("batchSchedules : " + batchSchedule.getUrl());
//                    JobParameters jobParameters = new JobParametersBuilder()
//                    		.addString("batchNum", Integer.toString(batchSchedule.getBatchNum()))
//                    		.addString("batchName", batchSchedule.getBatchName())
//                            .addString("url", batchSchedule.getUrl())
//                            .addString("totalSelector", batchSchedule.getTotalSelector())
//                            .addString("titleSelector1", batchSchedule.getTitleSelector1())
//                            .addString("titleSelector2", batchSchedule.getTitleSelector2())
//                            .addString("titleSelector3", batchSchedule.getTitleSelector3())
//                            .addString("titleLocation", Integer.toString(batchSchedule.getTitleLocation() != null? batchSchedule.getTitleLocation() : 0))
//                            .addString("priceSelector1", batchSchedule.getPriceSelector1())
//                            .addString("priceSelector2", batchSchedule.getPriceSelector2())
//                            .addString("priceSelector3", batchSchedule.getPriceSelector3())
//                            .addString("priceLocation", Integer.toString(batchSchedule.getPriceLocation() != null? batchSchedule.getPriceLocation() : 0))
//                            .addString("deliveryFeeSelector1", batchSchedule.getDeliveryFeeSelector1())
//                            .addString("deliveryFeeSelector2", batchSchedule.getDeliveryFeeSelector2())
//                            .addString("deliveryFeeSelector3", batchSchedule.getDeliveryFeeSelector3())
//                            .addString("deliveryFeeLocation", Integer.toString(batchSchedule.getDeliveryFeeLocation() != null? batchSchedule.getDeliveryFeeLocation() : 0))
//                            .addString("sellerSelector1", batchSchedule.getSellerSelector1())
//                            .addString("sellerSelector2", batchSchedule.getSellerSelector2())
//                            .addString("sellerSelector3", batchSchedule.getSellerSelector3())
//                            .addString("sellerLocation", Integer.toString(batchSchedule.getSellerLocation() != null? batchSchedule.getSellerLocation() : 0))
//                            .addString("urlSelector1", batchSchedule.getUrlSelector1())
//                            .addString("urlSelector2", batchSchedule.getUrlSelector2())
//                            .addString("urlSelector3", batchSchedule.getUrlSelector3())
//                            .addString("nextButtonSelector", batchSchedule.getNextButtonSelector())
//                            .addString("imageSelector", batchSchedule.getImageSelector())
//                            .addString("account",  account)
//                            .addLong("jobCount", (long) batchSchedules.size())
//                            .addLong("time", System.currentTimeMillis())
//                            .addLong("driver_num", (long) driver_num)
//                            .toJobParameters();
//                    try {
//	                    jobLauncher.run(simpleJobConfiguration.myJob(), jobParameters);
//                    } catch(Exception e) {
//                    	e.printStackTrace();
//                    	SpringApplication.exit(context);
////                    	System.exit(1);
//                    }
//                }
//            });
//        }

	  }
	
	private Integer makeDeliveryFee(String input) throws Exception{
	      if (input.contains("배송비")) {
	            // "배송비" 다음부터 "~ 원"까지의 값을 추출
	            int startIndex = input.indexOf("배송비") + "배송비".length();
	            int endIndex = input.indexOf("원", startIndex);

	            if (startIndex != -1 && endIndex != -1) {
	                String deliveryFeeString = input.substring(startIndex, endIndex);
	                deliveryFeeString = deliveryFeeString.replaceAll("[^0-9]", ""); // 숫자만 추출

	                if (!deliveryFeeString.isEmpty()) {
	                    int deliveryFee = Integer.parseInt(deliveryFeeString);
	                    return deliveryFee;
	                }
	                else return 0;
	            }
	            else return 0;
	        }
	      else {
	         throw new Exception("배송비 is not contained");
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
