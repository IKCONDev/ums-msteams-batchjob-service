package com.ikn.ums.msteams.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.azure.core.credential.AccessToken;
import com.ikn.ums.msteams.VO.ActionsItemsVO;
import com.ikn.ums.msteams.dto.BatchDetailsDto;
import com.ikn.ums.msteams.entity.BatchDetails;
import com.ikn.ums.msteams.entity.CronDetails;
import com.ikn.ums.msteams.entity.Event;
import com.ikn.ums.msteams.exception.BusinessException;
import com.ikn.ums.msteams.exception.ControllerException;
import com.ikn.ums.msteams.exception.EmptyInputException;
import com.ikn.ums.msteams.exception.ErrorCodeMessages;
import com.ikn.ums.msteams.exception.InvalidInputException;
import com.ikn.ums.msteams.exception.TranscriptGenerationFailedException;
import com.ikn.ums.msteams.exception.UsersNotFoundException;
import com.ikn.ums.msteams.service.EventService;
import com.ikn.ums.msteams.service.TeamsSourceDataBatchProcessService;
import com.ikn.ums.msteams.utils.InitializeMicrosoftGraph;
import com.netflix.servo.util.Strings;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/teams")
@Slf4j
public class TeamsSourceDataBatchProcessController {

	@Autowired
	private TeamsSourceDataBatchProcessService teamsSourceDataBatchProcessService;

	@Autowired
	private EventService eventService;
	
	@Autowired
	private InitializeMicrosoftGraph microsoftGraph;

	/**
	 * Test method to check the token retrieval from microsoft azure identity server
	 * @return
	 */
	  @GetMapping(path = "/auth/token") 
	  public ResponseEntity<String> authenticateTeamsServer() { 
		  log.info("authenticateTeamsServer() entered with no args");
		try { 
			AccessToken accessToken = this.microsoftGraph.initializeMicrosoftGraph(); 
			String actualToken = accessToken.getToken();
			log.info("authenticateTeamsServer() executed succesfully.");
			return new ResponseEntity<>(actualToken, HttpStatus.ACCEPTED); 
		} catch (Exception e) {
			log.error("authenticateTeamsServer() : An error occurred while retrieving token: {}." + e.getMessage(), e);
	        return new ResponseEntity<>(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR); 
	    } 
	  }
	 

	/**
	 * The batch processing code, that communicates with microsoft teams server and
	 * fetches the raw data of events and its related online meeting along with the
	 * transcripts of the meeting
	 * 
	 * @return
	 */
	@GetMapping(path = "/batch-process")
	public ResponseEntity<String> rawDataBatchProcessing() {
		log.info("TeamsSourceDataBatchProcessController.rawDataBatchProcessing() Entered with no args");
		try {
			BatchDetailsDto existingBatchProcess = teamsSourceDataBatchProcessService
					.getLatestSourceDataBatchProcessingRecordDetails();
			log.info("Last batch processing details " + existingBatchProcess.toString());
			if (existingBatchProcess.getStatus().equalsIgnoreCase("RUNNING")) {
				log.info("An instance of batch process is already running...");
				String message = "An instance of raw data batch process is already running";
				return new ResponseEntity<>(message,
						HttpStatus.PROCESSING);
			} else {
				log.info(
						"TeamsSourceDataBatchProcessController.rawDataBatchProcessing() is started its processing raw data...");
				// perform batch processing
				teamsSourceDataBatchProcessService.performSourceDataBatchProcessing(existingBatchProcess);
				log.info("TeamsSourceDataBatchProcessController.rawDataBatchProcessing() executed sucessfully");
				return new ResponseEntity<>(ErrorCodeMessages.ERR_MSTEAMS_BATCH_PROCESS_SUCCESS_MSG, HttpStatus.OK);
			}
		} catch (BusinessException | UsersNotFoundException | TranscriptGenerationFailedException e) {
			log.error("rawDataBatchProcessing() : An Business error/exception occurred: {}." + e.getMessage(), e);
			throw e;
		}
		catch (Exception e) {
			log.error("rawDataBatchProcessing() : An error/exception occurred: {}." + e.getMessage(), e);
			throw new ControllerException(ErrorCodeMessages.MSTEAMS_BATCHPROCESS_UNSUCCESS_CODE,
					ErrorCodeMessages.MSTEAMS_BATCHPROCESS_UNSUCCESS_MSG);
		}
	}

	
	/**
	 * 
	 * @param eventId
	 * @return
	 */
	@GetMapping("/events/actionitems/{eventId}")
	public ResponseEntity<List<ActionsItemsVO>> getActionItemsOfEvent(@PathVariable Integer eventId) {
		log.info("TeamsSourceDataBatchProcessController.getActionItemsOfEvent() entered with args : eventId : "
				+ eventId);
		if (eventId <= 0) {
			log.info(
					"TeamsSourceDataBatchProcessController.getActionItemsOfEvent() is exited with exception : Invalid event id : "
							+ eventId);
			throw new InvalidInputException(ErrorCodeMessages.ERR_EVENT_INVALID_EVENTID_CODE,
					ErrorCodeMessages.ERR_EVENT_INVALID_EVENTID_MSG);
		}
		try {
			log.info("TeamsSourceDataBatchProcessController.getActionItemsOfEvent() is under execution");
			List<ActionsItemsVO> actionItemsList = eventService.getActionItemsOfEvent(eventId);
			log.info("TeamsSourceDataBatchProcessController.getActionItemsOfEvent() exited sucessfully");
			return new ResponseEntity<>(actionItemsList, HttpStatus.OK);
		} catch (Exception e) {
			log.error("getActionItemsOfEvent() : An error/exception occurred: {}." + e.getMessage(), e);
			throw new ControllerException(ErrorCodeMessages.ERR_EVENT_ACTIONITEMS_GET_UNSUCCESS_CODE,
					ErrorCodeMessages.ERR_EVENT_ACTIONITEMS_GET_UNSUCCESS_MSG);
		}

	}

