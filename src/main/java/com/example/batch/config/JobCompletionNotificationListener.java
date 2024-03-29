package com.example.batch.config;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import com.example.batch.Domain.JobStatus;
import com.example.batch.Domain.Search;
import com.example.batch.Domain.esGoods;
import com.example.batch.Service.BatchScheduleService;
import com.example.batch.Service.ElasticsearchService;
import com.example.batch.Service.JobStatusService;
import com.example.batch.Service.PhoneService;
import com.example.batch.Service.SearchService;
import com.example.batch.Service.SlackService;

@Component
@Profile("main")
public class JobCompletionNotificationListener implements JobExecutionListener {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private static CountDownLatch latch = null;
    private SlackService slackService;
    private static int failedCount = 0;
    private static int successedCount = 0;
	private JobStatusService jobStatusService;
	private SearchService searchService;
	private ElasticsearchService elasticsearchService;
	private PhoneService phoneService;
    
    public JobCompletionNotificationListener(DataSource dataSource, BatchScheduleService batchScheduleService, SlackService slackService, JobStatusService jobStatusService, SearchService searchService, ElasticsearchService elasticsearchService, PhoneService phoneService) {
        this.slackService = slackService;
        this.jobStatusService = jobStatusService;
        this.searchService = searchService;
        this.elasticsearchService = elasticsearchService;
        this.phoneService = phoneService;
    }

    @Override
    // job 시작 전 호출
    public void beforeJob(JobExecution jobExecution) {
    	// CountDownLatch 초기화
        if(latch == null) {
        	latch = new CountDownLatch((int) (long) jobExecution.getJobParameters().getLong("jobCount"));
        }
    }
    
    @Override
    // job 종료 후 호출
    public void afterJob(JobExecution jobExecution){
    	jobExecution.setExitStatus(ExitStatus.UNKNOWN);
    	
    	// 남은 jobCount
    	latch.countDown();
    	log.info("jobCount : {}", latch.getCount());
    	
    	// Slack에 보낼 메시지 작성
    	int flag = 0; // flag == 0은 오류메시지, flag == 1은 정상메시지
    	String target = (String) jobExecution.getExecutionContext().get("target");
    	String account = (String) jobExecution.getExecutionContext().get("account");
    	log.info("target : {}", target);
    	String msg = "[account] " + account + "\n[target] " + target + "\n[result] : ";
    	int totalSize = (int) jobExecution.getExecutionContext().get("totalSize");
    	int insertSize = (int) jobExecution.getExecutionContext().get("insertSize");
		Date startTime = jobExecution.getStartTime();
		// 대한민국 표준시(KST)로 변환하기
        TimeZone kstTimeZone = TimeZone.getTimeZone("Asia/Seoul");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(kstTimeZone);
        String startFormattedTime = dateFormat.format(startTime);
        Date endTime = jobExecution.getEndTime();
        long executionTime = endTime.getTime() - startTime.getTime();
        // job 수행시간
        String executionFormattedTime = formatExecutionTime(executionTime);
        
        // job이 실패했다면
		if(jobExecution.getStatus() == BatchStatus.FAILED) {
			log.info("############## FAILED ###############");
			// 실패 job 개수 집계
			failedCount++;
			msg += "FAILED \n";
			msg += "totalSize : " + totalSize + " ";
			msg += "insertedSize : " + insertSize + "\n";
			msg += "notInserted : " + (totalSize - insertSize) + "\n";
			msg += "startTime : " + startFormattedTime + "\n";
			msg += "runTime : " + executionFormattedTime + "\n";
			msg += "[ errorLog ]\n" + jobExecution.getAllFailureExceptions().get(0).getMessage();
		}
		
		// job이 성공했다면
		else if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
			// 성공 job 개수 집계
			successedCount++;
			msg += "COMPLETED \n";
			msg += "totalSize : " + totalSize + " ";
			msg += "insertedSize : " + insertSize + "\n";
			msg += "notInserted : " + (totalSize - insertSize) + "\n";
			msg += "startTime : " + startFormattedTime + "\n";
			msg += "runTime : " + executionFormattedTime + "\n";
			flag = 1; // flag == 0은 오류메시지, flag == 1은 정상메시지
		}
		// Slack에 메시지 보내기
		slackService.call(flag, msg);
    	
		synchronized (this) {
			// 모든 job이 완료되었다면
        	if(latch != null && latch.getCount() == 0) {
            	// 모든 쓰레드가 완료될 때까지 대기
                try {
    				latch.await();
    			} catch (Exception e1) {
    				e1.printStackTrace();
    				jobExecution.setStatus(BatchStatus.FAILED);
    			}
                latch = null;
	    		
	    		// 해당 계정의 모든 job이 종료되었음을 DB에 적재
	    		JobStatus jobStatus = new JobStatus();
	    		jobStatus.setBatchId(Integer.parseInt(account));
	    		jobStatusService.endJobStatus(jobStatus);
	    		
	    		List<JobStatus> jobStatusList = jobStatusService.selectPJobStatus();
	    		// 모든 계정의 job이 종료되었다면
	    		if(jobStatusList.size() == 0) {
	    			try {
	    				// 아직 Mysql에 적재된 데이터가 ES에 적재되지 않았을 수 있으므로 최대 10분 wait
						Thread.currentThread().sleep(600000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
	    			
	    			// 가격 설정값보다 낮은지 검증 후 핸드폰으로 전송
	    			List<Search> searchList = searchService.selectSearch();
	    			// 시간대를 Asia/Seoul로 설정
	    	        TimeZone seoulTimeZone = TimeZone.getTimeZone("Asia/Seoul");
	    	        TimeZone.setDefault(seoulTimeZone);
	    	        // 현재 날짜를 YYYY-MM-DD 형식으로 가져오기
	    	        LocalDate currentDate = LocalDate.now();
	    	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	    	        String formattedDate = currentDate.format(formatter);
	    			for(Search search : searchList) {
	    				List<esGoods> goodsList = elasticsearchService.getDataFromElasticsearch(search, formattedDate);
	    				if(goodsList.size() > 0) {
	    					// 핸드폰으로 전송
	    					try {
		    					String phoneMsg = search.getSearchValue() + " : 최저가 " + goodsList.get(0).getPrice() + "원 입니다.";
		    					phoneService.sendMessage(search.getPhone(), phoneMsg);
	    					} catch(Exception e) {
	    						e.printStackTrace();
	    					}
	    				}
	    			}
	    		}
	    		log.info("#### ALL job END ####");
	    		
	    		// 그동안 집계한 전체 job의 성공/실패 횟수를 Slack 메시지로 전달
	    		String finalMsg = "All Job Complete\n";
	    		finalMsg += "Failed : " + failedCount + "\n";
	    		finalMsg += "Successed : " + successedCount;
	    		slackService.call(1, finalMsg);
	    	}
		}
		
		// jobExecution이 COMPLETED 상태가 아닌 경우에만 ExitStatus를 FAILED로 설정
    	if (!jobExecution.getStatus().equals(BatchStatus.COMPLETED)) {
    	    jobExecution.setExitStatus(ExitStatus.FAILED);
    	}
    	else if(jobExecution.getExitStatus().equals(ExitStatus.UNKNOWN)) jobExecution.setExitStatus(ExitStatus.COMPLETED);
    }
    
    public String formatExecutionTime(long executionTime) {
        long seconds = executionTime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        long remainingSeconds = seconds % 60;
        long remainingMinutes = minutes % 60;
        
        String formattedTime = String.format("%02d:%02d:%02d", hours, remainingMinutes, remainingSeconds);
        
        return formattedTime;
    }
}