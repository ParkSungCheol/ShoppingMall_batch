package com.example.batch.chunk;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
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
import org.springframework.stereotype.Component;
import com.example.batch.Domain.BatchSchedule;
import com.example.batch.Domain.Goods;
import com.example.batch.config.WebDriverManager;

@Component
public class WebCrawlingReader implements ItemReader<List<Goods>>, StepExecutionListener {

	private ThreadLocal<Logger> log = ThreadLocal.withInitial(() -> {
    	return LoggerFactory.getLogger(this.getClass());
    });
    private static final ThreadLocal<Integer> totalSize = new ThreadLocal<>();
    private static final ThreadLocal<BatchSchedule> batchSchedule = new ThreadLocal<>();
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    private static final ThreadLocal<Integer> pageNumber = new ThreadLocal<>();
    private static final ThreadLocal<JobExecution> jobExecution = new ThreadLocal<>();
    private static final ThreadLocal<Integer> driver_num = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> isFirst = new ThreadLocal<>();
    private static final ThreadLocal<StepExecution> stepEx = new ThreadLocal<>();
    private static String account;
    private static WebDriverManager webDriverManager;
    
    public WebCrawlingReader(WebDriverManager webDriverManager) {
		// TODO Auto-generated constructor stub
    	WebCrawlingReader.webDriverManager = webDriverManager;
	}
    
	@Override
	public List<Goods> read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		// TODO Auto-generated method stub
    	try {
	    	if(batchSchedule.get().getUrl() != null && !batchSchedule.get().getUrl().equals("")) {
	    		if(isFirst.get()) {
	    			log.get().info("#### START ####");
	    			isFirst.set(false);
	                // 3. WebDriver 객체 생성
	                driver.set(webDriverManager.getDriver(driver_num.get()));
	                
	    	        // 4. 웹페이지 요청
	                driver.get().get(batchSchedule.get().getUrl() + "&p=" + pageNumber.get());
	    	        
	    	        return crawling(log.get());
	    		}
	    		else {
	    		  WebElement nextButton = findNextButton();
	              if (nextButton == null) {
	                  
	                  log.get().info("totalSize : " + totalSize.get() + ", insertedSize : " + totalSize.get());
	                  
	                  return null;
	              }
	              nextButton.click();
	              try {
	                  Thread.sleep(1000); // 1초 대기
	              } catch (InterruptedException e) {
	                  e.printStackTrace();
	                  return null;
	              }
	              pageNumber.set(pageNumber.get()+1);
	              return crawling(log.get());
	    		}
	    	}
			return null;
    	} catch(TimeoutException e) {
    		stepEx.get().addFailureException(e);
    		stepEx.get().setExitStatus(ExitStatus.FAILED);
    		return null;
    	}
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
    	stepEx.set(stepExecution);
    	account = (String) stepExecution.getJobExecution().getJobParameters().getString("account");
    	driver_num.set(stepExecution.getJobExecution().getJobParameters().getLong("driver_num").intValue());
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
		isFirst.set(true);
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
	
