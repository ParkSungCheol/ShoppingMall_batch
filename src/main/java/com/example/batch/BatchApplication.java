package com.example.batch;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class BatchApplication implements CommandLineRunner { 

	private static ConfigurableApplicationContext context;
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	public static void main(String[] args) throws IOException {
		context = SpringApplication.run(BatchApplication.class, args);
		MainBatch mainBatch = new MainBatch(context);
		mainBatch.execute(args);
		
		// 애플리케이션 종료
	    context.close();
	    System.exit(0);
	}
	
	@Override
    public void run(String... args) throws Exception 
	  {
		log.info("BatchApplication running..");
	  }
}
