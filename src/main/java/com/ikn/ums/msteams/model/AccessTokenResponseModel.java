package com.ikn.ums.msteams.model;

import lombok.Data;

@Data
public class AccessTokenResponseModel {
	
	private String accesstoken;
	private String expiry;
	//private String refreshToken;
	
}
