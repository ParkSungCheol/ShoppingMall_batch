package com.example.batch.Service;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import com.example.batch.Domain.Goods;
import com.example.batch.Mapper.GoodsMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Profile("main")
public class GoodsService {

	@Autowired
    private GoodsMapper goodsMapper;

    public void insertGoodsList(List<Goods> goodsList) {
    	goodsMapper.insertGoodsList(goodsList);
    }
    
    public void deleteGoodsList() {
    	goodsMapper.deleteGoodsList();
    }
    
    public void deleteGoodsListByProductCode(String productCode) {
    	goodsMapper.deleteGoodsListByProductCode(productCode);
    }
}