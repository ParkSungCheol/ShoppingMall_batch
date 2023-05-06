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

    private ThreadLocal<Logger> log = ThreadLocal.withInitial(() -> {
    	return LoggerFactory.getLogger(this.getClass());
    });
    private static final ThreadLocal<Integer> totalSize = new ThreadLocal<>();
    private static final ThreadLocal<Integer> totalSkippedSize = new ThreadLocal<>();
    private static final ThreadLocal<BatchSchedule> batchSchedule = new ThreadLocal<>();
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
    	batchSchedule.set(new BatchSchedule());
    	batchSchedule.get().setBatchName((String) chunkContext.getStepContext().getJobParameters().get("batchName"));
    	batchSchedule.get().setUrl((String) chunkContext.getStepContext().getJobParameters().get("url"));
    	batchSchedule.get().setTotalSelector((String) chunkContext.getStepContext().getJobParameters().get("totalSelector"));
    	batchSchedule.get().setTitleSelector1((String) chunkContext.getStepContext().getJobParameters().get("titleSelector1"));
    	batchSchedule.get().setTitleSelector2((String) chunkContext.getStepContext().getJobParameters().get("titleSelector2"));
    	batchSchedule.get().setTitleSelector3((String) chunkContext.getStepContext().getJobParameters().get("titleSelector3"));
    	batchSchedule.get().setTitleLocation(chunkContext.getStepContext().getJobParameters().get("titleLocation") != null? Integer.parseInt((String) chunkContext.getStepContext().getJobParameters().get("titleLocation")) : 0);
    	batchSchedule.get().setPriceSelector1((String) chunkContext.getStepContext().getJobParameters().get("priceSelector1"));
    	batchSchedule.get().setPriceSelector2((String) chunkContext.getStepContext().getJobParameters().get("priceSelector2"));
    	batchSchedule.get().setPriceSelector3((String) chunkContext.getStepContext().getJobParameters().get("priceSelector3"));
    	batchSchedule.get().setPriceLocation(chunkContext.getStepContext().getJobParameters().get("priceLocation") != null? Integer.parseInt((String) chunkContext.getStepContext().getJobParameters().get("priceLocation")) : 0);
    	batchSchedule.get().setDeliveryFeeSelector1((String) chunkContext.getStepContext().getJobParameters().get("deliveryFeeSelector1"));
    	batchSchedule.get().setDeliveryFeeSelector2((String) chunkContext.getStepContext().getJobParameters().get("deliveryFeeSelector2"));
    	batchSchedule.get().setDeliveryFeeSelector3((String) chunkContext.getStepContext().getJobParameters().get("deliveryFeeSelector3"));
    	batchSchedule.get().setDeliveryFeeLocation(chunkContext.getStepContext().getJobParameters().get("deliveryFeeLocation") != null? Integer.parseInt((String) chunkContext.getStepContext().getJobParameters().get("deliveryFeeLocation")) : 0);
    	batchSchedule.get().setSellerSelector1((String) chunkContext.getStepContext().getJobParameters().get("sellerSelector1"));
    	batchSchedule.get().setSellerSelector2((String) chunkContext.getStepContext().getJobParameters().get("sellerSelector2"));
    	batchSchedule.get().setSellerSelector3((String) chunkContext.getStepContext().getJobParameters().get("sellerSelector3"));
    	batchSchedule.get().setSellerLocation(chunkContext.getStepContext().getJobParameters().get("sellerLocation") != null? Integer.parseInt((String) chunkContext.getStepContext().getJobParameters().get("sellerLocation")) : 0);
    	batchSchedule.get().setUrlSelector1((String) chunkContext.getStepContext().getJobParameters().get("urlSelector1"));
    	batchSchedule.get().setUrlSelector2((String) chunkContext.getStepContext().getJobParameters().get("urlSelector2"));
    	batchSchedule.get().setUrlSelector3((String) chunkContext.getStepContext().getJobParameters().get("urlSelector3"));
    	batchSchedule.get().setNextButtonSelector((String) chunkContext.getStepContext().getJobParameters().get("nextButtonSelector"));
    	
    	log.get().info("url : " + batchSchedule.get().getUrl());
    	if(batchSchedule.get().getUrl() != null && !batchSchedule.get().getUrl().equals("")) {
    		totalSize.set(0);
    		totalSkippedSize.set(0);
    		runSelenium(log.get());
    	}
        return RepeatStatus.FINISHED;
    }
    
    public static void infiniteScroll() {
        JavascriptExecutor js = (JavascriptExecutor) driver.get();
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
    
    private static void crawling(Logger log, int pageNumber) {
    	log.info("Current PageNumber : " + pageNumber);
    	// 5. 페이지 로딩을 위한 최대 1초 대기
        driver.get().manage().timeouts().implicitlyWait(100, TimeUnit.MILLISECONDS);
        
        // 6. 조회, 로드될 때까지 최대 5초 대기
        WebDriverWait wait = new WebDriverWait(driver.get(), Duration.ofSeconds(1));
        
        infiniteScroll();
        
        String byFunKey = "CSSSELECTOR";
        String selectString = batchSchedule.get().getTotalSelector();
//            String byFunKey = "XPATH";
//            String selectString = "//*[@id=\"mArticle\"]/div[2]/ul/li[3]/a";
        WebElement parent = wait.until(ExpectedConditions.presenceOfElementLocated( 
                byFunKey.equals("XPATH") ? By.xpath(selectString) : By.cssSelector(selectString) ));
//            log.info("#### innerHTML : \n" + parent.getAttribute("innerHTML"));
        
        // 7. 콘텐츠 조회
        List<WebElement> bestContests = parent.findElements(By.xpath("*"));
        log.info( "등록된 상품 수 : " + bestContests.size() );
        totalSize.set(totalSize.get() + bestContests.size());
        int skippedCount = 0;
        if (bestContests.size() > 0) {
            for (WebElement best : bestContests) {
            	try {
            		List<WebElement> title = best.findElements(By.cssSelector(batchSchedule.get().getTitleSelector1()));
                    if(title.size() == 0 && batchSchedule.get().getTitleSelector2() != null &&!batchSchedule.get().getTitleSelector2().equals("")) title = best.findElements(By.cssSelector(batchSchedule.get().getTitleSelector2()));
                    if(title.size() == 0 && batchSchedule.get().getTitleSelector3() != null &&!batchSchedule.get().getTitleSelector3().equals("")) title = best.findElements(By.cssSelector(batchSchedule.get().getTitleSelector3()));
                    String[] titles = title.get(0).getText().split("\n");
                    log.info("title : " + titles[batchSchedule.get().getTitleLocation()]);
                    
                    List<WebElement> price = best.findElements(By.cssSelector(batchSchedule.get().getPriceSelector1()));
                    if(price.size() == 0 && batchSchedule.get().getPriceSelector2() != null &&!batchSchedule.get().getPriceSelector2().equals("")) price = best.findElements(By.cssSelector(batchSchedule.get().getPriceSelector2()));
                    if(price.size() == 0 && batchSchedule.get().getPriceSelector3() != null &&!batchSchedule.get().getPriceSelector3().equals("")) price = best.findElements(By.cssSelector(batchSchedule.get().getPriceSelector3()));
                    String[] prices = price.get(0).getText().split("\n");
                    log.info("price : " + prices[batchSchedule.get().getPriceLocation()]);
                    
                    List<WebElement> deliveryFee = best.findElements(By.cssSelector(batchSchedule.get().getDeliveryFeeSelector1()));
                    if(deliveryFee.size() == 0 && batchSchedule.get().getDeliveryFeeSelector2() != null &&!batchSchedule.get().getDeliveryFeeSelector2().equals("")) deliveryFee = best.findElements(By.cssSelector(batchSchedule.get().getDeliveryFeeSelector2()));
                    if(deliveryFee.size() == 0 && batchSchedule.get().getDeliveryFeeSelector3() != null &&!batchSchedule.get().getDeliveryFeeSelector3().equals("")) deliveryFee = best.findElements(By.cssSelector(batchSchedule.get().getDeliveryFeeSelector3()));
                    String[] deliveryFees = deliveryFee.get(0).getText().split("\n");
                    if(batchSchedule.get().getBatchName().equals("네이버쇼핑")) {
                    	if(deliveryFees.length > batchSchedule.get().getDeliveryFeeLocation()) log.info("deliveryFee : " + deliveryFees[batchSchedule.get().getDeliveryFeeLocation()]);
                    	else log.info("deliveryFee : 별도확인필요");
                    }
                    else {
                    	// 타쇼핑몰일 경우..
                    }
                    
                    
                    List<WebElement> seller = best.findElements(By.cssSelector(batchSchedule.get().getSellerSelector1()));
                    if(seller.size() == 0 && batchSchedule.get().getSellerSelector2() != null &&!batchSchedule.get().getSellerSelector2().equals("")) seller = best.findElements(By.cssSelector(batchSchedule.get().getSellerSelector2()));
                    if(seller.size() == 0 && batchSchedule.get().getSellerSelector3() != null &&!batchSchedule.get().getSellerSelector3().equals("")) seller = best.findElements(By.cssSelector(batchSchedule.get().getSellerSelector3()));
                    String[] sellers = seller.get(0).getText().split("\n");
                    if(batchSchedule.get().getBatchName().equals("네이버쇼핑")) {
                    	String confirmSeller = sellers[batchSchedule.get().getSellerLocation()] == null || sellers[batchSchedule.get().getSellerLocation()].equals("")? batchSchedule.get().getBatchName() : sellers[batchSchedule.get().getSellerLocation()];
                        log.info("seller : " + (confirmSeller.equals("쇼핑몰별 최저가")? batchSchedule.get().getBatchName() : confirmSeller));
                    }
                    else {
                    	// 타쇼핑몰일 경우..
                    }
                    
                    List<WebElement> urls = best.findElements(By.cssSelector(batchSchedule.get().getUrlSelector1()));
                    if(urls.size() == 0 && batchSchedule.get().getUrlSelector2() != null &&!batchSchedule.get().getUrlSelector2().equals("")) urls = best.findElements(By.cssSelector(batchSchedule.get().getUrlSelector2()));
                    if(urls.size() == 0 && batchSchedule.get().getUrlSelector3() != null &&!batchSchedule.get().getUrlSelector3().equals("")) urls = best.findElements(By.cssSelector(batchSchedule.get().getUrlSelector3()));
                    String url = urls.get(0).getAttribute("href");
                    log.info("url : " + url);
            	}
            	catch(NoSuchElementException e) {
            		log.info("NoSuchElementException is expired");
            		skippedCount++;
            		totalSkippedSize.set(totalSkippedSize.get() + 1);
            		continue;
            	}
            	catch(Exception e) {
            		log.info("OtherException is expired");
            		skippedCount++;
            		totalSkippedSize.set(totalSkippedSize.get() + 1);
            		continue;
            	}
            }
        }
        log.info("target : " + bestContests.size() + ", inserted : " + (bestContests.size() - skippedCount) + ", error : " + skippedCount);
        
        log.info("#### crawling END ####");
    }
    
    public static void navigateToLastPage(Logger log) {
    	int pageNumber = 1;
    	crawling(log, pageNumber);
        while (true) {
            // 페이지에서 다음 버튼을 찾아 클릭합니다.
            WebElement nextButton = findNextButton();
            if (nextButton == null) {
            	// 8. WebDriver 종료
                driver.get().quit();
                
                log.info("totalSize : " + totalSize.get() + ", insertedSize : " + (totalSize.get() - totalSkippedSize.get()) +", totalSkippedSize : " + totalSkippedSize.get());
                log.info("#### driver END ####");
                
                break;
            }
            nextButton.click();
            pageNumber++;
            crawling(log, pageNumber);
            try {
                Thread.sleep(1000); // 1초 대기
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static WebElement findNextButton() {
        // TODO: 다음 버튼을 찾아서 반환하는 코드 작성
    	String byFunKey = "CSSSELECTOR";
    	WebDriverWait wait = new WebDriverWait(driver.get(), Duration.ofSeconds(1));
    	String selectString = batchSchedule.get().getNextButtonSelector();
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
        driver.set(new FirefoxDriver( options ));
        
        // 4. 웹페이지 요청
        driver.get().get(batchSchedule.get().getUrl());
        
        navigateToLastPage(log);
    }
}