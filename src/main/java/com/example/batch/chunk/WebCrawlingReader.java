package com.example.batch.chunk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
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
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.example.batch.Domain.BatchSchedule;
import com.example.batch.Domain.Goods;
import com.example.batch.Domain.NaverShoppingItem;
import com.example.batch.Domain.NaverShoppingResult;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class WebCrawlingReader implements ItemReader<List<Goods>>, StepExecutionListener {

	private ThreadLocal<Logger> log = ThreadLocal.withInitial(() -> {
    	return LoggerFactory.getLogger(this.getClass());
    });
    private static final ThreadLocal<Integer> totalSize = new ThreadLocal<>();
    private static final ThreadLocal<BatchSchedule> batchSchedule = new ThreadLocal<>();
    private static final ThreadLocal<Integer> pageNumber = new ThreadLocal<>();
    private static final ThreadLocal<JobExecution> jobExecution = new ThreadLocal<>();
    private static final ThreadLocal<StepExecution> stepEx = new ThreadLocal<>();
    private static String account;
    
	@Value("${naver.api-url}")
    private String API_URL;
	@Value("${naver.client-id}")
    private String CLIENT_ID;
	@Value("${naver.client-secret}")
    private String CLIENT_SECRET;
    
	@Override
	public List<Goods> read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		// TODO Auto-generated method stub
		String query = batchSchedule.get().getTarget();
        int display = 100;
        int start = (pageNumber.get() - 1) * 100 + 1;
        List<Goods> goodsList = new ArrayList<Goods>();
        String sort = "sim";
        int total = 0;
        
        if(start > 1000) return null;
        
        try {
        	log.get().info("Current PageNumber : " + pageNumber.get());
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
                
                // 응답 데이터 출력
                ObjectMapper objectMapper = new ObjectMapper();
                NaverShoppingResult result = objectMapper.readValue(response.toString(), NaverShoppingResult.class);
                total += result.getItems().length;
                Document doc;
                
                // items 출력
                for (NaverShoppingItem item : result.getItems()) {
                	String title = item.getTitle().replaceAll("<b>", "").replaceAll("</b>", "");
                	Goods goods = new Goods();
                	goods.setName(removeSpecialCharacters(title));
                	goods.setDetail(item.getLink());
					goods.setImage(item.getImage());
					goods.setPrice(Integer.parseInt(item.getLprice()));
					
                    String titleUrl = "";
                    if(!item.getMallName().equals("") && !item.getMallName().equals("네이버")) {
                       titleUrl += item.getMallName() + " ";
                    }
                    String deliveryUrl = "https://search.shopping.naver.com/search/all?maxPrice="
                                   + item.getLprice()
                                   + "&minPrice="
                                   + item.getLprice()
                                   + "&query="
                                   + URLEncoder.encode("\"" + titleUrl + title + "\"", "UTF-8");
                    synchronized (this) {
            		    doc = Jsoup.connect(item.getLink()).get();
            		}
                    Elements elems = doc.select(batchSchedule.get().getTotalSelector());
	                Integer deliveryFee = null;
	                for(Element elem : elems) {
	                   String mallName = elem.select(batchSchedule.get().getSellerSelector1()).get(0).text();
	                   if(mallName.equals("쇼핑몰별 최저가")) mallName = "네이버";
	                   if(item.getMallName().equals(mallName)) {
	                      if(mallName.equals("네이버")) break;
	                      Elements elemTarget = elem.select(batchSchedule.get().getDeliveryFeeSelector1());
	                      if(elemTarget.size() > 0) {
	                         deliveryFee = makeDeliveryFee(elemTarget.get(0).text());
	                         break;
	                      }
	                      elemTarget = elem.select(batchSchedule.get().getDeliveryFeeSelector2());
	                      if(elemTarget.size() > 0) {
	                         deliveryFee = makeDeliveryFee(elemTarget.get(0).text());
	                         break;
	                      }
	                      elemTarget = elem.select(batchSchedule.get().getDeliveryFeeSelector3());
	                      if(elemTarget.size() > 0) {
	                         deliveryFee = makeDeliveryFee(elemTarget.get(0).text());
	                         break;
	                      }
	                      throw new Exception("deliveryFee is not detected");
	                   }
	                }
	                
                goods.setDeliveryfee(deliveryFee);
                goods.setSellid(removeSpecialCharacters(item.getMallName()));
                goodsList.add(goods);
                }
            } else {
               throw new Exception("API 요청에 실패했습니다. 응답 코드: " + responseCode);
            }

            // 연결 해제
            connection.disconnect();
            
            log.get().info("target : " + total + ", inserted : " + total);
            totalSize.set(totalSize.get() + total);
            pageNumber.set(pageNumber.get() + 1);
            log.get().info("#### crawling END ####");
            
            return goodsList;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		// TODO Auto-generated method stub
		batchSchedule.set(new BatchSchedule());
    	batchSchedule.get().setBatchName((String) stepExecution.getJobExecution().getJobParameters().getString("batchName"));
    	batchSchedule.get().setUrl((String) stepExecution.getJobExecution().getJobParameters().getString("url"));
    	batchSchedule.get().setTarget((String) stepExecution.getJobExecution().getJobParameters().getString("target"));
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
    	stepEx.set(stepExecution);
    	account = (String) stepExecution.getJobExecution().getJobParameters().getString("account");
    	log.get().info("url : " + batchSchedule.get().getUrl());
		// 이전 실행에서 저장한 pageNum을 가져옴
        ExecutionContext executionContext = stepExecution.getJobExecution().getExecutionContext();
        if (executionContext.containsKey("startPageNum")) {
        	pageNumber.set((int) executionContext.get("startPageNum"));
        } else {
        	pageNumber.set(1); // 최초 실행 시 pageNum은 1로 초기화
        }
        if (executionContext.containsKey("totalSize")) {
        	totalSize.set((int) executionContext.get("totalSize"));
        } else {
        	totalSize.set(0); // 최초 실행 시 totalSize은 0로 초기화
        }
		jobExecution.set(stepExecution.getJobExecution());
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		// TODO Auto-generated method stub
		JobExecution jobExecution = stepExecution.getJobExecution();
        jobExecution.getExecutionContext().put("totalSize", totalSize.get());
        jobExecution.getExecutionContext().put("url", batchSchedule.get().getUrl());
        jobExecution.getExecutionContext().put("account", account);
        // pageNum을 저장하여 다음 실행에 사용할 수 있도록 ExecutionContext에 저장
        ExecutionContext executionContext = stepExecution.getJobExecution().getExecutionContext();
        executionContext.put("startPageNum", pageNumber.get());
        executionContext.put("totalSize", totalSize.get());
		return ExitStatus.COMPLETED;
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
    
    public static String removeSpecialCharacters(String text) {
        // 정규 표현식을 사용하여 특수 문자 제거
        String pattern = "[^a-zA-Z0-9가-힣\\s]";
        text = text.replaceAll(pattern, "");
        return text;
    }
}
