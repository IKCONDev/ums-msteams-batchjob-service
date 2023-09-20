package com.ikn.ums.msteams.service;

import java.util.List;

import com.ikn.ums.msteams.dto.UserProfileDto;
import com.ikn.ums.msteams.entity.UserProfile;

public interface UserProfileService {
	
	Integer saveAzureUsers();
	List<UserProfile> fetchAllUsers();
	String saveUser(String userPrincipalName);
	UserProfileDto getUserProfile(String userPrincipalName);
	List<UserProfile> getUsersByMailIds(List<String> mailIds);	

}
