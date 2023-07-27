package com.example.batch.Service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import com.example.batch.Domain.BatchSchedule;
import com.example.batch.Mapper.BatchScheduleMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Profile("main")
public class BatchScheduleService {

	@Autowired
    private BatchScheduleMapper batchScheduleMapper;

    public List<BatchSchedule> getBatchScheduleList(int startBatchNum, int endBatchNum) {
        return batchScheduleMapper.getBatchScheduleList(startBatchNum, endBatchNum);
    }
}