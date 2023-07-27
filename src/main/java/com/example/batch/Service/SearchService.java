package com.example.batch.Service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import com.example.batch.Domain.Search;
import com.example.batch.Mapper.SearchMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Profile("main")
public class SearchService {

	@Autowired
    private SearchMapper searchMapper;

    public List<Search> selectSearch() {
        return searchMapper.selectSearch();
    }
}