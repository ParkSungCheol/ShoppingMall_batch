package com.example.batch.chunk;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import com.example.batch.Domain.Goods;
import com.example.batch.Service.GoodsServiceTest;

@Component
@Profile("test") // test 프로파일에서만 사용
public class MyBatisItemWriterInsertTest implements ItemWriter<List<Goods>>{
	
	private ThreadLocal<Logger> log = ThreadLocal.withInitial(() -> {
    	return LoggerFactory.getLogger(this.getClass());
    });
	@Autowired
	private GoodsServiceTest goodsService;
	
	@Override
	public void write(List<? extends List<Goods>> items){
		log.get().info("########## Insert Step finished #######");
		// 각 item 순회하며 insert(각 chunk가 끝날 때마다 DB에 적재)
		for(List<Goods> item : items) {
			goodsService.insertGoodsList(item);
		}
	}
}
