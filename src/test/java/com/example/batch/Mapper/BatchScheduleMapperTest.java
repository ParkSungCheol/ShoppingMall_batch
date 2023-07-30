package com.example.batch.Mapper;

import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.context.annotation.Profile;

import com.example.batch.Domain.BatchSchedule;

@Profile("test") // test 프로파일에서만 사용
public class BatchScheduleMapperTest {

    public List<BatchSchedule> getBatchScheduleList(@Param("startBatchNum") int startBatchNum,@Param("endBatchNum") int endBatchNum) {
    	List<BatchSchedule> arr = new ArrayList<BatchSchedule>();
    	arr.add(new BatchSchedule(1, "test1", null, null, null,	null, null, null, null,	null, null, null, null,	null, null, null, null, null, null, null, null, null, null, null, null, null, null));
    	arr.add(new BatchSchedule(2, "test2", null, null, null,	null, null, null, null,	null, null, null, null,	null, null, null, null, null, null, null, null, null, null, null, null, null, null));
    	arr.add(new BatchSchedule(3, "test3", null, null, null,	null, null, null, null,	null, null, null, null,	null, null, null, null, null, null, null, null, null, null, null, null, null, null));
    	arr.add(new BatchSchedule(4, "test4", null, null, null,	null, null, null, null,	null, null, null, null,	null, null, null, null, null, null, null, null, null, null, null, null, null, null));
    	arr.add(new BatchSchedule(5, "test5", null, null, null,	null, null, null, null,	null, null, null, null,	null, null, null, null, null, null, null, null, null, null, null, null, null, null));
    	arr.add(new BatchSchedule(6, "test6", null, null, null,	null, null, null, null,	null, null, null, null,	null, null, null, null, null, null, null, null, null, null, null, null, null, null));
    	arr.add(new BatchSchedule(7, "test7", null, null, null,	null, null, null, null,	null, null, null, null,	null, null, null, null, null, null, null, null, null, null, null, null, null, null));
    	arr.add(new BatchSchedule(8, "test8", null, null, null,	null, null, null, null,	null, null, null, null,	null, null, null, null, null, null, null, null, null, null, null, null, null, null));
    	return arr;
    };
    
}