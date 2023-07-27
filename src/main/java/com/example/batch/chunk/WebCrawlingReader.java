package com.example.batch.chunk;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;
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
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import com.example.batch.Domain.BatchSchedule;
import com.example.batch.Domain.Goods;
import com.example.batch.Domain.Product;
import com.example.batch.Domain.ProductInfoResponse;
import com.example.batch.Domain.ProductSearchResponse;
import com.example.batch.Domain.Products;
import com.example.batch.Domain.Request;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;

@Component
@Profile("main")
public class WebCrawlingReader implements ItemReader<List<Goods>>, StepExecutionListener {

	private ThreadLocal<Logger> log = ThreadLocal.withInitial(() -> {
    	return LoggerFactory.getLogger(this.getClass());
    });
    private static final ThreadLocal<Integer> totalSize = new ThreadLocal<>();
    private static final ThreadLocal<Integer> insertSize = new ThreadLocal<>();
    private static final ThreadLocal<BatchSchedule> batchSchedule = new ThreadLocal<>();
    private static final ThreadLocal<Integer> pageNumber = new ThreadLocal<>();
    private static final ThreadLocal<JobExecution> jobExecution = new ThreadLocal<>();
    private static final ThreadLocal<StepExecution> stepEx = new ThreadLocal<>();
    private static final ThreadLocal<Integer> Count = new ThreadLocal<>();
    private static String account; 
    
	@Value("${11st.api-url}")
    private String API_URL;
	@Value("${11st.api-key}")
    private String API_KEY;
    
	@Override
	public List<Goods> read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		// TODO Auto-generated method stub
		String query = batchSchedule.get().getTarget();
		// chunk 당 200개의 데이터
        int display = 200;
        List<Goods> goodsList = new ArrayList<Goods>();
        int total = 0;
        int insert = 0;
        // 검색어당 최대 25페이지(5,000건) 적재
        if(pageNumber.get() > 25) return null;
        
        String responseXml;
        String apiUrl;
        URL url;
        HttpURLConnection connection;
        int responseCode;
        BufferedReader in;
        String line;
        StringBuilder response;
        
