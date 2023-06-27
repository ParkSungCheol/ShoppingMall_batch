package com.example.batch.Mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.example.batch.Domain.Search;

@Mapper
public interface SearchMapper {

    List<Search> selectSearch();
}