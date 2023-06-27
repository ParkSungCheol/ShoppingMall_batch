package com.example.batch.Service;

import java.util.ArrayList;
import java.util.List;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import com.example.batch.Domain.Search;
import com.example.batch.Domain.esGoods;

@Service
public class ElasticsearchService {
    private final ElasticsearchOperations elasticsearchOperations;
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    public ElasticsearchService(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public List<esGoods> getDataFromElasticsearch(Search search, String date) {
    	// BoolQueryBuilder 생성
    	BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

    		// must 조건 추가
		BoolQueryBuilder mustQuery = QueryBuilders.boolQuery();
		mustQuery.should(QueryBuilders.matchQuery("name", search.getSearchValue()));
		mustQuery.should(QueryBuilders.matchQuery("name.nori", search.getSearchValue()));
		mustQuery.should(QueryBuilders.matchQuery("name.ngram", search.getSearchValue()));
		boolQuery.must(mustQuery);
		
    	boolQuery.filter(QueryBuilders.termQuery("is_deleted", 0));
    	if(search.getTerm().equals("1")) {
    		boolQuery.filter(QueryBuilders.rangeQuery("price").lt(search.getPrice()));
    	}
    	else if(search.getTerm().equals("2")) {
    		boolQuery.filter(QueryBuilders.rangeQuery("price").lte(search.getPrice()));
    	}
    	else if(search.getTerm().equals("3")) {
    		boolQuery.filter(QueryBuilders.rangeQuery("price").gte(search.getPrice()));
    	}
    	else if(search.getTerm().equals("4")) {
    		boolQuery.filter(QueryBuilders.rangeQuery("price").gt(search.getPrice()));
    	}

    	// insertion_time 조건 추가
    	boolQuery.filter(QueryBuilders.matchQuery("insertion_time", date));
    	
    	// NativeSearchQuery를 사용하여 쿼리 실행
    	NativeSearchQueryBuilder searchQuery = new NativeSearchQueryBuilder()
    	        .withQuery(boolQuery);
    	
    	// ORDER BY 절 추가
    	searchQuery.withSort(Sort.by(Sort.Direction.ASC, "price"));

    	// NativeSearchQuery를 사용하여 쿼리 생성
    	NativeSearchQuery searchQueryComplete = searchQuery.build();
    	
    	logger.info("[ getDataFromElasticsearch ] Query : " + searchQueryComplete.getQuery().toString());
    	
        SearchHits<esGoods> searchHits = elasticsearchOperations.search(searchQueryComplete, esGoods.class);
        List<esGoods> dataList = new ArrayList<>();
        for (SearchHit<esGoods> searchHit : searchHits) {
            dataList.add(searchHit.getContent());
        }
        logger.info("[ getDataFromElasticsearch ] dataList.size() : " + dataList.size());
        return dataList;
    }
}
