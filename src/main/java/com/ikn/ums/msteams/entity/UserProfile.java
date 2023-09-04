package com.ikn.ums.msteams.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "user_profile_tab")
@AllArgsConstructor
@NoArgsConstructor
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
