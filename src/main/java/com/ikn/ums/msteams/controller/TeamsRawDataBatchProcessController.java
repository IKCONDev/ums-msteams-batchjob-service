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
import com.ikn.ums.msteams.exception.EmptyInputException;
import com.ikn.ums.msteams.exception.ErrorCodeMessages;
import com.ikn.ums.msteams.exception.InvalidInputException;
import com.ikn.ums.msteams.exception.UserPrincipalNotFoundException;
import com.ikn.ums.msteams.service.EventService;
import com.ikn.ums.msteams.service.TeamsRawDataBatchProcessService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/teams")
@Slf4j
public class TeamsRawDataBatchProcessController {

	@Autowired
	private TeamsRawDataBatchProcessService teamsRawDataBatchProcessService;

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
	public ResponseEntity<?> rawDataBatchProcessing() {
		log.info("TeamsRawDataBatchProcessController.rawDataBatchProcessing() Entered with no args");
		try {
			BatchDetailsDto existingBatchProcess = teamsRawDataBatchProcessService
					.getLatestRawDataBatchProcessingRecordDetails();
			log.info("Last batch processing details " + existingBatchProcess.toString());
			if (existingBatchProcess.getStatus().equalsIgnoreCase("RUNNING")) {
				log.info("An instance of batch process is already running...");
				return new ResponseEntity<>("An instance of raw data batch process is already running",
						HttpStatus.PROCESSING);
			} else {
				log.info("TeamsRawDataBatchProcessController.rawDataBatchProcessing() is started its processing raw data...");
				// perform batch processing
				teamsRawDataBatchProcessService.performRawDataBatchProcessing(existingBatchProcess);
				log.info("TeamsRawDataBatchProcessController.rawDataBatchProcessing() executed sucessfully");
				return new ResponseEntity<>(ErrorCodeMessages.MSTEAMS_BATCH_PROCESS_SUCCESS_MSG, HttpStatus.OK);
			}
		} catch (Exception e) {
			log.info(
					"TeamsRawDataBatchProcessController.rawDataBatchProcessing() : Exception occured while executing raw data batch process : "
							+ e.getMessage());
			throw new ControllerException(ErrorCodeMessages.MSTEAMS_BATCHPROCESS_UNSUCCESS_CODE,
					ErrorCodeMessages.MSTEAMS_BATCHPROCESS_UNSUCCESS_MSG);
		}
	}

	/**
	 * get all organized events of a user based on userId(email)
	 * 
	 * @param username
	 * @return list of user organized events
	 */
	@GetMapping(path = "/events/organized/{userEmailId}")
	public ResponseEntity<?> getUserEvents(@PathVariable String userEmailId) {
		log.info("TeamsRawDataBatchProcessController.getUserEvents() entered with args : userEmailId : " + userEmailId);
		if (userEmailId.equals("")) {
			log.info("TeamsRawDataBatchProcessController.getUserEvents() : useremailId is empty");
			throw new EmptyInputException(ErrorCodeMessages.ERR_EVENT_EMAIL_ID_EMPTY_CODE,
					ErrorCodeMessages.ERR_EVENT_EMAIL_ID_EMPTY_MSG);
		}
		try {
			List<Event> userEventsList = eventService.getEventByUserPrincipalName(userEmailId);
			log.info("TeamsRawDataBatchProcessController.getUserEvents() exited sucessfully");
			return new ResponseEntity<>(userEventsList, HttpStatus.OK);

		} catch (Exception e) {
			log.info(
					"TeamsRawDataBatchProcessController.getUserEvents() exited with exception : Exception occured while getting user organized events "
							+ e.fillInStackTrace());
			throw new ControllerException(ErrorCodeMessages.ERR_EVENT_GET_UNSUCCESS_CODE,
					ErrorCodeMessages.ERR_EVENT_GET_UNSUCCESS_MSG);
		}

	}

	@GetMapping(path = "/events/attended/{userEmailId}")
	public ResponseEntity<?> getUserAttendedEvents(@PathVariable String userEmailId) {
		log.info(
				"TeamsRawDataBatchProcessController.getUserAttendedEvents() entered with args : userEmailId : " + userEmailId);
		if (userEmailId.equals("")) {
			throw new EmptyInputException(ErrorCodeMessages.ERR_EVENT_EMAIL_ID_EMPTY_CODE,
					ErrorCodeMessages.ERR_EVENT_EMAIL_ID_EMPTY_MSG);
		}
		try {
			log.info("TeamsRawDataBatchProcessController.getUserAttendedEvents() is under execution");
			List<Event> userEventsList = eventService.getUserAttendedEvents(userEmailId);
			log.info("TeamsRawDataBatchProcessController.getUserAttendedEvents() exiting sucessfully");
			return new ResponseEntity<>(userEventsList, HttpStatus.OK);
		} catch (Exception e) {
			log.info(
					"TeamsRawDataBatchProcessController.getUserAttendedEvents() : Exception occured while getting user attended events "
							+ e.fillInStackTrace());
			throw new ControllerException(ErrorCodeMessages.ERR_EVENT_GET_UNSUCCESS_CODE,
					ErrorCodeMessages.ERR_EVENT_GET_UNSUCCESS_MSG);
		}

	}

