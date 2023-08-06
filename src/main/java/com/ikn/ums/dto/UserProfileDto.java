package com.ikn.ums.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@SuppressWarnings("unused")
public class UserProfileDto {
	
	@JsonProperty("id")
	private String userId;
	private String userPrincipalName;
	private String displayName;
	private String givenName;
	private String department;
	private String mail;
	private String mobilePhone;
	private String jobTitle;
	private String surname;
	
}
