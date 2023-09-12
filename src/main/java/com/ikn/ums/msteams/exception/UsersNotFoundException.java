package com.ikn.ums.msteams.exception;

public class UsersNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public UsersNotFoundException() {
		super();
	}
	
	public UsersNotFoundException(String message) {
		super(message);
	}
	
}
