package com.ikn.ums.exception;

public class UserPrincipalNotFoundException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UserPrincipalNotFoundException() {
		super();
	}
	
	public UserPrincipalNotFoundException(String errorMessage) {
		super(errorMessage);
	}

}
