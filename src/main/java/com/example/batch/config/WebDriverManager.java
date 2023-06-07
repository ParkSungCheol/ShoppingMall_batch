package com.example.batch.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.stereotype.Component;

@Component
public class WebDriverManager{
    private static List<WebDriver> webDrivers;
    private static final int MAX_THREADS = 1;
    
    public WebDriverManager() {
		// TODO Auto-generated constructor stub
    	// 메서드 수행 로직
    	
    	webDrivers = new ArrayList<>();
    	
    	for(int i = 0; i < MAX_THREADS; i++) {
	        // 1. WebDriver 경로 설정
	    	Path path = Paths.get("driver\\geckodriver.exe");
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
    	}
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
            if (driver instanceof RemoteWebDriver) {
                RemoteWebDriver remoteDriver = (RemoteWebDriver) driver;
                if (remoteDriver.getSessionId() != null) {
                    remoteDriver.quit();
                    try {
                        Thread.sleep(2000); // 2초 대기
                    } catch (InterruptedException e) {
                        // 대기 중인 스레드가 인터럽트되었을 경우 예외 처리
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        webDrivers.clear();
    }
}