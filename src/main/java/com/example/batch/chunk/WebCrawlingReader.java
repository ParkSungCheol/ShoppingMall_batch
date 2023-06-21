package com.example.batch.chunk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
    private static final ThreadLocal<Integer> Count = new ThreadLocal<>();
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
                	Boolean isNotExist = false;
                	String title = item.getTitle().replaceAll("<b>", "").replaceAll("</b>", "").replaceAll("&amp;", "&");
                	Goods goods = new Goods();
                	goods.setName(removeSpecialCharacters(title));
                	goods.setDetail(item.getLink());
					goods.setImage(item.getImage());
					String mallNameTest = item.getMallName().replaceAll("\\s+", " ");
					item.setMallName(removeSpecialCharacters(item.getMallName().replaceAll("\\s+", " ")));
					
					Integer price = null;
					Integer apiPrice = Integer.parseInt(item.getLprice());
					Integer crawlPrice = null;
					String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
					Elements elems;
					Count.set(0);
					
					while(true) {
						synchronized (this) {
							Thread.currentThread().sleep(250);
							
							doc = Jsoup.connect(item.getLink()).header("User-Agent", userAgent).get();
							elems = doc.select("#wrap > div > p");
							
							if(elems.size() == 1) {
								String crawlPriceString = elems.get(0).text().replaceAll("[^0-9]", "");
								if(crawlPriceString.equals("")) {
									isNotExist = true;
									break;
								}
								else {
									crawlPrice = Integer.parseInt(crawlPriceString);
									if(crawlPrice != null) break;
								}
							}
						}
						
						Count.set(Count.get() + 1);
                    	if(Count.get() > 10) {
                    		log.get().info("url : {}", item.getLink());
                    		throw new Exception("Price select count over 10");
                    	}
					}
					
					if(isNotExist) {
						total--;
						continue;
					}
					
                    Count.set(0);
                    String deliveryUrl = "";
                    while(true) {
                    	String titleUrl1 = "";
                    	String titleUrl2 = "";
                    	
                    	synchronized (this) {
                        	Thread.currentThread().sleep(250);
                        	
                        	if(!item.getMallName().equals("") && !item.getMallName().equals("네이버")) {
                        		String[] tokens = mallNameTest.split("[\\s+\\-|]");
                        		titleUrl1 += " +" + tokens[0];
//                        		if(item.getMallName().contains(" ")) titleUrl1 += " " + item.getMallName().replaceAll(" ", "");
                                titleUrl2 += " \"" + tokens[0] + "\"";
//                                if(item.getMallName().contains(" ")) titleUrl2 += " \"" + item.getMallName().replaceAll(" ", "") + "\"";
                             }
                        	deliveryUrl = "https://search.shopping.naver.com/search/all?maxPrice="
                                    + apiPrice
                                    + "&minPrice="
                                    + apiPrice
                                    + "&query="
                                    + URLEncoder.encode(makeSpecialCharactersTokenizer(title, "") 
                                    + titleUrl1 
                                    //+ " " + makeSpecialCharactersTokenizer(title, "\"") 
                                    + titleUrl2
                                    , "UTF-8");
                        	
                		    doc = Jsoup.connect(deliveryUrl).header("User-Agent", userAgent).get();
                		    
                		    elems = doc.select(batchSchedule.get().getTotalSelector());
                        	if(elems.size() > 0) {
                        		price = apiPrice;
                        		break;
                        	}
                        	
//                		    Thread.currentThread().sleep(500);
//                        	
//                        	if(!item.getMallName().equals("") && !item.getMallName().equals("네이버")) {
//                                titleUrl = " \"" + item.getMallName().replaceAll(" ", "") + "\"";
//                             }
//                        	deliveryUrl = "https://search.shopping.naver.com/search/all?maxPrice="
//                                    + apiPrice
//                                    + "&minPrice="
//                                    + apiPrice
//                                    + "&query="
//                                    + URLEncoder.encode("\"" + title.replace("&", "&amp;") + "\"" + titleUrl.replace("&", "&amp;"), "UTF-8");
//                        	
//                		    doc = Jsoup.connect(deliveryUrl).header("User-Agent", userAgent).get();
//                		    
//                		    elems = doc.select(batchSchedule.get().getTotalSelector());
//                		    if(elems.size() > 0) {
//                        		price = apiPrice;
//                        		break;
//                        	}
                		    if(apiPrice != crawlPrice) {
                		    	Thread.currentThread().sleep(250);
                		    	
//                		    	if(!item.getMallName().equals("") && !item.getMallName().equals("네이버")) {
//                                    titleUrl = " \"" + item.getMallName() + "\"";
//                                 }
                            	deliveryUrl = "https://search.shopping.naver.com/search/all?maxPrice="
                                        + crawlPrice
                                        + "&minPrice="
                                        + crawlPrice
                                        + "&query="
                                        + URLEncoder.encode(makeSpecialCharactersTokenizer(title, "") 
                                        + titleUrl1 
                                        //+ " " + makeSpecialCharactersTokenizer(title, "\"") 
                                        + titleUrl2
                                        , "UTF-8");
                            	
                    		    doc = Jsoup.connect(deliveryUrl).header("User-Agent", userAgent).get();
                    		    
                    		    elems = doc.select(batchSchedule.get().getTotalSelector());
                    		    if(elems.size() > 0) {
                            		price = crawlPrice;
                            		break;
                            	}
                            	
//                            	Thread.currentThread().sleep(500);
//                            	
//                            	if(!item.getMallName().equals("") && !item.getMallName().equals("네이버")) {
//                                    titleUrl = " \"" + item.getMallName().replaceAll(" ", "") + "\"";
//                                 }
//                            	deliveryUrl = "https://search.shopping.naver.com/search/all?maxPrice="
//                                        + crawlPrice
//                                        + "&minPrice="
//                                        + crawlPrice
//                                        + "&query="
//                                        + URLEncoder.encode("\"" + title.replace("&", "&amp;") + "\"" + titleUrl.replace("&", "&amp;"), "UTF-8");
//                            	
//                    		    doc = Jsoup.connect(deliveryUrl).header("User-Agent", userAgent).get();
//                    		    
//                    		    elems = doc.select(batchSchedule.get().getTotalSelector());
//                    		    if(elems.size() > 0) {
//                            		price = crawlPrice;
//                            		break;
//                            	}
                		    }
                		}
                    	
                    	Count.set(Count.get() + 1);
                    	if(Count.get() > 10) {
                    		log.get().info("title : {}", title);
                        	log.get().info("preUrl : {}", makeSpecialCharactersTokenizer(title, ""));
                        	log.get().info("afterUrl : {}", makeSpecialCharactersTokenizer(title, "\""));
                    		log.get().info("url : {}", item.getLink());
                    		log.get().info("deliveryUrl : {}", deliveryUrl);
                    		throw new Exception("deliveryFee select count over 10");
                    	}
                    }