	/**
	 * 
	 * @return
	 */
	@GetMapping("/events/actionitems")
	public ResponseEntity<List<ActionsItemsVO>> getActionItemsOfAllEvents() {
		log.info("TeamsSourceDataBatchProcessController.getActionItemsOfAllEvents() entered");
		try {
			log.info("TeamsSourceDataBatchProcessController.getActionItemsOfAllEvents() is under execution");
			List<ActionsItemsVO> actionItemsList = eventService.getActionItems();
			log.info("TeamsSourceDataBatchProcessController.getActionItemsOfAllEvents() exiting succesfully");
			return new ResponseEntity<>(actionItemsList, HttpStatus.OK);
		} catch (Exception e) {
			log.error("getActionItemsOfAllEvents() : An error/exception occurred: {}." + e.getMessage(), e);
			throw new ControllerException(ErrorCodeMessages.ERR_EVENT_ACTIONITEMS_GET_UNSUCCESS_CODE,
					ErrorCodeMessages.ERR_EVENT_ACTIONITEMS_GET_UNSUCCESS_MSG);
		}
	}

	/**
	 * 
	 */
	@GetMapping("/events/{userEmailId}")
	public ResponseEntity<List<Event>> getAllEvents(@PathVariable String userEmailId) {
		log.info("TeamsSourceDataBatchProcessController.getAllEvents() entered with args : userEmailId : "
				+ userEmailId);
		if (Strings.isNullOrEmpty(userEmailId) || userEmailId.isEmpty()) {
			log.info(
					"TeamsSourceDataBatchProcessController.getAllEvents() exited with exception : userEmailid is empty or null");
			throw new EmptyInputException(ErrorCodeMessages.ERR_EVENT_EMAIL_ID_EMPTY_CODE,
					ErrorCodeMessages.ERR_EVENT_EMAIL_ID_EMPTY_MSG);
		}
		log.info("TeamsSourceDataBatchProcessController.getAllEvents() is under execution");
		boolean isActionItemsGeneratedForEvent = false;
		try {
			List<Event> eventsList = eventService.getAllEvents(userEmailId, isActionItemsGeneratedForEvent);
			log.error("TeamsSourceDataBatchProcessController.getAllEvents() is exited sucessfully");
			return new ResponseEntity<>(eventsList, HttpStatus.OK);
		} catch (Exception e) {
			log.error(
					"TeamsSourceDataBatchProcessController.getAllEvents() exited with exception : exception occurred while fetching events of user "
							+ e.getMessage(), e);
			throw new ControllerException(ErrorCodeMessages.ERR_EVENTS_GET_ALL_UNSUCCESS_CODE,
					ErrorCodeMessages.ERR_EVENTS_GET_ALL_UNSUCCESS_MSG);
		}
	}

