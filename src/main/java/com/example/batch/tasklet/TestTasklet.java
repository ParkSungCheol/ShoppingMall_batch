package com.example.batch.tasklet;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.example.batch.Domain.BatchSchedule;

public class TestTasklet implements Tasklet {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private static int totalSize;
    private static int totalSkippedSize;
    private static BatchSchedule batchSchedule = null;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
    	batchSchedule = new BatchSchedule();
    	batchSchedule.setBatchName((String) chunkContext.getStepContext().getJobParameters().get("batchName"));
    	batchSchedule.setUrl((String) chunkContext.getStepContext().getJobParameters().get("url"));
    	batchSchedule.setTotalSelector((String) chunkContext.getStepContext().getJobParameters().get("totalSelector"));
    	batchSchedule.setTitleSelector1((String) chunkContext.getStepContext().getJobParameters().get("titleSelector1"));
    	batchSchedule.setTitleSelector2((String) chunkContext.getStepContext().getJobParameters().get("titleSelector2"));
    	batchSchedule.setTitleSelector3((String) chunkContext.getStepContext().getJobParameters().get("titleSelector3"));
    	batchSchedule.setTitleLocation(chunkContext.getStepContext().getJobParameters().get("titleLocation") != null? Integer.parseInt((String) chunkContext.getStepContext().getJobParameters().get("titleLocation")) : 0);
    	batchSchedule.setPriceSelector1((String) chunkContext.getStepContext().getJobParameters().get("priceSelector1"));
    	batchSchedule.setPriceSelector2((String) chunkContext.getStepContext().getJobParameters().get("priceSelector2"));
    	batchSchedule.setPriceSelector3((String) chunkContext.getStepContext().getJobParameters().get("priceSelector3"));
    	batchSchedule.setPriceLocation(chunkContext.getStepContext().getJobParameters().get("priceLocation") != null? Integer.parseInt((String) chunkContext.getStepContext().getJobParameters().get("priceLocation")) : 0);
    	batchSchedule.setDeliveryFeeSelector1((String) chunkContext.getStepContext().getJobParameters().get("deliveryFeeSelector1"));
    	batchSchedule.setDeliveryFeeSelector2((String) chunkContext.getStepContext().getJobParameters().get("deliveryFeeSelector2"));
    	batchSchedule.setDeliveryFeeSelector3((String) chunkContext.getStepContext().getJobParameters().get("deliveryFeeSelector3"));
    	batchSchedule.setDeliveryFeeLocation(chunkContext.getStepContext().getJobParameters().get("deliveryFeeLocation") != null? Integer.parseInt((String) chunkContext.getStepContext().getJobParameters().get("deliveryFeeLocation")) : 0);
    	batchSchedule.setSellerSelector1((String) chunkContext.getStepContext().getJobParameters().get("sellerSelector1"));
    	batchSchedule.setSellerSelector2((String) chunkContext.getStepContext().getJobParameters().get("sellerSelector2"));
    	batchSchedule.setSellerSelector3((String) chunkContext.getStepContext().getJobParameters().get("sellerSelector3"));
    	batchSchedule.setSellerLocation(chunkContext.getStepContext().getJobParameters().get("sellerLocation") != null? Integer.parseInt((String) chunkContext.getStepContext().getJobParameters().get("sellerLocation")) : 0);
    	batchSchedule.setUrlSelector1((String) chunkContext.getStepContext().getJobParameters().get("urlSelector1"));
    	batchSchedule.setUrlSelector2((String) chunkContext.getStepContext().getJobParameters().get("urlSelector2"));
    	batchSchedule.setUrlSelector3((String) chunkContext.getStepContext().getJobParameters().get("urlSelector3"));
    	batchSchedule.setNextButtonSelector((String) chunkContext.getStepContext().getJobParameters().get("nextButtonSelector"));
    	