//                    log.get().info("title : {}", title);
//                	log.get().info("preUrl : {}", makeSpecialCharactersTokenizer(title, ""));
//                	log.get().info("afterUrl : {}", makeSpecialCharactersTokenizer(title, "\""));
	                Integer deliveryFee = null;
	                for(Element elem : elems) {
	                	String mallName = elem.select(batchSchedule.get().getSellerSelector1()).get(0).text();
	                   if(mallName.equals("쇼핑몰별 최저가")) mallName = "네이버";
	                   if(mallName.equals("")) mallName = elem.select(batchSchedule.get().getSellerSelector2()).get(0).attr("alt");
	                   mallName = removeSpecialCharacters(mallName);
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
	                
                goods.setSellid(item.getMallName());
                goods.setDeliveryfee(deliveryFee);
                goods.setPrice(price);
                if((!goods.getSellid().equals("네이버") && deliveryFee == null) || (goods.getSellid().equals("네이버") && deliveryFee != null)) {
                	log.get().info("title : {}", title);
                	log.get().info("preUrl : {}", makeSpecialCharactersTokenizer(title, ""));
                	log.get().info("afterUrl : {}", makeSpecialCharactersTokenizer(title, "\""));
                	log.get().info("url : {}", item.getLink());
            		log.get().info("deliveryUrl : {}", deliveryUrl);
                	log.get().info(goods.toString());
                	throw new Exception("this is not normal");
                }
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
