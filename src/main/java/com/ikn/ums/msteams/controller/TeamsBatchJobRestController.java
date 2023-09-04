package com.ikn.ums.msteams.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ikn.ums.msteams.dto.BatchDetailsDto;
import com.ikn.ums.msteams.dto.EventDto;
import com.ikn.ums.msteams.entity.Event;
import com.ikn.ums.msteams.exception.UserPrincipalNotFoundException;
import com.ikn.ums.msteams.exception.UsersNotFoundException;
import com.ikn.ums.msteams.service.ITeamsBatchService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/teams")
@Slf4j
public class TeamsBatchJobRestController {

	@Autowired
	private ITeamsBatchService teamsBatchService;
	
	//@Autowired
	//private InitializeMicrosoftGraph microsoftGraph;

	/*
	@GetMapping(path = "/auth/token")
	public ResponseEntity<?> authenticateTeamsServer() {
		try {
			String accessToken = this.microsoftGraph.initializeMicrosoftGraph();
			return new ResponseEntity<>(accessToken, HttpStatus.ACCEPTED);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	*/

	@GetMapping(path="/batch-process")
	public ResponseEntity<?> meetingDataBatchProcessing() {
		try {
			BatchDetailsDto existingBatchProcess = teamsBatchService.getLatestBatchProcessingRecordDetails();
			log.info("Last batch processing details "+existingBatchProcess.toString());
			if(existingBatchProcess.getStatus().equalsIgnoreCase("RUNNING")) {
				log.info("An instance of batch process is already running");
				return new ResponseEntity<>("An instance of batch process is already running",HttpStatus.ALREADY_REPORTED);
			}else {
				//perform batch processing
				teamsBatchService.performBatchProcessing(existingBatchProcess);
				//log.info("An instance of batch process is already running");
				return new ResponseEntity<>("Batch processing successfull", HttpStatus.OK);
			}
		}catch (UsersNotFoundException e) {
			e.printStackTrace();
			log.info("Users not found for batch processing");
			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
		}
		catch (Exception e) {
			e.printStackTrace();
			log.info("Error while batch processing "+e.getStackTrace());
			return new ResponseEntity<>("Error while batch processing, Check server logs for full details",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	
	/**
	 * 
	 * @param username
	 * @return list of user organized events
	 */
	@GetMapping(path = "/events/{username}")
	public ResponseEntity<?> getUserEvents(@PathVariable String username) {
		try {
			List<Event> userEventsList = teamsBatchService.getEventByUserPrincipalName(username);
			return new ResponseEntity<>(userEventsList,HttpStatus.OK);
		}catch (UserPrincipalNotFoundException e1) {
			e1.printStackTrace();
			return new ResponseEntity<>(e1.getMessage(), HttpStatus.NOT_FOUND);
		}catch (Exception e2) {
			e2.printStackTrace();
			return new ResponseEntity<>(e2.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	@GetMapping(path = "/attendedevents/{username}")
	public ResponseEntity<?> getUserAttendedEvents(@PathVariable String username) {
		try {
			List<Event> userEventsList = teamsBatchService.getUserAttendedEvents(username);
			return new ResponseEntity<>(userEventsList,HttpStatus.OK);
		}catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	@GetMapping(path = "/events/count/{email}")
	public ResponseEntity<?> getUserOragnizedMeetingCount(@PathVariable String email){
		return new ResponseEntity<>(76, HttpStatus.OK);
	}

}