	/**
	 * get attended events count of the user
	 * 
	 * @param userId
	 * @return count of attended events
	 */
	@GetMapping(path = "/events/attended-count/{userEmailId}")
	public ResponseEntity<?> getUserAttendedEventCount(@PathVariable String userEmailId) {
		log.info("TeamsRawDataBatchProcessController.getUserAttendedEventCount() entered with args : " + userEmailId);
		if (userEmailId.equalsIgnoreCase("") || userEmailId == null) {
			log.info("TeamsRawDataBatchProcessController.getUserAttendedEventCount() userEmailId is empty or null");
			throw new EmptyInputException(ErrorCodeMessages.ERR_EVENT_EMAIL_ID_EMPTY_CODE,
					ErrorCodeMessages.ERR_EVENT_EMAIL_ID_EMPTY_MSG);
		}
		try {
			log.info("TeamsRawDataBatchProcessController.getUserAttendedEventCount() is under execution ");
			Integer count = eventService.getUserAttendedEventsCount(userEmailId);
			log.info("TeamsRawDataBatchProcessController.getUserAttendedEventCount() exited sucessfully by retruning count : "
					+ count);
			return new ResponseEntity<>(count, HttpStatus.OK);
		} catch (Exception e) {
			log.info(
					"TeamsRawDataBatchProcessController.getUserAttendedEventCount() exited with exception : Exception occured while getting user attended evebts count "
							+ e.fillInStackTrace());
			throw new ControllerException(ErrorCodeMessages.ERR_EVENT_GET_ATT_COUNT_UNSUCCESS_CODE,
					ErrorCodeMessages.ERR_EVENT_GET_ATT_COUNT_UNSUCCESS_MSG);
		}
	}

	/**
	 * get organized events count of the user
	 * 
	 * @param email
	 * @return count of organized events
	 */
	@GetMapping(path = "/events/count/{userEmailId}")
	public ResponseEntity<?> getUserOragnizedEventCount(@PathVariable String userEmailId) {
		log.info("TeamsRawDataBatchProcessController.getUserOragnizedEventCount() entered with args : userEmailId : "
				+ userEmailId);
		if (userEmailId.equalsIgnoreCase("") || userEmailId == null) {
			log.info(
					"TeamsRawDataBatchProcessController.getUserOragnizedEventCount() exited with exception : userEmailid is empty or null");
			throw new EmptyInputException(ErrorCodeMessages.ERR_EVENT_EMAIL_ID_EMPTY_CODE,
					ErrorCodeMessages.ERR_EVENT_EMAIL_ID_EMPTY_MSG);
		}
		try {
			log.info("TeamsRawDataBatchProcessController.getUserOragnizedEventCount() is under execution");
			Integer count = eventService.getUserOrganizedEventCount(userEmailId);
			log.info(
					"TeamsRawDataBatchProcessController.getUserOragnizedEventCount() exited succesfully by returning organizedEventsCount : "
							+ count);
			return new ResponseEntity<>(count, HttpStatus.OK);
		} catch (Exception e) {
			log.info(
					"TeamsRawDataBatchProcessController.getUserOragnizedEventCount() exited with exeception : Exception occured while getting organizedEventsCount "
							+ e.fillInStackTrace());
			throw new ControllerException(ErrorCodeMessages.ERR_EVENT_GET_ORG_COUNT_UNSUCCESS_CODE,
					ErrorCodeMessages.ERR_EVENT_GET_ORG_COUNT_UNSUCCESS_MSG);
		}
	}

	/**
	 * 
	 * @param eventId
	 * @return
	 */
	@GetMapping("/events/actionitems/{eventId}")
	public ResponseEntity<?> getActionItemsOfEvent(@PathVariable Integer eventId) {
		log.info("TeamsRawDataBatchProcessController.getActionItemsOfEvent() entered with args : eventId : " + eventId);
		if (eventId < 1) {
			log.info(
					"TeamsRawDataBatchProcessController.getActionItemsOfEvent() is exited with exception : Invalid event id : "
							+ eventId);
			throw new InvalidInputException(ErrorCodeMessages.ERR_EVENT_INVALID_EVENTID_CODE,
					ErrorCodeMessages.ERR_EVENT_INVALID_EVENTID_MSG);
		}
		try {
			log.info("TeamsRawDataBatchProcessController.getActionItemsOfEvent() is under execution");
			List<ActionsItemsVO> actionItemsList = eventService.getActionItemsOfEvent(eventId);
			log.info("TeamsRawDataBatchProcessController.getActionItemsOfEvent() exited sucessfully");
			return new ResponseEntity<>(actionItemsList, HttpStatus.OK);
		} catch (Exception e) {
			log.info(
					"TeamsRawDataBatchProcessController.getActionItemsOfEvent() exited with exception : exception occured while getting action items of an event "
							+ e.fillInStackTrace());
			throw new ControllerException(ErrorCodeMessages.ERR_EVENT_ACTIONITEMS_GET_UNSUCCESS_CODE,
					ErrorCodeMessages.ERR_EVENT_ACTIONITEMS_GET_UNSUCCESS_MSG);
		}

	}

