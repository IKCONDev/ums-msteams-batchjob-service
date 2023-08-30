package com.ikn.ums.service.impl;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.azure.core.credential.AccessToken;
import com.ikn.ums.dto.UserProfileDto;
import com.ikn.ums.entity.UserProfile;
import com.ikn.ums.model.UserProfilesResponseWrapper;
import com.ikn.ums.repo.UserProfileRepository;
import com.ikn.ums.service.IUserProfileService;
import com.ikn.ums.utils.InitializeMicrosoftGraph;
import com.ikn.ums.utils.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserProfileServiceImpl implements IUserProfileService {
	
	@Autowired
	private UserProfileRepository userProfileRepository;
	
	@Autowired
	private InitializeMicrosoftGraph microsoftGraph;
	
	@Autowired 
	ObjectMapper mapper;
	
	private String accessToken = null;
	
	private AccessToken acToken = new AccessToken(this.accessToken,OffsetDateTime.now() );

	@Transactional
	@Override
	public Integer saveAllUsers() {
		List<UserProfileDto> userProfileDtoList = getUsers();
		int listSize = userProfileDtoList.size();
		List<UserProfile> userProfiles = new ArrayList<>(listSize);
		int insertedUserProfilesCount= 0 ;
		
		if(userProfileDtoList != null) {
			
			//convert dto to entity
			userProfiles = userProfileDtoList.stream()
			.map(source -> mapper.modelMapper.map(source, UserProfile.class))
			.collect(Collectors.toList());
			
			//save all user profiles
			List<UserProfile> insertedUserProfiles = userProfileRepository.saveAll(userProfiles);
			insertedUserProfilesCount = insertedUserProfiles.size();
		}
		
		//return count of user saved in db
	return insertedUserProfilesCount;
	}
	
	@SuppressWarnings("rawtypes")
	private List<UserProfileDto> getUsers() {

		// get access token from MS teams server , only if it is already null
				if (this.acToken.isExpired()) {
					log.info("Access Token expired");
					 this.acToken = this.microsoftGraph.initializeMicrosoftGraph();
					 log.info("Access Token Refreshed");
					 this.accessToken = this.acToken.getToken();
				}
		
		// get users
		String userProfileUrl = "https://graph.microsoft.com/v1.0/users?$filter=accountEnabled eq true and userType eq 'Member'";

		// prepare headers
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Authorization", "Bearer " + this.accessToken);
		httpHeaders.add("content-type", "application/json");

		// prepare http entity with headers
		HttpEntity httpEntity = new HttpEntity<>(httpHeaders);

		// prepare the rest template and hit the graph api user end point
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<UserProfilesResponseWrapper> userProfilesResponse = restTemplate.exchange(userProfileUrl,
				HttpMethod.GET, httpEntity, UserProfilesResponseWrapper.class);

		// get all user profiles from reponse object
		List<UserProfileDto> userDtoList = userProfilesResponse.getBody().getValue();

		return userDtoList;
	}

	@Override
	public List<UserProfile> fetchAllUsers() {
		List<UserProfile> userProfilesList =  userProfileRepository.findAll();
		return userProfilesList;
	}

	@Override
	@Transactional
	public String saveUser(String userPrincipalName) {
		UserProfile userProfile = null;
		UserProfileDto userProfileDto = getUser(userPrincipalName);
		UserProfile insertedUser = null;
		
		//check for null
		if(userProfileDto != null) {
			userProfile = new UserProfile();
			mapper.modelMapper.map(userProfileDto, userProfile);
			
			//save user
			insertedUser = userProfileRepository.save(userProfile);
		}
		return "User "+insertedUser.getUserPrincipalName()+" saved successfully";
	}
	
	@SuppressWarnings("rawtypes")
	private UserProfileDto getUser(String userPrincipalName) {
		
		// get access token from MS teams server , only if it is already null
				if (this.acToken.isExpired()) {
					log.info("Access Token expired");
					 this.acToken = this.microsoftGraph.initializeMicrosoftGraph();
					 log.info("Access Token Refreshed");
					 this.accessToken = this.acToken.getToken();
				}
		
		// get users
		String userProfileUrl = "https://graph.microsoft.com/v1.0/users/"+userPrincipalName;

		// prepare headers
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Authorization", "Bearer " + this.accessToken);
		httpHeaders.add("content-type", "application/json");

		// prepare http entity with headers
		HttpEntity httpEntity = new HttpEntity<>(httpHeaders);

		// prepare the rest template and hit the graph api user end point
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<UserProfileDto> userProfileResponse = restTemplate.exchange(userProfileUrl,
				HttpMethod.GET, httpEntity, UserProfileDto.class);

		// get all user profiles from reponse object
		UserProfileDto userDto = userProfileResponse.getBody();

		return userDto;
		
	}

	@Override
	public UserProfileDto getUserProfile(String userPrincipalName) {
		Optional<UserProfile> optUserProfile = userProfileRepository.findByUserPrincipalName(userPrincipalName);
		UserProfileDto userProfileDto = null;
		if(optUserProfile.isPresent()) {
			userProfileDto = new UserProfileDto();
			mapper.modelMapper.map(optUserProfile.get(), userProfileDto);
		}
		return userProfileDto;
	}

	@Override
	public List<UserProfile> getUsersByMailIds(List<String> mailIds) {
		List<UserProfile> userProfilesList = userProfileRepository.findByMailIn(mailIds);
		return userProfilesList;
	}
	

}