	public static void infiniteScroll(Logger log) {
		JavascriptExecutor js = (JavascriptExecutor) driver.get();
//        JavascriptExecutor js = (JavascriptExecutor) webDriverManager.getDriver(driver_num.get());
        long currentHeight = 0;
        while (true) {
        	long scrollHeight = (long) js.executeScript("return document.body.scrollHeight");
        	
        	while (true) {
        		currentHeight += 100;
    		   js.executeScript("window.scrollTo(0, " + currentHeight + ")");
    		   try {
    		      Thread.sleep(100);
    		   } catch (InterruptedException e) {
    		      e.printStackTrace();
    		      break;
    		   }
    		   if(currentHeight + 100 > scrollHeight) {
    			   js.executeScript("window.scrollTo(0, " + scrollHeight + ")");
        		   currentHeight = scrollHeight;
        		   try {
        		      Thread.sleep(100);
        		      break;
        		   } catch (InterruptedException e) {
        		      e.printStackTrace();
        		      break;
        		   }
    		   }
    		}
        	
            try {
                Thread.sleep(100); // 1초 대기
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
            long newHeight = (long) js.executeScript("return document.body.scrollHeight");
            if (newHeight == currentHeight) {
                break;
            }
        }
    }
	
	private static WebElement findNextButton() {
	      // TODO: 다음 버튼을 찾아서 반환하는 코드 작성
	  	String byFunKey = "CSSSELECTOR";
	  	WebDriverWait wait = new WebDriverWait(driver.get(), Duration.ofSeconds(10));
	//  	WebDriverWait wait = new WebDriverWait(webDriverManager.getDriver(driver_num.get()), Duration.ofSeconds(10));
	  	String selectString = batchSchedule.get().getNextButtonSelector();
	  	try {
		  	WebElement target = wait.until(ExpectedConditions.presenceOfElementLocated( 
		              byFunKey.equals("XPATH") ? By.xpath(selectString) : By.cssSelector(selectString) ));
		  	return target;
	  	} catch (Exception e) {
	  		return null;
	  	}
	}
    
    private static List<Goods> crawling(Logger log) {
    	log.info("Current PageNumber : " + pageNumber.get());
    	// 5. 페이지 로딩을 위한 최대 1초 대기
    	driver.get().manage().timeouts().implicitlyWait(1000, TimeUnit.MILLISECONDS);
//    	webDriverManager.getDriver(driver_num.get()).manage().timeouts().implicitlyWait(100, TimeUnit.MILLISECONDS);
        
        // 6. 조회, 로드될 때까지 최대 5초 대기
    	WebDriverWait wait = new WebDriverWait(driver.get(), Duration.ofSeconds(10));
//        WebDriverWait wait = new WebDriverWait(webDriverManager.getDriver(driver_num.get()), Duration.ofSeconds(10));
        
    	infiniteScroll(log);
    	
        String byFunKey = "CSSSELECTOR";
        String selectString = batchSchedule.get().getTotalSelector();
//            String byFunKey = "XPATH";
//            String selectString = "//*[@id=\"mArticle\"]/div[2]/ul/li[3]/a";
//        WebElement parent = wait.until(ExpectedConditions.presenceOfElementLocated( 
//                byFunKey.equals("XPATH") ? By.xpath(selectString) : By.cssSelector(selectString) ));
//            log.info("#### innerHTML : \n" + parent.getAttribute("innerHTML"));
        
        List<Goods> goodsList = new ArrayList<Goods>();
        
        // 7. 콘텐츠 조회
        List<WebElement> bestContests = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(byFunKey.equals("XPATH") ? By.xpath(selectString) : By.cssSelector(selectString)));
        log.info( "등록된 상품 수 : " + bestContests.size() );
        if (bestContests.size() > 0) {
            for (WebElement best : bestContests) {
            	try {
            		goodsList.add(new Goods());
            		Goods goods = goodsList.get(goodsList.size() - 1);
            		
            		List<WebElement> title = best.findElements(By.cssSelector(batchSchedule.get().getTitleSelector1()));
                    if(title.size() == 0 && batchSchedule.get().getTitleSelector2() != null &&!batchSchedule.get().getTitleSelector2().equals("")) title = best.findElements(By.cssSelector(batchSchedule.get().getTitleSelector2()));
                    if(title.size() == 0 && batchSchedule.get().getTitleSelector3() != null &&!batchSchedule.get().getTitleSelector3().equals("")) title = best.findElements(By.cssSelector(batchSchedule.get().getTitleSelector3()));
//                    String[] titles = title.get(0).getText().split("\n");
//                    String name = removeSpecialCharacters(titles[batchSchedule.get().getTitleLocation()]);
//                    goods.setName(name);
                    String name = removeSpecialCharacters(title.get(batchSchedule.get().getTitleLocation()).getText());
                    goods.setName(name);
                    
                    List<WebElement> price = best.findElements(By.cssSelector(batchSchedule.get().getPriceSelector1()));
                    if(price.size() == 0 && batchSchedule.get().getPriceSelector2() != null &&!batchSchedule.get().getPriceSelector2().equals("")) price = best.findElements(By.cssSelector(batchSchedule.get().getPriceSelector2()));
                    if(price.size() == 0 && batchSchedule.get().getPriceSelector3() != null &&!batchSchedule.get().getPriceSelector3().equals("")) price = best.findElements(By.cssSelector(batchSchedule.get().getPriceSelector3()));
//                    String[] prices = price.get(0).getText().split("\n");
//                    goods.setPrice(Integer.parseInt(prices[batchSchedule.get().getPriceLocation()].replaceAll("[^0-9]", "")));
                    goods.setPrice(Integer.parseInt(price.get(batchSchedule.get().getPriceLocation()).getText().replaceAll("[^0-9]", "")));
                    
                    List<WebElement> deliveryFee = best.findElements(By.cssSelector(batchSchedule.get().getDeliveryFeeSelector1()));
                    String deliveryFeeString = "";
                    if(deliveryFee.size() != 0) {
                    	deliveryFeeString = deliveryFee.get(batchSchedule.get().getDeliveryFeeLocation()).getText();
                    }
                    else if(deliveryFee.size() == 0 && batchSchedule.get().getDeliveryFeeSelector2() != null &&!batchSchedule.get().getDeliveryFeeSelector2().equals("")) {
                    	deliveryFee = best.findElements(By.cssSelector(batchSchedule.get().getDeliveryFeeSelector2()));
                    	deliveryFeeString = deliveryFee.get(batchSchedule.get().getDeliveryFeeLocation()).getAttribute("alt");
                    }
                    deliveryFeeString = deliveryFeeString.replaceAll("[^0-9]", "");
            		if(deliveryFeeString.equals("")) deliveryFeeString = "0";
            		goods.setDeliveryfee(Integer.parseInt(deliveryFeeString));
//                    if(deliveryFee.size() == 0 && batchSchedule.get().getDeliveryFeeSelector3() != null &&!batchSchedule.get().getDeliveryFeeSelector3().equals("")) deliveryFee = best.findElements(By.cssSelector(batchSchedule.get().getDeliveryFeeSelector3()));
//                    String[] deliveryFees = deliveryFee.get(0).getText().split("\n");
//                    if(batchSchedule.get().getBatchName().equals("네이버쇼핑")) {
//                    	if(deliveryFees.length > batchSchedule.get().getDeliveryFeeLocation()) {
//                    		String deliveryFeeString = deliveryFees[batchSchedule.get().getDeliveryFeeLocation()].replaceAll("[^0-9]", "");
//                    		if(deliveryFeeString.equals("")) deliveryFeeString = "0";
//                    		goods.setDeliveryfee(Integer.parseInt(deliveryFeeString));
//                    	}
//                    	else goods.setDeliveryfee(null);
//                    }
//                    else {
//                    	// 타쇼핑몰일 경우..
//                    	
//                    }
                    
                    List<WebElement> seller = best.findElements(By.cssSelector(batchSchedule.get().getSellerSelector1()));
                    String confirmSeller = "";
                    if(seller.size() > 0) {
                    	confirmSeller = seller.get(batchSchedule.get().getSellerLocation()).getText();
                    	confirmSeller = removeSpecialCharacters(confirmSeller);
                    }
                    else {
                    	seller = best.findElements(By.cssSelector(batchSchedule.get().getSellerSelector2()));
                    	if(seller.size() > 0) {
                    		seller = best.findElements(By.cssSelector(batchSchedule.get().getSellerSelector2()));
                        	confirmSeller = seller.get(batchSchedule.get().getSellerLocation()).getAttribute("alt");
                        	confirmSeller = removeSpecialCharacters(confirmSeller);
                    	}
                    	else {
                    		confirmSeller = "지마켓";
                    	}
                    }
                    goods.setSellid(confirmSeller);
//                    List<WebElement> seller = best.findElements(By.cssSelector(batchSchedule.get().getSellerSelector1()));
//                    if(seller.size() == 0 && batchSchedule.get().getSellerSelector2() != null &&!batchSchedule.get().getSellerSelector2().equals("")) seller = best.findElements(By.cssSelector(batchSchedule.get().getSellerSelector2()));
//                    if(seller.size() == 0 && batchSchedule.get().getSellerSelector3() != null &&!batchSchedule.get().getSellerSelector3().equals("")) seller = best.findElements(By.cssSelector(batchSchedule.get().getSellerSelector3()));
//                    if(seller.size() < 1) { throw new MyException("seller is null"); }
//                    String[] sellers = seller.get(0).getText().split("\n");
//                    if(batchSchedule.get().getBatchName().equals("네이버쇼핑")) {
//                    	String confirmSeller = sellers[batchSchedule.get().getSellerLocation()] == null || sellers[batchSchedule.get().getSellerLocation()].equals("") || sellers[batchSchedule.get().getSellerLocation()].equals("쇼핑몰별 최저가")? batchSchedule.get().getBatchName() : sellers[batchSchedule.get().getSellerLocation()];
//                    	confirmSeller = removeSpecialCharacters(confirmSeller);
//                        goods.setSellid(confirmSeller);
//                    }
//                    else {
//                    	// 타쇼핑몰일 경우..
//                    }
                    
                    List<WebElement> urls = best.findElements(By.cssSelector(batchSchedule.get().getUrlSelector1()));
                    if(urls.size() == 0 && batchSchedule.get().getUrlSelector2() != null &&!batchSchedule.get().getUrlSelector2().equals("")) urls = best.findElements(By.cssSelector(batchSchedule.get().getUrlSelector2()));
                    if(urls.size() == 0 && batchSchedule.get().getUrlSelector3() != null &&!batchSchedule.get().getUrlSelector3().equals("")) urls = best.findElements(By.cssSelector(batchSchedule.get().getUrlSelector3()));
//                    if(urls.size() < 1) { throw new MyException("urls is null"); }
//                    String url = urls.get(0).getAttribute("href");
//                    goods.setDetail(url);
                    goods.setDetail(urls.get(0).getAttribute("href"));
                    
                    // elements 요소들이 나타날 때까지 대기
                    wait.until(webDriver -> {
                    	List<WebElement> images = best.findElements(By.cssSelector(batchSchedule.get().getImageSelector()));
                        JavascriptExecutor jsExecutor = (JavascriptExecutor) webDriver;
                        String src = (String) jsExecutor.executeScript("return arguments[0].src;", images.get(0));
                        return !src.contains("lazyload");
                    });
                    List<WebElement> images = best.findElements(By.cssSelector(batchSchedule.get().getImageSelector()));
//                	String image = images.get(0).getAttribute("src");
//                	goods.setImage(image);
                    goods.setImage(images.get(0).getAttribute("src"));
                    
            	}
//            	catch(NoSuchElementException e) {
//            		goodsList.remove(goodsList.size() - 1);
//            		log.info("NoSuchElementException is expired");
//            		skippedCount++;
//            		totalSkippedSize.set(totalSkippedSize.get() + 1);
//            		throw e;
//            	}
//            	catch(MyException e) {
//            		goodsList.remove(goodsList.size() - 1);
//            		log.info(e.getMessage());
//            		skippedCount++;
//            		totalSkippedSize.set(totalSkippedSize.get() + 1);
//            		continue;
//            	}
            	catch(Exception e) {
            		e.printStackTrace();
            		throw e;
            	}
            }
        }
        
        log.info("target : " + bestContests.size() + ", inserted : " + bestContests.size());
        totalSize.set(totalSize.get() + bestContests.size());
        log.info("#### crawling END ####");
        return goodsList;
    }
    
    public static String removeSpecialCharacters(String text) {
        // 정규 표현식을 사용하여 특수 문자 제거
        String pattern = "[^a-zA-Z0-9가-힣\\s]";
        text = text.replaceAll(pattern, "");
        return text;
    }

}
