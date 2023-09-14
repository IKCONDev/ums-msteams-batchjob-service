package com.ikn.ums.msteams.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ikn.ums.msteams.VO.ActionsItemsVO;
import com.ikn.ums.msteams.dto.BatchDetailsDto;
import com.ikn.ums.msteams.entity.Attendee;
import com.ikn.ums.msteams.entity.Event;
import com.ikn.ums.msteams.exception.BusinessException;
import com.ikn.ums.msteams.exception.ControllerException;
import com.ikn.ums.msteams.exception.UserPrincipalNotFoundException;
import com.ikn.ums.msteams.service.EventService;
import com.ikn.ums.msteams.service.ITeamsBatchService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/teams")
@Slf4j
public class TeamsBatchJobRestController {

	@Autowired
	private ITeamsBatchService teamsBatchService;

	@Autowired
	private EventService eventService;
	// @Autowired
	// private InitializeMicrosoftGraph microsoftGraph;

	/*
	 * @GetMapping(path = "/auth/token") public ResponseEntity<?>
	 * authenticateTeamsServer() { try { String accessToken =
	 * this.microsoftGraph.initializeMicrosoftGraph(); return new
	 * ResponseEntity<>(accessToken, HttpStatus.ACCEPTED); } catch (Exception e) {
	 * e.printStackTrace(); return new ResponseEntity<>(e.getMessage(),
	 * HttpStatus.INTERNAL_SERVER_ERROR); } }
	 */

	/**
	 * The batch processing code, that communicates with microsoft teams server and
	 * fetches the raw data of events and its related online meeting along with the
	 * transcripts of the meeting
	 * 
	 * @return
	 */
	@GetMapping(path = "/batch-process")
	public ResponseEntity<?> meetingDataBatchProcessing() {
		try {
			BatchDetailsDto existingBatchProcess = teamsBatchService.getLatestBatchProcessingRecordDetails();
			log.info("Last batch processing details " + existingBatchProcess.toString());
			if (existingBatchProcess.getStatus().equalsIgnoreCase("RUNNING")) {
				log.info("An instance of batch process is already running");
				return new ResponseEntity<>("An instance of batch process is already running",
						HttpStatus.ALREADY_REPORTED);
			} else {
				// perform batch processing
				teamsBatchService.performBatchProcessing(existingBatchProcess);
				// log.info("An instance of batch process is already running");
				return new ResponseEntity<>("Batch processing successfull", HttpStatus.OK);
			}
		} catch (Exception e) {
			log.info("Exception while performing batch processing " + e.getStackTrace());
			ControllerException umsCE = new ControllerException("<code>", e.getStackTrace().toString());
			// e.printStackTrace();
			log.info("Error while batch processing " + umsCE);
			return new ResponseEntity<>(umsCE, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * get all organized events of a user based on username(email) or userId
	 * 
	 * @param username
	 * @return list of user organized events
	 */
	@GetMapping(path = "/events/organized/{username}")
	public ResponseEntity<?> getUserEvents(@PathVariable String username) {
		try {
			List<Event> userEventsList = eventService.getEventByUserPrincipalName(username);
			return new ResponseEntity<>(userEventsList, HttpStatus.OK);
		} catch (UserPrincipalNotFoundException e1) {
			e1.printStackTrace();
			return new ResponseEntity<>(e1.getMessage(), HttpStatus.NOT_FOUND);
		} catch (Exception e2) {
			e2.printStackTrace();
			return new ResponseEntity<>(e2.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	/**
	 * get all attended events of a user based on userId
	 * 
	 * @param userId
	 * @return
	 */
	@GetMapping(path = "/events/attended/{email}")
	public ResponseEntity<?> getUserAttendedEvents(@PathVariable String email) {
		try {
			List<Event> userEventsList = eventService.getUserAttendedEvents(email);
			System.out.println(userEventsList);
			return new ResponseEntity<>(userEventsList, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	/**
	 * get attended events count of the user
	 * 
	 * @param userId
	 * @return count of attended events
	 */
	@GetMapping(path = "/events/attended-count/{emailId}")
	public ResponseEntity<?> getUserAttendedEventCount(@PathVariable String emailId) {
		Integer count = eventService.getUserAttendedEventsCount(emailId);
		return new ResponseEntity<>(count, HttpStatus.OK);
	}

	/**
	 * get organized events count of the user
	 * 
	 * @param email
	 * @return count of organized events
	 */
	@GetMapping(path = "/events/count/{email}")
	public ResponseEntity<?> getUserOragnizedEventCount(@PathVariable String email) {
		Integer count = eventService.getUserOrganizedEventCount(email);
		return new ResponseEntity<>(count, HttpStatus.OK);
	}

	/**
	 * 
	 * @param eventId
	 * @return
	 */
	@GetMapping("/events/actionitems/{eventId}")
	public ResponseEntity<?> getActionItemsOfEvent(@PathVariable Integer eventId) {
		List<ActionsItemsVO> actionItemsList = eventService.getActionItemsOfEvent(eventId);
		return new ResponseEntity<>(actionItemsList, HttpStatus.OK);
	}

	/**
	 * 
	 * @return
	 */
	@GetMapping("/events/actionitems")
	public ResponseEntity<?> getActionItemsOfEvent() {
		List<ActionsItemsVO> actionItemsList = eventService.getActionItems();
		return new ResponseEntity<>(actionItemsList, HttpStatus.OK);
	}

	/**
	 * 
	 */
	@GetMapping("/events")
	public ResponseEntity<?> getAllEvents() {
		boolean isActionItemsGeneratedForEvent = false;
		List<Event> eventsList = eventService.getAllEvents(isActionItemsGeneratedForEvent);
		return new ResponseEntity<>(eventsList, HttpStatus.OK);
	}

	@GetMapping("/events/status/{eventIds}/{isGenerated}")
	public ResponseEntity<?> updateActionItemGeneratedStatus(@PathVariable String eventIds,
			@PathVariable boolean isGenerated) {
		System.out.println("TeamsBatchJobRestController.updateActionItemGeneratedStatus() Entered " + eventIds + " "
				+ isGenerated);
		try {
//			List<Integer> actualEventids = null;
//			if (eventIds.contains(",")) {
//				String[] actualEventIds = eventIds.split(",");
//				List<String> idsList = Arrays.asList(actualEventIds);
//
//				// convert string of ids to Integer ids
//				actualEventids = idsList.stream().map(s -> Integer.parseInt(s)).collect(Collectors.toList());
//			} else {
//				List<String> idsList = Arrays.asList(eventIds);
//
//				// convert string of ids to Integer ids
//				actualEventids = idsList.stream().map(s -> Integer.parseInt(s)).collect(Collectors.toList());
//			}

			System.out.println("TeamsBatchJobRestController.updateActionItemGeneratedStatus() under execution");
			// update the status of events
			Integer status = eventService.updateActionItemStatusOfEvent(isGenerated, Arrays.asList(5));
			System.out.println("TeamsBatchJobRestController.updateActionItemGeneratedStatus() Going to Exit");
			return new ResponseEntity<>(status, HttpStatus.OK);

		} catch (Exception e) {
			ControllerException umsCE = new ControllerException("<errorcode>",
					"Error occured in controller " + e.getStackTrace().toString());
			return new ResponseEntity<>(umsCE, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
