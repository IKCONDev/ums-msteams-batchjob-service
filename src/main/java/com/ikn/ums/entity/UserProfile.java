package com.ikn.ums.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "user_profile_tab")
public class UserProfile {
	
	@Id
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
