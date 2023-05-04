package com.example.batch.tasklet;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
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
import org.springframework.stereotype.Component;

import io.netty.handler.timeout.TimeoutException;

@Component
public class TestTasklet implements Tasklet {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        String url = "https://search.shopping.naver.com/search/all?query=해피머니";
        runSelenium(url, log);
        return RepeatStatus.FINISHED;
    }
    
    private static void runSelenium(String URL, Logger log) throws Exception {
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
        options.addArguments("--disable-default-apps");     // 기본앱 사용안함
        
        // 3. WebDriver 객체 생성
        WebDriver driver = new FirefoxDriver( options );
                
        try {
            
            // 4. 웹페이지 요청
            driver.get(URL);
            
            // 5. 페이지 로딩을 위한 최대 5초 대기
            driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
            
            // 6. 조회, 로드될 때까지 최대 10초 대기
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            String byFunKey = "CSSSELECTOR";
            String selectString = "div.list_basis > div:nth-child(1)";
//            String byFunKey = "XPATH";
//            String selectString = "//*[@id=\"mArticle\"]/div[2]/ul/li[3]/a";
            WebElement parent = wait.until(ExpectedConditions.presenceOfElementLocated( 
                    byFunKey.equals("XPATH") ? By.xpath(selectString) : By.cssSelector(selectString) ));
//            log.info("#### innerHTML : \n" + parent.getAttribute("innerHTML"));
            
            // 7. 콘텐츠 조회
            List<WebElement> bestContests = parent.findElements(By.xpath("*"));
            log.info( "등록된 상품 수 : " + bestContests.size() );
            if (bestContests.size() > 0) {
                for (WebElement best : bestContests) {
                    String title = best.findElement(By.cssSelector("div:nth-child(1) > div:nth-child(1) > div:nth-child(2) > div:nth-child(1) > a:nth-child(1)")).getText();
                    log.info("title : " + title);
                    String price = best.findElement(By.cssSelector("div:nth-child(1) > div:nth-child(1) > div:nth-child(2) > div:nth-child(2) > strong:nth-child(2) > span:nth-child(1) > span:nth-child(1) > span:nth-child(1)")).getText();
                    log.info("price : " + price);
                }
            }
            
            log.info("#### crawling END ####");
            
        } catch ( TimeoutException e ) {
            e.printStackTrace();
            log.info(e.toString());            
        } catch ( Exception e ) {
            e.printStackTrace();
            log.info(e.toString());         
        }
        
        // 8. WebDriver 종료
        driver.quit();
        
        log.info("#### driver END ####");
    }
}