package com.example.batch.chunk;

import java.util.List;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import com.example.batch.Domain.Goods;

@Component
public class DataProcessor implements ItemProcessor<List<Goods>, List<Goods>>{

	@Override
	public List<Goods> process(List<Goods> items) throws Exception {
		// TODO Auto-generated method stub
		return items;
	}

}
