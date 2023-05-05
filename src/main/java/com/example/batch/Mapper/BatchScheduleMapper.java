package com.example.batch.Mapper;

import org.apache.ibatis.annotations.Mapper;

import com.example.batch.Domain.BatchSchedule;

import java.util.List;

@Mapper
public interface BatchScheduleMapper {

    List<BatchSchedule> getBatchScheduleList();
}