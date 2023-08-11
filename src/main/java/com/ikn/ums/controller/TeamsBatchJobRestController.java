package com.ikn.ums.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ikn.ums.dto.BatchDetailsDto;
import com.ikn.ums.exception.UsersNotFoundException;
import com.ikn.ums.service.ITeamsBatchService;

@RestController
@RequestMapping("/api/teams")
//@Slf4j
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
			if(existingBatchProcess.getStatus() == "RUNNING") {
				//log.info("An instance of batch process is already running");
				return new ResponseEntity<>("An instance of batch process is already running",HttpStatus.OK);
			}else {
				//perform batch processing
				teamsBatchService.performBatchProcessing(existingBatchProcess);
				return new ResponseEntity<>("Batch processing successfull", HttpStatus.OK);
			}
		}catch (UsersNotFoundException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
		}
		catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>("Error while batch processing, Check server logs for full details",
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/*
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
	*/

}
