package com.ikn.ums.model;

import lombok.Data;

@Data
public class AccessTokenResponseModel {
	
	private String accesstoken;
	private String expiry;
	//private String refreshToken;
	
}
