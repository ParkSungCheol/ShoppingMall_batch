package com.example.batch.Service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import com.example.batch.Domain.JobStatus;
import com.example.batch.Mapper.JobStatusMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Profile("main")
public class JobStatusService {

	@Autowired
    private JobStatusMapper jobStatusMapper;

    public List<JobStatus> selectPJobStatus() {
        return jobStatusMapper.selectPJobStatus();
    }
    
    public void startJobStatus(JobStatus jobStatus) {
        jobStatusMapper.startJobStatus(jobStatus);
    }
    
    public void endJobStatus(JobStatus jobStatus) {
        jobStatusMapper.endJobStatus(jobStatus);
    }
}