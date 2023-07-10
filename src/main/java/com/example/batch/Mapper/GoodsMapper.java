package com.example.batch.Mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.example.batch.Domain.Goods;

@Mapper
public interface GoodsMapper {
    void insertGoodsList(@Param("goodsList") List<Goods> goodsList);
    void deleteGoodsList();
}