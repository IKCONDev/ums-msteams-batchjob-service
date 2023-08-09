package com.ikn.ums.controller;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ikn.ums.dto.EventDto;
import com.ikn.ums.exception.UserPrincipalNotFoundException;
import com.ikn.ums.service.ITeamsBatchService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/teams")
@Slf4j
public class TeamsBatchJobRestController {

	@Autowired
	private ITeamsBatchService teamsBatchService;

	
	@GetMapping(path = "/auth/token")
	public ResponseEntity<?> authenticateTeamsServer() {
		try {
			String accessToken = teamsBatchService.initializeMicrosoftGraph();
			return new ResponseEntity<>(accessToken, HttpStatus.ACCEPTED);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	

	@GetMapping(path="/batch-process")
	public ResponseEntity<?> meetingDataBatchProcessing() {
		try {
			//perform batch processing
			teamsBatchService.performBatchProcessing();
			return new ResponseEntity<>("Batch processing successfull", HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Error while batch processing, Check server logs for full details",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping(path = "/batch-user/{userPrincipalName}")
	public ResponseEntity<?> meetingUserDataBatchProcessing(@PathVariable String userPrincipalName) {
		try {
			teamsBatchService.performSingleUserBatchProcessing(userPrincipalName);
			return new ResponseEntity<>("Batch processing successfull for user "+userPrincipalName, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Error while batch processing for user "+userPrincipalName+", Check server logs for full details",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping(path = "/events/{userPrincipalName}")
	public ResponseEntity<?> getUserEvents(@PathVariable String userPrincipalName) {
		try {
			List<EventDto> eventslistDto = teamsBatchService.getEventByUserPrincipalName(userPrincipalName);
			return new ResponseEntity<>(eventslistDto, HttpStatus.OK);
		}catch (UserPrincipalNotFoundException e1) {
			e1.printStackTrace();
			return new ResponseEntity<>(e1.getMessage(), HttpStatus.NOT_FOUND);
		}catch (Exception e2) {
			e2.printStackTrace();
			return new ResponseEntity<>(e2.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}

}
