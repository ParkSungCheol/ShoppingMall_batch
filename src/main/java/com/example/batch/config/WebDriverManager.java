package com.example.batch.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.stereotype.Component;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class WebDriverManager{
    private static List<WebDriver> webDrivers = new ArrayList<>();
    private static int driver_num = -1;
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private boolean methodInProgress = false;

    public Integer createDriver() throws Exception{
    	lock.lock();
        try {
            // 다른 쓰레드가 이미 메서드를 수행 중인 경우 대기
            while (methodInProgress) {
                condition.await();
            }

            // 현재 쓰레드가 메서드를 수행 중임을 표시
            methodInProgress = true;

            // 메서드 수행 로직
            // 1. WebDriver 경로 설정
        	Path path = Paths.get("driver//geckodriver");
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
            
            WebDriver driver = new FirefoxDriver( options );
            webDrivers.add(driver);
            driver_num++;

            // 다른 쓰레드에게 수행 종료를 알림
            methodInProgress = false;
            condition.signalAll();
            
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            // 다른 쓰레드에게 수행 종료를 알림
            methodInProgress = false;
            condition.signalAll();
            throw e;
        } finally {
            lock.unlock();
        }
        return driver_num;
    }
    
    public WebDriver getDriver(int driver_num) {
    	return webDrivers.get(driver_num);
    }
    
    public void quitDriver(int driver_num) {
    	if(webDrivers.get(driver_num) != null && ((RemoteWebDriver) webDrivers.get(driver_num)).getSessionId() != null)
    		webDrivers.get(driver_num).quit();
    }

    public void quitAllDrivers() {
        for (WebDriver driver : webDrivers) {
        	if(driver != null && ((RemoteWebDriver) driver).getSessionId() != null)
        		driver.quit();
        }
        webDrivers.clear();
    }
}