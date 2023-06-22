package com.example.batch.chunk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
import com.example.batch.Domain.Product;
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
        int display = 200;
        List<Goods> goodsList = new ArrayList<Goods>();
        int total = 0;
        if(pageNumber.get() > 10) return null;
        
        try {
        	log.get().info("Current PageNumber : " + pageNumber.get());
            // 쿼리를 UTF-8로 인코딩
            String encodedQuery = URLEncoder.encode(query, "UTF-8");

            // API 요청 URL 생성
            String apiUrl = API_URL + "?key=" + API_KEY + "&apiCode=ProductSearch" + "&keyword=" + encodedQuery + "&pageNum=" + pageNumber.get() + "&pageSize=" + display;

            // API 요청을 위한 HttpURLConnection 객체 생성
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

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
                    Document doc;
                    
                    for (Product product : productList) {
                    	Goods goods = new Goods();
                        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    					Elements elems;
    					Count.set(0);
    					
    					while(true) {
    						synchronized (this) {
    							Boolean isOk = true;
    							
    							Thread.currentThread().sleep(500);
    							
    							goods.setImage(product.getProductImage300());
    	                    	goods.setDetail(product.getDetailPageUrl());
//    	                        log.get().info("image: " + product.getProductImage300());
//    	                        log.get().info("detail: " + product.getDetailPageUrl());
    							
    							doc = Jsoup.connect(goods.getDetail()).header("User-Agent", userAgent).get();
    							elems = doc.select("#layBodyWrap h1.title");
    							
    							if(elems.size() == 1) {
    								String title = elems.get(0).text();
    								if(!title.equals("")) {
//    									log.get().info("title : {}", title);
    									goods.setName(title);
    								}
    								else isOk = false;
    							}
    							else isOk = false;
    							
    							elems = doc.select(".price_wrap span.value");
    							
    							if(elems.size() >= 1) {
    								String price = elems.get(0).text().replaceAll("[^0-9]", "");
    								if(!price.equals("")) {
//    									log.get().info("price : {}", price);
    									goods.setPrice(Integer.parseInt(price));
    								}
    								else isOk = false;
    							}
    							else isOk = false;
    							
    							elems = doc.select("div.delivery > dt");
    							
    							if(elems.size() == 1) {
    								Integer deliveryFee = null;
    								String delivery = elems.get(0).text();
    								StringTokenizer st = new StringTokenizer(delivery, " ");
    								while(st.hasMoreTokens()) {
    									String token = st.nextToken();
    									if(token.equals("무료배송")) {
    										deliveryFee = 0;
    										break;
    									}
    									if(token.contains("원")) {
    										deliveryFee = Integer.parseInt(token.replaceAll("[^0-9]", ""));
    										break;
    									}
    								}
    								if(deliveryFee != null) {
//    									log.get().info("deliveryFee : {}", deliveryFee);
    									goods.setDeliveryfee(deliveryFee);
    								}
    								else isOk = false;
    							}
    							else isOk = false;
    							
    							elems = doc.select("#productSellerWrap h4 > a");
    							
    							if(elems.size() == 1) {
    								String seller = elems.get(0).text();
    								if(!seller.equals("")) {
    									goods.setSellid(seller);
//    									log.get().info("seller : {}", seller);
    								}
    								else isOk = false;
    							}
    							else isOk = false;
    							
    							if(isOk) {
    								goodsList.add(goods);
    								total++;
    								break;
    							}
    						}
    						
    						Count.set(Count.get() + 1);
                        	if(Count.get() > 10) {
                        		if(doc.select("#layBodyWrap").size() == 0 || doc.select(".price_wrap").size() == 0) {
                        			log.get().info("this is not exist dom : {}", goods.getDetail());
                        			break;
                        		}
                        		else {
                        			throw new Exception("Price select count over 10");
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
            
            log.get().info("target : " + total + ", inserted : " + total);
            totalSize.set(totalSize.get() + total);
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
		ExecutionContext executionContext = stepExecution.getJobExecution().getExecutionContext();
		executionContext.put("url", batchSchedule.get().getUrl());
		executionContext.put("account", account);
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
        String pattern = "[^.a-zA-Z0-9가-힣\\s]";
        text = text.replaceAll(pattern, "");
        return text;
    }
    
    public String makeSpecialCharactersTokenizer(String input, String delimeter) {
    	String regex = "[^\\p{L}\\p{Z}\\p{N}.]+";
    	String numberRegex = "^\\d+$";
    	
    	StringTokenizer st = new StringTokenizer(input, " ");
    	
    	String processedString = "" + delimeter;
    	while(st.hasMoreTokens()) {
    		String target = st.nextToken();
    		while(true) {
	    		// 특정 토큰의 마지막 글자가 숫자인 경우
	            if (Character.isDigit(target.charAt(target.length() - 1))) {
	                if (st.hasMoreTokens()) {
	                    String nextToken = st.nextToken();
	                    if(Character.isDigit(nextToken.charAt(nextToken.length() - 1))) continue;
	                    else target += " "; // 다음 토큰과 붙여서 문자열 생성
	                }
	                else break;
	            }
	            else break;
    		}
	        Pattern pattern = Pattern.compile(regex);
	        Matcher matcher = pattern.matcher(target);
	
	        if (matcher.find()) {
	        	processedString = processedString.trim() + delimeter + " " + delimeter;
	        	continue;
	        }
	        String token = target.trim();
	        if (!token.isEmpty() && target.length() > 1) {
	            processedString += token + " ";
	        }
    	}

        processedString = processedString.replaceAll("\\s+", " ").trim();
        int lastSpaceIndex = processedString.lastIndexOf(" ");
        
        if (lastSpaceIndex != -1) {
            // 띄어쓰기가 포함되어 있지 않은 경우 원본 문자열을 그대로 반환
        	String pre = processedString.substring(0, lastSpaceIndex + 1);
        	String last = processedString.substring(lastSpaceIndex + 1);
        	Pattern pattern = Pattern.compile(numberRegex);
	        Matcher matcher = pattern.matcher(last);
	        
	        if(matcher.find()) {
	        	last = "";
	        }
	        processedString = pre + last;
        }
        
        processedString += delimeter; // 중복 공백 제거

        return processedString;
    }
}
