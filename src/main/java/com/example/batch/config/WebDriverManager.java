package com.example.batch.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

@Component
public class WebDriverManager{
    private static Map<Integer, WebDriver> webDrivers;
    
    public WebDriverManager() {
		// TODO Auto-generated constructor stub
    	// 메서드 수행 로직
    	webDrivers = new HashMap<>();
	}
    
    public WebDriver makeDriver() {
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
        return driver;
    }
    
    public WebDriver getDriver(int driver_num) {
    	WebDriver driver = webDrivers.get(driver_num);
    	if(driver != null && ((RemoteWebDriver) driver).getSessionId() != null) {
    		// 현재 웹드라이버가 실행 중인 경우 초기화 작업 수행
            try {
            	driver.close(); // 현재 창 닫기
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                wait.until(ExpectedConditions.numberOfWindowsToBe(0)); // 모든 창이 닫힐 때까지 대기
            } catch (Exception e) {
                // 예외 처리
                e.printStackTrace();
            }
            
    		// 원하는 초기 상태 설정
            driver.manage().deleteAllCookies();
//    		webDrivers.get(driver_num).manage().window().maximize();
    		return driver;
    	}
    	else {
    		driver = makeDriver();
    		webDrivers.put(driver_num, driver);
    		return driver;
    	}
    }
    
    public void quitDriver(int driver_num) {
    	if(webDrivers.get(driver_num) != null && ((RemoteWebDriver) webDrivers.get(driver_num)).getSessionId() != null) {
    		try {
    			webDrivers.get(driver_num).close(); // 현재 창 닫기
                WebDriverWait wait = new WebDriverWait(webDrivers.get(driver_num), Duration.ofSeconds(10));
                wait.until(ExpectedConditions.numberOfWindowsToBe(0)); // 모든 창이 닫힐 때까지 대기
            } catch (Exception e) {
                // 예외 처리
                e.printStackTrace();
            } finally {
                try {
                	webDrivers.get(driver_num).quit(); // 웹 드라이버 종료
                } catch (Exception e) {
                    // 예외 처리
                    e.printStackTrace();
                }
            }
    	}
    }

    public void quitAllDrivers() {
        for (WebDriver driver : webDrivers.values()) {
            if (driver instanceof RemoteWebDriver) {
                RemoteWebDriver remoteDriver = (RemoteWebDriver) driver;
                if (remoteDriver.getSessionId() != null) {
                    try {
                        driver.close(); // 현재 창 닫기
                        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                        wait.until(ExpectedConditions.numberOfWindowsToBe(0)); // 모든 창이 닫힐 때까지 대기
                    } catch (Exception e) {
                        // 예외 처리
                        e.printStackTrace();
                    } finally {
                        try {
                            remoteDriver.quit(); // 웹 드라이버 종료
                        } catch (Exception e) {
                            // 예외 처리
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        webDrivers.clear();
    }

}