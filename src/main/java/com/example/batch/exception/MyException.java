package com.example.batch.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyException extends Exception {
	private ThreadLocal<Integer> pageNum = new ThreadLocal<>();
	private ThreadLocal<Logger> log = ThreadLocal.withInitial(() -> {
    	return LoggerFactory.getLogger(this.getClass());
    });

    public MyException(Throwable cause, int pageNum) {
        super(cause);
        this.pageNum.set(pageNum);
    }

    public int getPageNum() {
        return this.pageNum.get();
    }
}
