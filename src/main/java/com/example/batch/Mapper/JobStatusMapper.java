package com.example.batch.Mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.example.batch.Domain.JobStatus;

@Mapper
public interface JobStatusMapper {

    List<JobStatus> selectPJobStatus();
    void startJobStatus(@Param("jobStatus") JobStatus jobStatus);
    void endJobStatus(@Param("jobStatus") JobStatus jobStatus);
}