    	log.info("url : " + batchSchedule.getUrl());
    	if(batchSchedule.getUrl() != null && !batchSchedule.getUrl().equals("")) {
    		totalSize = 0;
    		totalSkippedSize = 0;
    		runSelenium(log);
    	}
        return RepeatStatus.FINISHED;
    }
    
    public static void infiniteScroll(WebDriver driver) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        long currentHeight = (long) js.executeScript("return document.body.scrollHeight");
        while (true) {
            js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
            try {
                Thread.sleep(1000); // 1초 대기
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long newHeight = (long) js.executeScript("return document.body.scrollHeight");
            if (newHeight == currentHeight) {
                break;
            }
            currentHeight = newHeight;
        }
    }
    
    private static void crawling(WebDriver driver, Logger log, int pageNumber) {
    	log.info("Current PageNumber : " + pageNumber);
    	// 5. 페이지 로딩을 위한 최대 1초 대기
        driver.manage().timeouts().implicitlyWait(100, TimeUnit.MILLISECONDS);
        
        // 6. 조회, 로드될 때까지 최대 5초 대기
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
        
        infiniteScroll(driver);
        
        String byFunKey = "CSSSELECTOR";
        String selectString = batchSchedule.getTotalSelector();
//            String byFunKey = "XPATH";
//            String selectString = "//*[@id=\"mArticle\"]/div[2]/ul/li[3]/a";
        WebElement parent = wait.until(ExpectedConditions.presenceOfElementLocated( 
                byFunKey.equals("XPATH") ? By.xpath(selectString) : By.cssSelector(selectString) ));
//            log.info("#### innerHTML : \n" + parent.getAttribute("innerHTML"));
        
        // 7. 콘텐츠 조회
        List<WebElement> bestContests = parent.findElements(By.xpath("*"));
        log.info( "등록된 상품 수 : " + bestContests.size() );
        totalSize += bestContests.size();
        int skippedCount = 0;
        if (bestContests.size() > 0) {
            for (WebElement best : bestContests) {
            	try {
            		List<WebElement> title = best.findElements(By.cssSelector(batchSchedule.getTitleSelector1()));
                    if(title.size() == 0 && batchSchedule.getTitleSelector2() != null &&!batchSchedule.getTitleSelector2().equals("")) title = best.findElements(By.cssSelector(batchSchedule.getTitleSelector2()));
                    if(title.size() == 0 && batchSchedule.getTitleSelector3() != null &&!batchSchedule.getTitleSelector3().equals("")) title = best.findElements(By.cssSelector(batchSchedule.getTitleSelector3()));
                    String[] titles = title.get(0).getText().split("\n");
                    log.info("title : " + titles[batchSchedule.getTitleLocation()]);
                    
                    List<WebElement> price = best.findElements(By.cssSelector(batchSchedule.getPriceSelector1()));
                    if(price.size() == 0 && batchSchedule.getPriceSelector2() != null &&!batchSchedule.getPriceSelector2().equals("")) price = best.findElements(By.cssSelector(batchSchedule.getPriceSelector2()));
                    if(price.size() == 0 && batchSchedule.getPriceSelector3() != null &&!batchSchedule.getPriceSelector3().equals("")) price = best.findElements(By.cssSelector(batchSchedule.getPriceSelector3()));
                    String[] prices = price.get(0).getText().split("\n");
                    log.info("price : " + prices[batchSchedule.getPriceLocation()]);
                    
                    List<WebElement> deliveryFee = best.findElements(By.cssSelector(batchSchedule.getDeliveryFeeSelector1()));
                    if(deliveryFee.size() == 0 && batchSchedule.getDeliveryFeeSelector2() != null &&!batchSchedule.getDeliveryFeeSelector2().equals("")) deliveryFee = best.findElements(By.cssSelector(batchSchedule.getDeliveryFeeSelector2()));
                    if(deliveryFee.size() == 0 && batchSchedule.getDeliveryFeeSelector3() != null &&!batchSchedule.getDeliveryFeeSelector3().equals("")) deliveryFee = best.findElements(By.cssSelector(batchSchedule.getDeliveryFeeSelector3()));
                    String[] deliveryFees = deliveryFee.get(0).getText().split("\n");
                    if(batchSchedule.getBatchName().equals("네이버쇼핑")) {
                    	if(deliveryFees.length > batchSchedule.getDeliveryFeeLocation()) log.info("deliveryFee : " + deliveryFees[batchSchedule.getDeliveryFeeLocation()]);
                    	else log.info("deliveryFee : 별도확인필요");
                    }
                    else {
                    	// 타쇼핑몰일 경우..
                    }
                    
                    
                    List<WebElement> seller = best.findElements(By.cssSelector(batchSchedule.getSellerSelector1()));
                    if(seller.size() == 0 && batchSchedule.getSellerSelector2() != null &&!batchSchedule.getSellerSelector2().equals("")) seller = best.findElements(By.cssSelector(batchSchedule.getSellerSelector2()));
                    if(seller.size() == 0 && batchSchedule.getSellerSelector3() != null &&!batchSchedule.getSellerSelector3().equals("")) seller = best.findElements(By.cssSelector(batchSchedule.getSellerSelector3()));
                    String[] sellers = seller.get(0).getText().split("\n");
                    if(batchSchedule.getBatchName().equals("네이버쇼핑")) {
                    	String confirmSeller = sellers[batchSchedule.getSellerLocation()] == null || sellers[batchSchedule.getSellerLocation()].equals("")? batchSchedule.getBatchName() : sellers[batchSchedule.getSellerLocation()];
                        log.info("seller : " + (confirmSeller.equals("쇼핑몰별 최저가")? batchSchedule.getBatchName() : confirmSeller));
                    }
                    else {
                    	// 타쇼핑몰일 경우..
                    }
                    
                    List<WebElement> urls = best.findElements(By.cssSelector(batchSchedule.getUrlSelector1()));
                    if(urls.size() == 0 && batchSchedule.getUrlSelector2() != null &&!batchSchedule.getUrlSelector2().equals("")) urls = best.findElements(By.cssSelector(batchSchedule.getUrlSelector2()));
                    if(urls.size() == 0 && batchSchedule.getUrlSelector3() != null &&!batchSchedule.getUrlSelector3().equals("")) urls = best.findElements(By.cssSelector(batchSchedule.getUrlSelector3()));
                    String url = urls.get(0).getAttribute("href");
                    log.info("url : " + url);
            	}
            	catch(NoSuchElementException e) {
            		log.info("NoSuchElementException is expired");
            		skippedCount++;
            		totalSkippedSize++;
            		continue;
            	}
            	catch(Exception e) {
            		log.info("OtherException is expired");
            		skippedCount++;
            		totalSkippedSize++;
            		continue;
            	}
            }
        }
        log.info("target : " + bestContests.size() + ", inserted : " + (bestContests.size() - skippedCount) + ", error : " + skippedCount);
        
        log.info("#### crawling END ####");
    }
    
    public static void navigateToLastPage(WebDriver driver, Logger log) {
    	int pageNumber = 1;
    	crawling(driver, log, pageNumber);
        while (true) {
            // 페이지에서 다음 버튼을 찾아 클릭합니다.
            WebElement nextButton = findNextButton(driver);
            if (nextButton == null) {
            	// 8. WebDriver 종료
                driver.quit();
                
                log.info("totalSize : " + totalSize + ", insertedSize : " + (totalSize - totalSkippedSize) +", totalSkippedSize : " + totalSkippedSize);
                log.info("#### driver END ####");
                
                break;
            }
            nextButton.click();
            pageNumber++;
            crawling(driver, log, pageNumber);
            try {
                Thread.sleep(1000); // 1초 대기
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static WebElement findNextButton(WebDriver driver) {
        // TODO: 다음 버튼을 찾아서 반환하는 코드 작성
    	String byFunKey = "CSSSELECTOR";
    	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(1));
    	String selectString = batchSchedule.getNextButtonSelector();
    	WebElement target = wait.until(ExpectedConditions.presenceOfElementLocated( 
                byFunKey.equals("XPATH") ? By.xpath(selectString) : By.cssSelector(selectString) ));
    	if(target.getText().equals("다음")) return target;
    	else return null;
    }
    
    private static void runSelenium(Logger log) throws Exception {
    	log.info("#### START ####");
        
        // 1. WebDriver 경로 설정
        Path path = Paths.get("C:\\ShoppingMall/batch/driver/geckodriver.exe");
        System.setProperty("webdriver.gecko.driver", path.toString());
        
        // 2. WebDriver 옵션 설정
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--start-maximized");          // 최대크기로
        options.addArguments("--headless");                 // Browser를 띄우지 않음
        options.addArguments("--disable-gpu");              // GPU를 사용하지 않음, Linux에서 headless를 사용하는 경우 필요함.
        options.addArguments("--no-sandbox");               // Sandbox 프로세스를 사용하지 않음, Linux에서 headless를 사용하는 경우 필요함.
        options.addArguments("--disable-popup-blocking");    // 팝업 무시
        options.addArguments("--blink-settings=imagesEnabled=false"); //이미지 다운 안받음
        options.addArguments("--disable-default-apps");     // 기본앱 사용안함
        
        // 3. WebDriver 객체 생성
        WebDriver driver = new FirefoxDriver( options );
            
        // 4. 웹페이지 요청
        driver.get(batchSchedule.getUrl());
        
        navigateToLastPage(driver, log);
    }
}