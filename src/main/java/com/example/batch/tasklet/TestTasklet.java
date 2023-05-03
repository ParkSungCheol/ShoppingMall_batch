package com.example.batch.tasklet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
public class TestTasklet implements Tasklet {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        String url = "https://search.shopping.naver.com/search/all?query=해피머니";
        Document doc = Jsoup.connect(url).get();
        
        Elements elem = doc.select("div.basicList_title__VfX3c");
        log.info(elem.toString());
        return RepeatStatus.FINISHED;
    }
}