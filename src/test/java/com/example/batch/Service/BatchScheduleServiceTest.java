package com.example.batch.Service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.example.batch.Domain.BatchSchedule;
import com.example.batch.Mapper.BatchScheduleMapperTest;

@Service
public class BatchScheduleServiceTest {

    private BatchScheduleMapperTest batchScheduleMapperTest;

    public List<BatchSchedule> getBatchScheduleList(int startBatchNum, int endBatchNum) {
    	batchScheduleMapperTest = new BatchScheduleMapperTest();
        return batchScheduleMapperTest.getBatchScheduleList(startBatchNum, endBatchNum);
    }
}