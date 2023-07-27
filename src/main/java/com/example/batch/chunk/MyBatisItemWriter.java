package com.example.batch.chunk;

import java.util.List;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import com.example.batch.Domain.Goods;
import com.example.batch.Service.GoodsService;

@Component
@Profile("main")
public class MyBatisItemWriter implements ItemWriter<List<Goods>>{

	@Autowired
	private GoodsService goodsService;

	@Override
	public void write(List<? extends List<Goods>> items) throws Exception {
		// 각 item 순회하며 insert(각 chunk가 끝날 때마다 DB에 적재)
		for(List<Goods> item : items) {
			goodsService.insertGoodsList(item);
		}
	}
}
