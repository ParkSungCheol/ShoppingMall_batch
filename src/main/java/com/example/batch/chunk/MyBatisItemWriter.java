package com.example.batch.chunk;

import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.example.batch.Domain.Goods;
import com.example.batch.Service.GoodsService;

@Component
public class MyBatisItemWriter implements ItemWriter<List<Goods>>{

	@Autowired
	private GoodsService goodsService;

	@Override
	public void write(List<? extends List<Goods>> items) throws Exception {
		// TODO Auto-generated method stub
		for(List<Goods> item : items) {
			goodsService.insertGoodsList(item);
		}
	}
}
