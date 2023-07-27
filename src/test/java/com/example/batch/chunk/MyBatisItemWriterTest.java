package com.example.batch.chunk;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import com.example.batch.Domain.Goods;

@Component
@Profile("test") // test 프로파일에서만 사용
public class MyBatisItemWriterTest implements ItemWriter<List<Goods>>{
	
	private ThreadLocal<Logger> log = ThreadLocal.withInitial(() -> {
    	return LoggerFactory.getLogger(this.getClass());
    });
	
	@Override
	public void write(List<? extends List<Goods>> items){
		// 각 item 순회하며 insert(각 chunk가 끝날 때마다 DB에 적재)
		log.get().info("########## Step finished #######");
	}
}