	/**
	 * 
	 * @return
	 */
	@GetMapping("/events/actionitems")
	public ResponseEntity<?> getActionItemsOfAllEvents() {
		log.info("TeamsRawDataBatchProcessController.getActionItemsOfAllEvents() entered");
		try {
			log.info("TeamsRawDataBatchProcessController.getActionItemsOfAllEvents() is under execution");
			List<ActionsItemsVO> actionItemsList = eventService.getActionItems();
			log.info("TeamsRawDataBatchProcessController.getActionItemsOfAllEvents() exiting succesfully");
			return new ResponseEntity<>(actionItemsList, HttpStatus.OK);
		} catch (Exception e) {
			log.info(
					"TeamsRawDataBatchProcessController.getActionItemsOfAllEvents() exiting with exception : Exception occurred while fetching action items of all events");
			throw new ControllerException(ErrorCodeMessages.ERR_EVENT_ACTIONITEMS_GET_UNSUCCESS_CODE,
					ErrorCodeMessages.ERR_EVENT_ACTIONITEMS_GET_UNSUCCESS_MSG);
		}
	}

	/**
	 * 
	 */
	@GetMapping("/events/{userEmailId}")
	public ResponseEntity<?> getAllEvents(@PathVariable String userEmailId) {
		log.info("TeamsRawDataBatchProcessController.getAllEvents() entered with args : userEmailId : " + userEmailId);
		if (userEmailId.equalsIgnoreCase("") || userEmailId == null) {
			log.info("TeamsRawDataBatchProcessController.getAllEvents() exited with exception : userEmailid is empty or null");
			throw new EmptyInputException(ErrorCodeMessages.ERR_EVENT_EMAIL_ID_EMPTY_CODE,
					ErrorCodeMessages.ERR_EVENT_EMAIL_ID_EMPTY_MSG);
		}
		log.info("TeamsRawDataBatchProcessController.getAllEvents() is under execution");
		boolean isActionItemsGeneratedForEvent = false;
		try {
			List<Event> eventsList = eventService.getAllEvents(userEmailId, isActionItemsGeneratedForEvent);
			log.info("TeamsRawDataBatchProcessController.getAllEvents() is exited sucessfully");
			return new ResponseEntity<>(eventsList, HttpStatus.OK);
		} catch (Exception e) {
			log.info(
					"TeamsRawDataBatchProcessController.getAllEvents() exited with exception : exception occurred while fetching events of user "
							+ e.fillInStackTrace());
			throw new ControllerException(ErrorCodeMessages.ERR_EVENTS_GET_ALL_UNSUCCESS_CODE,
					ErrorCodeMessages.ERR_EVENTS_GET_ALL_UNSUCCESS_MSG);
		}
	}

	@GetMapping("/events/status/{eventIds}/{isActionItemGenerated}")
	public ResponseEntity<?> updateActionItemGeneratedStatus(@PathVariable String eventIds,
			@PathVariable boolean isActionItemGenerated) {
		log.info("TeamsRawDataBatchProcessController.updateActionItemGeneratedStatus() Entered with args : eventIds" + eventIds
				+ ", isActionItemsGenerated " + isActionItemGenerated);
		try {
			log.info("TeamsRawDataBatchProcessController.updateActionItemGeneratedStatus() under execution");
			// update the status of events
			List<Integer> actualEventIds = new ArrayList<>();
			String newEventIds = eventIds.replace("[", "");
			String orginalEventIds = newEventIds.replace("]", "");
			// List<String> eventIdsList = List.of(orginalEventIds);
			System.out.println(orginalEventIds);
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
					System.out.println(id);
					actualEventIds.add(Integer.parseInt(id));
				});
				System.out.println(actualEventIds);
			}
			Integer status = eventService.updateActionItemStatusOfEvent(isActionItemGenerated, actualEventIds);
			log.info("TeamsRawDataBatchProcessController.updateActionItemGeneratedStatus() exiting sucessfully");
			return new ResponseEntity<>(status, HttpStatus.OK);

		} catch (Exception e) {
			log.info(
					"TeamsRawDataBatchProcessController.updateActionItemGeneratedStatus() exiting with exception : Exception occured while updating action items generation status for events  "
							+ e.fillInStackTrace());
			ControllerException umsCE = new ControllerException(ErrorCodeMessages.ERR_UNKNOWN_BATCH_CODE,
					ErrorCodeMessages.ERR_UNKNOWN_BATCH_MSG + " " + e.getStackTrace().toString());
			return new ResponseEntity<>(umsCE, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

}
