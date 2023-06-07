package com.example.batch.exception;

public class MyException extends Exception {
	private ThreadLocal<Integer> pageNum;

    public MyException(Throwable cause, int pageNum) {
        super(cause);
        this.pageNum.set(pageNum);
    }

    public int getPageNum() {
        return this.pageNum.get();
    }
}
