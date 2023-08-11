package com.ikn.ums.service;

import java.util.List;

import com.ikn.ums.dto.UserProfileDto;
import com.ikn.ums.entity.UserProfile;

public interface IUserProfileService {
	
	Integer saveAllUsers();
	List<UserProfile> fetchAllUsers();
	String saveUser(String userPrincipalName);
	UserProfileDto getUserProfile(String userPrincipalName);
	List<UserProfile> getUsersByMailIds(List<String> mailIds);	

}
