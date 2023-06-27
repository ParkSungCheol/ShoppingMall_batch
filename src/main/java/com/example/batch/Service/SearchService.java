package com.example.batch.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.batch.Domain.Search;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class SearchService {

	@Autowired
    private SearchService searchService;

    public List<Search> selectSearch() {
        return searchService.selectSearch();
    }
}