        // 제대로 된 응답을 받을 때까지 무한루프
        while(true) {
        	// API 요청 시 매 요청마다 0.5초의 간격 부여
        	Thread.currentThread().sleep(750);
        	log.get().info("Current PageNumber : " + pageNumber.get());
            // 쿼리를 UTF-8로 인코딩
            String encodedQuery = URLEncoder.encode(query, "UTF-8");

            // API 요청 URL 생성
            apiUrl = API_URL + "?key=" + API_KEY + "&apiCode=ProductSearch" + "&keyword=" + encodedQuery + "&pageNum=" + pageNumber.get() + "&pageSize=" + display;

            // API 요청을 위한 HttpURLConnection 객체 생성
            url = new URL(apiUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            
            // API 응답 확인
            responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // API 응답 데이터 읽기
                in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "EUC-KR"));
                response = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                
            	responseXml = response.toString();
            }
            else throw new Exception("API 요청에 실패했습니다. 응답 코드: " + responseCode);
            
            // 제대로 된 응답을 받지 못한 경우 해당 페이지 넘김
            if(responseXml == null || responseXml.equals("")) {
            	pageNumber.set(pageNumber.get() + 1);
            	if(pageNumber.get() > 25) break;
            	continue;
            }
            else break;
    	}
        
        if(pageNumber.get() > 25) return null;
        
        JAXBContext jaxbContext = JAXBContext.newInstance(ProductSearchResponse.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        // 응답을 ProductSearchResponse 객체로 만들어서 사용
        ProductSearchResponse responseString = (ProductSearchResponse) unmarshaller.unmarshal(new StringReader(responseXml));

        Request request = responseString.getRequest();
        log.get().info("Processing Time: " + request.getProcessingTime());

        Products products = responseString.getProducts();
        log.get().info("Total Count: " + products.getTotalCount());

        List<Product> productList = products.getProductList();
        
        // 더 이상 상품을 가지고 있지 않다면 job 종료시키기 위해 return null
        if(productList == null) return null;
        
        // 전체 상품개수 누적
        total += productList.size();
        
        for (Product product : productList) {
        	// API 요청 시 매 요청마다 0.5초의 간격 부여
        	Thread.currentThread().sleep(750);
        	Goods goods = new Goods();
        	
        	// 총 3번까지 재시도 가능
        	Count.set(0);
			while(true) {
				Boolean isOk = true;
				
				// API 요청 URL 생성
	            apiUrl = API_URL + "?key=" + API_KEY + "&apiCode=ProductInfo" + "&productCode=" + product.getProductCode();

	            // API 요청을 위한 HttpURLConnection 객체 생성
	            url = new URL(apiUrl);
	            connection = (HttpURLConnection) url.openConnection();
	            connection.setRequestMethod("GET");
	            
	            // API 응답 확인
	            responseCode = connection.getResponseCode();
	            if (responseCode == HttpURLConnection.HTTP_OK) {
	                // API 응답 데이터 읽기
	                in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "EUC-KR"));
	                response = new StringBuilder();
	                while ((line = in.readLine()) != null) {
	                    response.append(line);
	                }
	                in.close();
	                
                	responseXml = response.toString();
                	
                	// 제대로 된 응답을 받지 못하거나 오류가 있는 경우 해당 상품 넘김
                	if(responseXml == null || responseXml.equals("")) break;
                	if(responseXml.contains("ErrorResponse")) {
                		log.get().info("ErrorResponse is occured");
                		break;
                	}
                	
                    jaxbContext = JAXBContext.newInstance(ProductInfoResponse.class);
                    unmarshaller = jaxbContext.createUnmarshaller();
                    
                    // 응답을 ProductInfoResponse 객체로 만들어서 사용
                    ProductInfoResponse responseString2 = (ProductInfoResponse) unmarshaller.unmarshal(new StringReader(responseXml));
                    Product product2 = responseString2.getProduct();
                    
                    log.get().info("image: " + product.getProductImage300());
                    log.get().info("detail: " + product.getDetailPageUrl());
                    goods.setImage(product.getProductImage300());
                	goods.setDetail(product.getDetailPageUrl());
                	goods.setProduct_code(product.getProductCode());
                	
                	// 시간대를 Asia/Seoul로 설정
        	        TimeZone seoulTimeZone = TimeZone.getTimeZone("Asia/Seoul");
        	        TimeZone.setDefault(seoulTimeZone);
        	        // 현재 날짜를 YYYY-MM-DD 형식으로 가져오기
        	        LocalDate currentDate = LocalDate.now();
        	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        	        String formattedDate = currentDate.format(formatter);
        	        goods.setInsertion_date(formattedDate);
                	
                	log.get().info("title : {}", product.getProductName());
					goods.setName(product.getProductName());
					if(product.getProductName() == null || product.getProductName().equals("")) {
						isOk = false;
					}
					
					log.get().info("price : {}", product.getSalePrice());
					goods.setPrice(product.getSalePrice());
					if((Integer)product.getSalePrice() == null || product.getSalePrice() == 0) {
						isOk = false;
					}
					
					Integer deliveryFee = null;
					String delivery = product2.getShipFee();
					log.get().info("delivery Check : {}", product2.getShipFee());
					StringTokenizer st = new StringTokenizer(delivery, " ");
					boolean isExist = false;
					while(st.hasMoreTokens()) {
						String token = st.nextToken();
						
						// 배송비 문자열 중 무료배송 CASE
						if(token.contains("무료") || token.contains("SMS") || token.contains("없음")) {
							deliveryFee = 0;
							isExist = true;
							break;
						}
						
						// 배송비 문자열 중 유료배송 CASE
						if(token.contains("원")) {
							
							// 배송비 문자열 중 "/"이 포함될 경우 단일배송비가 아닌 여러 CASE가 존재하므로 별도확인필요(null로 INSERT)
							if(!delivery.contains("/")) deliveryFee = Integer.parseInt(token.replaceAll("[^0-9]", ""));
							isExist = true;
							break;
						}
					}
					
					// 배송비 문자열 중 유료배송이면서 단일배송비가 아닌 여러 CASE가 존재하므로 별도확인필요(null로 INSERT)
					if(!isExist && (delivery.contains("착불") || delivery.contains("참조"))) {
						isExist = true;
					}
					if(isExist) {
						log.get().info("deliveryFee : {}", deliveryFee);
						goods.setDeliveryfee(deliveryFee);
					}
					else isOk = false;
					
					goods.setSellid(product.getSellerNick());
					log.get().info("seller : {}", product.getSellerNick());
					if(product.getSellerNick() == null || product.getSellerNick().equals("")) {
						isOk = false;
					}
					
					// 앞선 정보들 중 하나라도 null이거나 데이터가 존재하지 않는 경우 isOk = false
					if(isOk) {
						goodsList.add(goods);
						// INSERT 상품개수 누적
						insert++;
						break;
					}
					
					Count.set(Count.get() + 1);
                	if(Count.get() > 3) {
                		throw new Exception("Product API callCount over 3");
                	}
	            }
			}
        }

        // 연결 해제
        connection.disconnect();
        
        log.get().info("target : " + total + ", inserted : " + insert);
        totalSize.set(totalSize.get() + total);
        insertSize.set(insertSize.get() + insert);
        pageNumber.set(pageNumber.get() + 1);
        log.get().info("#### crawling END ####");
        
        if(goodsList.size() == 0) return null;
        return goodsList;
	}

	@Override
	// read 메서드 시작하기 전 호출
	public void beforeStep(StepExecution stepExecution) {
		// StepExecution에서 String으로 받아온 BatchSchedule 정보로 객체구현
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
    	batchSchedule.get().setDeliveryFeeSelector4((String) stepExecution.getJobExecution().getJobParameters().getString("deliveryFeeSelector4"));
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
    	log.get().info("target : " + batchSchedule.get().getTarget());
    	
    	stepEx.set(stepExecution);
    	jobExecution.set(stepExecution.getJobExecution());
    	account = (String) stepExecution.getJobExecution().getJobParameters().getString("account");
		
    	// 이전 실행에서 저장한 pageNum, totalSize, insertSize 사용(job 단위로 누적 후 Slack 알림발송)
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
        if (executionContext.containsKey("insertSize")) {
        	insertSize.set((int) executionContext.get("insertSize"));
        } else {
        	insertSize.set(0); // 최초 실행 시 insertSize은 0로 초기화
        }
	}

	@Override
	// read 메서드 종료 후 호출
	public ExitStatus afterStep(StepExecution stepExecution) {
		ExecutionContext executionContext = stepExecution.getJobExecution().getExecutionContext();
		executionContext.put("target", batchSchedule.get().getTarget());
		executionContext.put("account", account);
		// pageNum, totalSize, insertSize 저장(job 단위로 누적 후 Slack 알림발송)
        executionContext.put("startPageNum", pageNumber.get());
        executionContext.put("totalSize", totalSize.get());
        executionContext.put("insertSize", insertSize.get());
		return ExitStatus.COMPLETED;
	}
}
