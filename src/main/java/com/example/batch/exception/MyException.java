package com.example.batch.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyException extends Exception {
	private ThreadLocal<Integer> pageNum;
	private ThreadLocal<Logger> log = ThreadLocal.withInitial(() -> {
    	return LoggerFactory.getLogger(this.getClass());
    });

    public MyException(Throwable cause, int pageNum) {
        super(cause);
        log.get().info("pageNum : " + pageNum);
        this.pageNum.set(pageNum);
    }

    public int getPageNum() {
        return this.pageNum.get();
    }
}
