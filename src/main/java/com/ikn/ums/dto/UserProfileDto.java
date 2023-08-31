package com.ikn.ums.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ikn.ums.entity.UserProfile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
