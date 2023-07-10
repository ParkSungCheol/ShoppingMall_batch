package com.example.batch.chunk;

import java.util.List;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import com.example.batch.Domain.Goods;

@Component
public class DataProcessor implements ItemProcessor<List<Goods>, List<Goods>>{

	@Override
	public List<Goods> process(List<Goods> items) throws Exception {
		// 데이터 전처리 필요 시 작성
		return items;
	}

}
