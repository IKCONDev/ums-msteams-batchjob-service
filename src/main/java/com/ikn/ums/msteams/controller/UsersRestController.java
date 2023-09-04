package com.ikn.ums.msteams.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ikn.ums.msteams.dto.UserProfileDto;
import com.ikn.ums.msteams.entity.UserProfile;
import com.ikn.ums.msteams.service.IUserProfileService;

@RestController
@RequestMapping("/azure/users")
public class UsersRestController {
	
	@Autowired
	private IUserProfileService userProfileService;
	
	@PostMapping("/save")
	public ResponseEntity<?> saveAllUserProfiles(){
		try {
			int insertedUsersCount =  userProfileService.saveAzureUsers();
			return new ResponseEntity<>(insertedUsersCount, HttpStatus.CREATED);
		}catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Users data could not be saved , Please try again",HttpStatus.INTERNAL_SERVER_ERROR);
		}		
	}
	
	@GetMapping("/get")
		public ResponseEntity<?> getAllUserProfiles(){
		try {
			List<UserProfile> userProfiles = userProfileService.fetchAllUsers();
			return new ResponseEntity<>(userProfiles, HttpStatus.OK);
		}catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("users data could not be retrived, please try again", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping("/save/{userPrincipalName}")
	public ResponseEntity<?> saveUserProfile(@PathVariable String userPrincipalName){
		try {
			UserProfileDto dbUserProfile = userProfileService.getUserProfile(userPrincipalName);
			if(dbUserProfile == null) {
				String message = userProfileService.saveUser(userPrincipalName);
				return new ResponseEntity<>(message, HttpStatus.CREATED);
			}else {
				return new ResponseEntity<>("User already exists in DB, duplicate users not allowed", HttpStatus.NOT_ACCEPTABLE);
			}
		}catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("User could not be saved, please try again",HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/get/{userPrincipalName}")
	public ResponseEntity<?> getUserProfile(@PathVariable String userPrincipalName){
		try {
			UserProfileDto userProfile = userProfileService.getUserProfile(userPrincipalName);
			if(userProfile != null) {
				return new ResponseEntity<>(userProfile, HttpStatus.OK);
			}else {
				return new ResponseEntity<>("User Not Found", HttpStatus.NOT_FOUND);
			}
		}catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Error while retreiving user details", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
}
