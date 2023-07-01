package com.example.batch.chunk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
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
import com.example.batch.Domain.Product;
import com.example.batch.Domain.ProductInfoResponse;
import com.example.batch.Domain.ProductSearchResponse;
import com.example.batch.Domain.Products;
import com.example.batch.Domain.Request;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

@Component
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
    private static Set<String> set = new HashSet<String>();
    
	@Value("${11st.api-url}")
    private String API_URL;
	@Value("${11st.api-key}")
    private String API_KEY;
    
	@Override
	public List<Goods> read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		// TODO Auto-generated method stub
		String query = batchSchedule.get().getTarget();
        int display = 200;
        List<Goods> goodsList = new ArrayList<Goods>();
        int total = 0;
        int insert = 0;
        if(pageNumber.get() > 25) return null;
        
        try {
        	log.get().info("Current PageNumber : " + pageNumber.get());
            // 쿼리를 UTF-8로 인코딩
            String encodedQuery = URLEncoder.encode(query, "UTF-8");

            // API 요청 URL 생성
            String apiUrl = API_URL + "?key=" + API_KEY + "&apiCode=ProductSearch" + "&keyword=" + encodedQuery + "&sortCd=N" + "&pageNum=" + pageNumber.get() + "&pageSize=" + display;

            // API 요청을 위한 HttpURLConnection 객체 생성
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            
//            log.get().info("apiUrl : {}", apiUrl);

            // API 응답 확인
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // API 응답 데이터 읽기
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "EUC-KR"));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                
                try {
                	String responseXml = response.toString();
                	
                    JAXBContext jaxbContext = JAXBContext.newInstance(ProductSearchResponse.class);
                    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                    ProductSearchResponse responseString = (ProductSearchResponse) unmarshaller.unmarshal(new StringReader(responseXml));

                    // 접근 및 사용 예시
                    Request request = responseString.getRequest();
                    log.get().info("Processing Time: " + request.getProcessingTime());

                    Products products = responseString.getProducts();
                    log.get().info("Total Count: " + products.getTotalCount());

                    List<Product> productList = products.getProductList();
                    
                    if(productList == null) return null;
                    
                    total += productList.size();
                    
                    for (Product product : productList) {
                    	Goods goods = new Goods();
                    	Count.set(0);
    					while(true) {
							Boolean isOk = true;
							
							// API 요청 URL 생성
				            apiUrl = API_URL + "?key=" + API_KEY + "&apiCode=ProductInfo" + "&productCode=" + product.getProductCode();

				            // API 요청을 위한 HttpURLConnection 객체 생성
				            url = new URL(apiUrl);
				            connection = (HttpURLConnection) url.openConnection();
				            connection.setRequestMethod("GET");
				            
//				            log.get().info("apiUrl : {}", apiUrl);

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
				                
				                try {
				                	responseXml = response.toString();
				                	
				                    jaxbContext = JAXBContext.newInstance(ProductInfoResponse.class);
				                    unmarshaller = jaxbContext.createUnmarshaller();
				                    ProductInfoResponse responseString2 = (ProductInfoResponse) unmarshaller.unmarshal(new StringReader(responseXml));
				                    Product product2 = responseString2.getProduct();
				                    
			                        log.get().info("image: " + product.getProductImage300());
			                        log.get().info("detail: " + product.getDetailPageUrl());
			                        goods.setImage(product.getProductImage300());
			                    	goods.setDetail(product.getDetailPageUrl());
			                    	
			                    	log.get().info("title : {}", product.getProductName());
									goods.setName(product.getProductName());
									if(product.getProductName() == null || product.getProductName().equals("")) {
										isOk = false;
									}
									
									//렌탈인지 검증
//									if(!(product2.getInstallment() == null) || !product2.getInstallment().equals("")) {
//										log.get().info("해당 상품은 렌탈상품입니다.");
//										break;									
//									}
										
									log.get().info("price : {}", product.getSalePrice());
									goods.setPrice(product.getSalePrice());
									if((Integer)product.getSalePrice() == null || product.getSalePrice() == 0) {
										isOk = false;
									}
									
									// delivery 1
									Integer deliveryFee = null;
									String delivery = product2.getShipFee();
									log.get().info("delivery Check : {}", product2.getShipFee());
									StringTokenizer st = new StringTokenizer(delivery, " ");
									boolean isExist = false;
									while(st.hasMoreTokens()) {
										String token = st.nextToken();
										if(token.contains("무료") || token.contains("SMS") || token.contains("없음")) {
											deliveryFee = 0;
											isExist = true;
											break;
										}
										if(token.contains("원")) {
											StringTokenizer st_1 = new StringTokenizer(token, "원");
											if(st_1.countTokens() == 1) deliveryFee = Integer.parseInt(token.replaceAll("[^0-9]", ""));
											isExist = true;
											break;
										}
									}
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
									
									if(isOk) {
										goodsList.add(goods);
										insert++;
										break;
									}
									
									Count.set(Count.get() + 1);
		                        	if(Count.get() > 3) {
		                        		throw new Exception("Price select count over 3");
		                        	}


				                } catch (JAXBException e) {
				                    e.printStackTrace();
				                }
				            }
    					}
                    }
                } catch (JAXBException e) {
                    e.printStackTrace();
                }
            } else {
               throw new Exception("API 요청에 실패했습니다. 응답 코드: " + responseCode);
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
    	stepEx.set(stepExecution);
    	account = (String) stepExecution.getJobExecution().getJobParameters().getString("account");
    	log.get().info("target : " + batchSchedule.get().getTarget());
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
        if (executionContext.containsKey("insertSize")) {
        	insertSize.set((int) executionContext.get("insertSize"));
        } else {
        	insertSize.set(0); // 최초 실행 시 insertSize은 0로 초기화
        }
		jobExecution.set(stepExecution.getJobExecution());
		set.add(Thread.currentThread().getName().replaceAll("[^0-9]", ""));
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		// TODO Auto-generated method stub
		ExecutionContext executionContext = stepExecution.getJobExecution().getExecutionContext();
		executionContext.put("target", batchSchedule.get().getTarget());
		executionContext.put("account", account);
        executionContext.put("startPageNum", pageNumber.get());
        executionContext.put("totalSize", totalSize.get());
        executionContext.put("insertSize", insertSize.get());
        set.remove(Thread.currentThread().getName().replaceAll("[^0-9]", ""));
		return ExitStatus.COMPLETED;
	}
}
