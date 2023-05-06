package com.example.batch.Mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.batch.Domain.BatchSchedule;

@Mapper
public interface BatchScheduleMapper {

    List<BatchSchedule> getBatchScheduleList();
}