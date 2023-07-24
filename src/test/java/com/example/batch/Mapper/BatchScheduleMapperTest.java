package com.example.batch.Mapper;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.example.batch.Domain.BatchSchedule;

public class BatchScheduleMapperTest {

    public List<BatchSchedule> getBatchScheduleList(@Param("startBatchNum") int startBatchNum,@Param("endBatchNum") int endBatchNum) {
    	List<BatchSchedule> arr = new ArrayList<BatchSchedule>();
    	arr.add(new BatchSchedule(1, "test1", null, null, null,	null, null, null, null,	null, null, null, null,	null, null, null, null, null, null, null, null, null, null, null, null, null, null));
    	arr.add(new BatchSchedule(2, "test2", null, null, null,	null, null, null, null,	null, null, null, null,	null, null, null, null, null, null, null, null, null, null, null, null, null, null));
    	arr.add(new BatchSchedule(3, "test3", null, null, null,	null, null, null, null,	null, null, null, null,	null, null, null, null, null, null, null, null, null, null, null, null, null, null));
    	arr.add(new BatchSchedule(4, "test4", null, null, null,	null, null, null, null,	null, null, null, null,	null, null, null, null, null, null, null, null, null, null, null, null, null, null));
    	return arr;
    };
    
}