	@GetMapping("/events/status/{eventIds}/{isActionItemGenerated}")
	public ResponseEntity<Integer> updateActionItemGeneratedStatus(@PathVariable String eventIds,
			@PathVariable boolean isActionItemGenerated) {
		log.info("TeamsSourceDataBatchProcessController.updateActionItemGeneratedStatus() Entered with args : eventIds"
				+ eventIds + ", isActionItemsGenerated " + isActionItemGenerated);
		if(Strings.isNullOrEmpty(eventIds) || eventIds.isEmpty()) {
			throw new EmptyInputException(ErrorCodeMessages.ERR_MSTEAMS_EVENTID_EMPTY_CODE, 
					ErrorCodeMessages.ERR_MSTEAMS_EVENTID_EMPTY_MSG);
		}
		try {
			log.info("TeamsSourceDataBatchProcessController.updateActionItemGeneratedStatus() under execution");
			// update the status of events
			List<Integer> actualEventIds = new ArrayList<>();
			String newEventIds = eventIds.replace("[", "");
			String orginalEventIds = newEventIds.replace("]", "");
			if (orginalEventIds.contains(",")) {
				String[] eventids = orginalEventIds.split(",");
				List<String> eventidsString = Arrays.asList(eventids);
				eventidsString.forEach(eventId -> {
					actualEventIds.add(Integer.parseInt(eventId.trim()));
				});
				System.out.println(actualEventIds);
			} else {
				List<String> eventIdsList = List.of(orginalEventIds);
				eventIdsList.forEach(id -> {
					actualEventIds.add(Integer.parseInt(id));
				});
			}
			Integer status = eventService.updateActionItemStatusOfEvent(isActionItemGenerated, actualEventIds);
			log.info("updateActionItemGeneratedStatus() exiting sucessfully");
			return new ResponseEntity<>(status, HttpStatus.OK);

		} catch (Exception e) {
			log.error(
					"updateActionItemGeneratedStatus() Exception occured while updating action items generation status for events  "
							,e);
			throw new ControllerException(ErrorCodeMessages.ERR_UNKNOWN_BATCH_CODE,
					ErrorCodeMessages.ERR_UNKNOWN_BATCH_MSG);
		}
	}
	
	@GetMapping(path="/batch-details")
	public ResponseEntity<List<BatchDetails>> getBatchProcessDetails(){
		log.info("getBatchProcessDetails() entered with no args");
		try {
			List<BatchDetails> batchDetails = teamsSourceDataBatchProcessService.getBatchProcessDetails();
			log.info("getBatchProcessDetails() executed successfully.");
			return new ResponseEntity<>(batchDetails,HttpStatus.OK);
		}catch (Exception e) {
			log.error("getBatchProcessDetails() General Exception occured while updating batch process time : "+e.getMessage(), e);
			throw new ControllerException(ErrorCodeMessages.ERR_MSTEAMS_BATCH_PROCESS_GET_UNSUCCESS_CODE, 
					ErrorCodeMessages.ERR_MSTEAMS_BATCH_PROCESS_GET_UNSUCCESS_MSG);
		}
	}
	
	@PutMapping(path="/crontime")
	public ResponseEntity<CronDetails> updateBatchProcessTime(@RequestBody CronDetails cronDetails){
		log.info("updateBatchProcessTime() entered with no args");
		if(cronDetails ==  null) {
			throw new EmptyInputException(ErrorCodeMessages.ERR_MSTEAMS_BATCH_PROCESS_CRONTIME_EMPTY_CODE, 
					ErrorCodeMessages.ERR_MSTEAMS_BATCH_PROCESS_CRONTIME_EMPTY_MSG);
		}
		try {
			CronDetails updatedCronDetails = teamsSourceDataBatchProcessService.updateBatchProcessTime(cronDetails);
			log.info("updateBatchProcessTime() executed successfully.");
			return new ResponseEntity<>(updatedCronDetails,HttpStatus.PARTIAL_CONTENT);
		}catch (EmptyInputException businessException) {
			log.error("getBatchProcessDetails() Business Exception occured while updating batch process time : "+businessException.getMessage(), businessException);
			throw businessException;
		}
		catch (Exception e) {
			log.error("getBatchProcessDetails() General Exception occured while updating batch process time : "+e.getMessage(), e);
			throw new ControllerException(ErrorCodeMessages.ERR_MSTEAMS_BATCH_PROCESS_CRONTIME_UPADTE_UNSUCCESS_CODE, 
					ErrorCodeMessages.ERR_MSTEAMS_BATCH_PROCESS_CRONTIME_UPADTE_UNSUCCESS_MSG);
		}
	}
	
	@GetMapping(path="/crontime")
	public ResponseEntity<CronDetails> getBatchProcessTime(){
		log.info("getBatchProcessDetails() entered with no args");
		try {
			log.info("getBatchProcessDetails() is under execution...");
			CronDetails cronDetails = teamsSourceDataBatchProcessService.getCronDetails();
			log.info("getBatchProcessDetails() executed successfully.");
			return new ResponseEntity<>(cronDetails,HttpStatus.OK);
		}catch (EmptyInputException businessException) {
			log.error("getBatchProcessDetails() Business Exception occured while fetching batch process time : "+businessException.getMessage(), businessException);
			throw businessException;
		}
		catch (Exception e) {
			log.error("getBatchProcessDetails() General Exception occured while fetching batch process time : "+e.getMessage(), e);
			throw new ControllerException(ErrorCodeMessages.ERR_MSTEAMS_BATCH_PROCESS_CRONTIME_GET_UNSUCCESS_CODE, 
					ErrorCodeMessages.ERR_MSTEAMS_BATCH_PROCESS_CRONTIME_GET_UNSUCCESS_MSG);
		}
	}

}
