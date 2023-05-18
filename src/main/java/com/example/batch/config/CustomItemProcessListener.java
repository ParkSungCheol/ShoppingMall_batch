package com.example.batch.config;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import com.example.batch.Domain.Goods;

@Component
public class CustomItemProcessListener implements ItemProcessListener<Goods, Goods>{

	@Autowired
	private TaskExecutor taskExecutor;
	@Autowired
	private Connection connection;
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Override
	public void beforeProcess(Goods item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterProcess(Goods item, Goods result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProcessError(Goods item, Exception e) {
		log.info("onProcessError is triggered!");
		// TODO Auto-generated method stub
		ThreadPoolTaskExecutor tte = (ThreadPoolTaskExecutor) taskExecutor;
		try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                log.info("after onProcessError is triggered, db connection closed.");
                tte.shutdown();
            }
        } catch (SQLException i) {
            i.printStackTrace();
        }
	}

}
