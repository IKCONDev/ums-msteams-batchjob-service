package com.ikn.ums.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ikn.ums.dto.EventDto;
import com.ikn.ums.service.ITeamsBatchService;

@RestController
@RequestMapping("/api/teams")
public class TeamsBatchJobController {
	
	@Autowired
	private ITeamsBatchService teamsBatchService;

	@GetMapping("/auth/token")
	public ResponseEntity<?> authenticateTeamsServer(){
		try {
			String accessToken = teamsBatchService.initializeMicrosoftGraph();
			return new ResponseEntity<>(accessToken, HttpStatus.ACCEPTED);
		}catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GetMapping("/batch-process")
	public ResponseEntity<?> meetingDataBatchProcessing(){
		try {
			teamsBatchService.initializeMicrosoftGraph();
			teamsBatchService.performBatchProcessing();
			return new ResponseEntity<>("Batch Processing Successfull",HttpStatus.OK);
		}catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Error while batch processing", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}	
	
	

	@GetMapping("/batch-user/{userPrincipalName}")
	public ResponseEntity<?> meetingUserDataBatchProcessing(@PathVariable String userPrincipalName){
		try {
			teamsBatchService.initializeMicrosoftGraph();
			teamsBatchService.performSingleUserBatchProcessing(userPrincipalName);
			return new ResponseEntity<>("Batch Processing Successfull",HttpStatus.OK);
		}catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Error while batch processing", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}	
	
	
	@GetMapping("/events/{userPrincipalName}")
	public ResponseEntity<?> getUserEvents(@PathVariable String userPrincipalName){
		List<EventDto> eventslistDto = teamsBatchService.getEventByUserPrincipalName(userPrincipalName);
		return new ResponseEntity<>(eventslistDto,HttpStatus.OK);
	}
	
}
