package com.example.batch.config;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import com.example.batch.Service.SlackService;

@Component
@Profile("main")
public class TimeoutDecider implements JobExecutionDecider {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private final Map<Long, Integer> retryCounts = new HashMap<>();
    @Autowired
    private SlackService slackService;

    @Override
    public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
        long jobExecutionId = jobExecution.getId();
        
        // 새롭게 시작한 job의 경우 재시도횟수 3회 부여
        if (!retryCounts.containsKey(jobExecutionId)) {
            retryCounts.put(jobExecutionId, 3);
        }

        int currentRetryCount = retryCounts.get(jobExecutionId);
        
        // Slack 메시지 작성
        String msg = "[account] " + jobExecution.getExecutionContext().get("account") + "\n";
        msg += "[target] " + jobExecution.getExecutionContext().get("target") + "\n";
        msg += "[startPageNum] " + jobExecution.getExecutionContext().get("startPageNum") + "\n";
        retryCounts.put(jobExecutionId, currentRetryCount - 1);
        msg += "[remainCount] " + retryCounts.get(jobExecutionId) + "번 남았습니다.\n";

        // 재시도횟수 모두 소진한 경우
        if (currentRetryCount == 0) {
            return new FlowExecutionStatus("FAILED");
        } 
        // 실패 없이 성공한 경우
        else if (stepExecution.getFailureExceptions().isEmpty()) {
            log.info("No failure exceptions. Job completed.");
            return new FlowExecutionStatus("COMPLETED");
        } 
        // 실패한 경우
        else {
            log.info("Failure exceptions occurred. Retry this.");
            msg += "[errorLog] " + stepExecution.getFailureExceptions();
            slackService.call(0, msg);
            return new FlowExecutionStatus("RESTART");
        }
    }
}