package com.ikn.ums.msteams.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.azure.core.credential.AccessToken;
import com.ikn.ums.msteams.VO.EmployeeListVO;
import com.ikn.ums.msteams.VO.EmployeeVO;
import com.ikn.ums.msteams.dto.AttendanceReportDto;
import com.ikn.ums.msteams.dto.BatchDetailsDto;
import com.ikn.ums.msteams.dto.EventDto;
import com.ikn.ums.msteams.dto.OnlineMeetingDto;
import com.ikn.ums.msteams.dto.TranscriptDto;
import com.ikn.ums.msteams.entity.AttendanceReport;
import com.ikn.ums.msteams.entity.Attendee;
import com.ikn.ums.msteams.entity.BatchDetails;
import com.ikn.ums.msteams.entity.CronDetails;
import com.ikn.ums.msteams.entity.Event;
import com.ikn.ums.msteams.entity.Transcript;
import com.ikn.ums.msteams.exception.EmptyInputException;
import com.ikn.ums.msteams.exception.ErrorCodeMessages;
import com.ikn.ums.msteams.exception.TranscriptGenerationFailedException;
import com.ikn.ums.msteams.exception.UsersNotFoundException;
import com.ikn.ums.msteams.model.AttendanceReportResponseWrapper;
import com.ikn.ums.msteams.model.CalendarViewResponseWrapper;
import com.ikn.ums.msteams.model.OnlineMeetingResponseWrapper;
import com.ikn.ums.msteams.model.TranscriptsResponseWrapper;
import com.ikn.ums.msteams.repo.BatchDetailsRepository;
import com.ikn.ums.msteams.repo.CronRepository;
import com.ikn.ums.msteams.repo.EventRepository;
import com.ikn.ums.msteams.service.TeamsSourceDataBatchProcessService;
import com.ikn.ums.msteams.utils.EmailService;
import com.ikn.ums.msteams.utils.InitializeMicrosoftGraph;
import com.ikn.ums.msteams.utils.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TeamsSourceDataBatchProcessServiceImpl implements TeamsSourceDataBatchProcessService {

	@Autowired
	private Environment environment;

	private String accessToken = null;

	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private BatchDetailsRepository batchDetailsRepository;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private InitializeMicrosoftGraph microsoftGraph;
	
	@Autowired
	private CronRepository cronRepository;

	private LocalDateTime lastBatchProcessingStartTime;

	private AccessToken acToken = new AccessToken(this.accessToken, OffsetDateTime.now());

	private List<EmployeeVO> userDtoList = null;
	
	private RestTemplate graphRestTemplate;
	
	private static final String authHeader = "Authorization";
	
	private static final String tokenType = " Bearer ";
	
	private static final String contentHeader = "content-type";
	
	private static final String jsonContentType = "application/json";
	
	private static String partialTranscriptFileName = "Transcript";
	
	@Autowired
	private EmailService emailService;
	
	// constructor
	@Autowired
	public TeamsSourceDataBatchProcessServiceImpl() {
		graphRestTemplate = new RestTemplate();
		log.info("TeamsSourceDataBatchProcessServiceImpl() constructor executed.");
	}

	@Transactional
	@Override
	public void performSourceDataBatchProcessing(BatchDetailsDto lastBatchDetails) throws Exception {
		log.info("performSourceDataBatchProcessing() entered with args : LastBatchProcessDetails");
		List<List<Event>> allUsersEventListOfCurrentBatchProcess = new ArrayList<>();
		// get access token from MS teams server , only if existing access token is expired
		if (this.acToken.isExpired()) {
			log.info("performSourceDataBatchProcessing() Access Token expired.");
			this.acToken = this.microsoftGraph.initializeMicrosoftGraph();
			log.info("performSourceDataBatchProcessing() Microsoft graph api initialized.");
			this.accessToken = this.acToken.getToken();
			log.info("performSourceDataBatchProcessing() New Access Token aquired.");
		}
		// get all employees data from employee microservice to perform batch processing
		var empList = restTemplate.getForObject("http://UMS-EMPLOYEE-SERVICE/employees/get-all",
				EmployeeListVO.class);
		log.info("performSourceDataBatchProcessing() Call to employee microservice successfull.");
		userDtoList = empList.getEmployee();
//		List<EmployeeVO> activeEmployeesList = userDtoList.stream()
//				.filter(employee -> employee.getEmployeeStatus().equalsIgnoreCase("Active"))
//				.collect(Collectors.toList());
		log.info("performSourceDataBatchProcessing() Employees details fecthed from employee microservice.");
		if (!userDtoList.isEmpty()) {
			log.info("performSourceDataBatchProcessing() Employee list size : " + userDtoList.size());
			// start batch process
			// set current batch processing details
			BatchDetailsDto currentBatchDetailsDto = new BatchDetailsDto();
			BatchDetails batchDetails = new BatchDetails();
			// get current time which is taken as batch processing start date time for
			// current batch process
			LocalDateTime currentbatchStartTime = LocalDateTime.now();
			log.info("performSourceDataBatchProcessing() current batch process start time : " + currentbatchStartTime);
			// save current status of batch process in DB, and this will be latest record in
			// DB
			currentBatchDetailsDto.setStatus("RUNNING");
			log.info("performSourceDataBatchProcessing() current batch process status : "
					+ currentBatchDetailsDto.getStatus());
			currentBatchDetailsDto.setStartDateTime(currentbatchStartTime);
			// map dto to entity
			ObjectMapper.modelMapper.map(currentBatchDetailsDto, batchDetails);
			// save current batch object
			BatchDetails currentDbBatchDetails = batchDetailsRepository.save(batchDetails);
			log.info("performSourceDataBatchProcessing() current batch process details saved in DB ");

			// get last batch processing time
			try {
				if (lastBatchDetails.getStartDateTime() != null) {
					lastBatchProcessingStartTime = lastBatchDetails.getStartDateTime();
					log.info("performSourceDataBatchProcessing() last batch process start time : "
							+ lastBatchDetails.getStartDateTime());
				}

				// iterate each user and get their events and save to UMS db
				userDtoList.forEach(userDto -> {
					//if (!environment.getProperty("userprincipal.exclude.users").contains(userDto.getEmail())) {
						// the employees/users whoese teams user id is not added,
						// they will just be ignored while getting the batch processing
						// details.(TeamsUserId is optional)
						if (userDto.getEmployeeStatus().equalsIgnoreCase("Active") && userDto.getTeamsUserId() != null && !userDto.getTeamsUserId().isBlank() && 
								userDto.getBatchProcessStatus().equalsIgnoreCase("Enabled")) {
							String userId = userDto.getTeamsUserId();
							// get userprincipalName and pass it to calendarView method to fetch the
							// calendar events if the user
							String userPrincipalName = userDto.getEmail();
							log.info("BATCH PROCESSING STARTED FOR USER : " + userPrincipalName + " WITH TEAMS USERID : "
									+ userId);
							// get users calendar view of events using principal name
							List<EventDto> calendarEventsDtolist = getUserCalendarView(userDto,
									lastBatchProcessingStartTime);
                            if(calendarEventsDtolist.size() > 0) {
                            	// save user events
    							List<Event> userEventList = saveAllUsersCalendarEvents(calendarEventsDtolist, userDto,
    									currentDbBatchDetails.getBatchId());
    							if (!userEventList.isEmpty()) {
    								allUsersEventListOfCurrentBatchProcess.add(userEventList);
    							}
                            }else {
                            	log.info("No meetings for user "+userDto.getEmail()+" in current batch process");
                            }
						} else {
							log.info("BATCH PROCESSING EXCLUDED FOR USER : " + userDto.getEmail()+" ---NO TEAMS USER ID---");
						}
					//}
				});
				// log.info(allUsersEventList.toString());
				// set current batch processing details, if passed
				currentDbBatchDetails.setStatus("COMPLETED");
				currentDbBatchDetails.setEndDateTime(LocalDateTime.now());
				currentDbBatchDetails.setLastSuccessfullExecutionDateTime(currentbatchStartTime);
				BatchDetails currentBatchDetails =   batchDetailsRepository.save(currentDbBatchDetails);
				
				boolean status = sendBatchProcessEmail("ums-support@ikcontech.com","COMPLETED",currentBatchDetails.getStartDateTime(),currentBatchDetails.getEndDateTime());
				log.info("performSourceDataBatchProcessing() email sended status : "+ status);
				log.info("performSourceDataBatchProcessing() Current batch processing details after completion " + currentDbBatchDetails);

				// if batch processing contains any events of users , then copy the meetings to
				// meeting microservice
				if (!allUsersEventListOfCurrentBatchProcess.isEmpty())
					// copy all the events of current batch processing to meeting microservice
					copySourceDataofCurrentBatchProcessingToMeetingsMicroservice(
							allUsersEventListOfCurrentBatchProcess);
				log.info("performSourceDataBatchProcessing() executed successfully.");
			}catch (HttpClientErrorException  e) {
				currentDbBatchDetails.setStatus("FAILED");
				currentDbBatchDetails.setEndDateTime(LocalDateTime.now());
				currentDbBatchDetails.setBatchProcessFailureReason(e.getMessage());
				BatchDetails currentBatchDetails =   batchDetailsRepository.saveAndFlush(currentDbBatchDetails);
				boolean status = sendBatchProcessEmail("ums-support@ikcontech.com","FAILED",currentBatchDetails.getStartDateTime(),currentBatchDetails.getEndDateTime());
				log.info("performSourceDataBatchProcessing() email sended status"+ status);
				log.info("performSourceDataBatchProcessing() Current batch processing details after exception " + currentDbBatchDetails);
				log.error("performSourceDataBatchProcessing() HttpClientErrorException :Exception occured while batch processing in Business layer " + e.getMessage(), e);
			} 
			catch (Exception e) {
				// set current batch processing details, if failed
				currentDbBatchDetails.setStatus("FAILED");
				currentDbBatchDetails.setEndDateTime(LocalDateTime.now());
				currentDbBatchDetails.setBatchProcessFailureReason(e.getMessage());
				BatchDetails currentBatchDetails =   batchDetailsRepository.saveAndFlush(currentDbBatchDetails);
				boolean status = sendBatchProcessEmail("ums-support@ikcontech.com","FAILED",currentBatchDetails.getStartDateTime(),currentBatchDetails.getEndDateTime());
				log.info("performSourceDataBatchProcessing() email sended status"+ status);
				log.info("performSourceDataBatchProcessing() Current batch processing details after exception " + currentDbBatchDetails);
				log.error("performSourceDataBatchProcessing() BusinessException :Exception occured while batch processing in Business layer " + e.getMessage(), e);
			}
		} else {
			log.error("performSourceDataBatchProcessing() UsersNotFoundException: Users not found for batch processing / Users List is empty.");
			throw new UsersNotFoundException(ErrorCodeMessages.ERR_MSTEAMS_USERS_NOT_FOUND_CODE,
					ErrorCodeMessages.ERR_MSTEAMS_USERS_NOT_FOUND_MSG);
		}
	}

	private List<EventDto> getUserCalendarView(EmployeeVO userDto, LocalDateTime lastSuccessfulBatchStartTime) {
		log.info("getUserCalendarView() entered with args : employee object, lastSuccessfulBatchStartTime");
		StringBuilder calendarViewBaseUrl = null;
		LocalDateTime currentStartDateTimeUtc = LocalDateTime.now(ZoneOffset.UTC);
		// Set time to 12:00 AM
		LocalDateTime startOfTheDayUtc = currentStartDateTimeUtc
				.withHour(0)
				.withMinute(0)
				.withSecond(0)
				.withNano(0);
        LocalDateTime currentEndDateTimeUtc = LocalDateTime.now(ZoneOffset.UTC);
        // Set time to end of the day (11:59:59.999 PM)
        LocalDateTime endOfTheDayUtc = currentEndDateTimeUtc
                .withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(999_999_999);
        //get calendar meetings of a user between above times
			calendarViewBaseUrl = new StringBuilder("https://graph.microsoft.com/v1.0/users/" + userDto.getEmail()
					+ "/calendarView?startDateTime=" + startOfTheDayUtc + "&endDateTime=" + endOfTheDayUtc);
			log.info("getUserCalendarView() : user calendar view formed url : " + calendarViewBaseUrl);
			log.info(
					"getUserCalendarView() : user calendar view formed url : https://graph.microsoft.com/v1.0/users/\" + userDto.getEmail()\r\n"
							+ "					+ \"/calendarView?startDateTime=\" + startOfTheDayUtc + \"&endDateTime=\" + endOfTheDayUtc");
		var filter = environment.getProperty("calendarview.url.queryparam.filter");
		log.info("filter query param applied to URL : " + filter);
		var skip = environment.getProperty("calendarview.url.queryparam.skip");
		log.info("skip query param applied to URL : " + skip);
		// append select, filter and skip to calendarViewBaseUrl
		calendarViewBaseUrl = calendarViewBaseUrl.append(filter).append(skip);
		// prepare final URL
		var finalCalendarViewUrl = calendarViewBaseUrl.toString();
		log.info("getUserCalendarView() final calendar url of a organizer : " + finalCalendarViewUrl);
		// prepare required http headers for the request
		HttpHeaders headers = new HttpHeaders();
		headers.add(authHeader, tokenType + this.accessToken);
		headers.add(contentHeader, jsonContentType);
		// prepare http entity object with headers
		HttpEntity<String> hentity = new HttpEntity<>(headers);
		ResponseEntity<CalendarViewResponseWrapper> response = graphRestTemplate.exchange(finalCalendarViewUrl,
				HttpMethod.GET, hentity, CalendarViewResponseWrapper.class);
		log.info("getUserCalendarView() sending request to graph api with prepared url : " + finalCalendarViewUrl);
		// get the response (list of calendar event objects)
		CalendarViewResponseWrapper calendarViewWrapper = response.getBody();
		log.info("getUserCalendarView() : calendar view of organizer "+userDto.getEmail()+" obtained.");
		List<EventDto> listCalendarViewDto = calendarViewWrapper.getValue();
		// get user events with its attached online meetings
		// for each event attach corresponding online meeting
		List<EventDto> updateEventsListDto = new ArrayList<>();
		Iterator<EventDto> calendarDtoIteratorOriginal = listCalendarViewDto.iterator();
		while(calendarDtoIteratorOriginal.hasNext()) {
			EventDto e = calendarDtoIteratorOriginal.next();
			int currentYear = LocalDate.now().getYear();
			int previousYear = currentYear - 1;
			if(!e.getCreatedDateTime().contains(String.valueOf(currentYear)) && !e.getCreatedDateTime().contains(String.valueOf(previousYear))){
				calendarDtoIteratorOriginal.remove();
			}
		}
		Iterator<EventDto> calendarDtoIterator = listCalendarViewDto.iterator();
		while(calendarDtoIterator.hasNext()) {
			var calendarEventDto = calendarDtoIterator.next();
			log.info("getUserCalendarView() Organizer " +userDto.getFirstName()+" "+userDto.getLastName()+" with email id "+userDto.getEmail()+" Organized an Event/Meeting with subject : "+calendarEventDto.getSubject());
			//log.info("The Event/Meeting Join Url is "+calendarEventDto.getOnlineMeeting().getJoinUrl());
			List<Event> currentDayEventsList = eventRepository.getCurrentDayEvents(LocalDate.now(),userDto.getEmail());
			Iterator<Event> itr = currentDayEventsList.iterator();
			while(itr.hasNext()) {
				Event e = itr.next();
				if(calendarEventDto.getType().equals("singleInstance") && calendarEventDto.getEventId().equals(e.getEventId())) {
					//if the current incoming meeting/event is already exist in our DB , execuled it from the batch process
					//but check if the attendance report of the meeting/event is modified, if it is modified that means
					//the meeting/event was conducted again,then just update the meeting's/event's attendance report.
					log.info("The Single Instance Event '"+calendarEventDto.getSubject()+"' is already found in db table 'event_sourcedata_tab',just updating attendance report of the event...");
					Event eventToBeUpdated = e;
					if(eventToBeUpdated != null) {
						var dbEventAttendanceReportCount = eventToBeUpdated.getAttendanceReport().size();
						//get current attendance report count of event/meeting
						OnlineMeetingDto onlineMeetingDto = getOnlineMeeting(calendarEventDto.getOnlineMeeting().getJoinUrl(), userDto.getTeamsUserId());
						var updatedAttendanceReportCountFromGraphAPI = onlineMeetingDto.getAttendanceReport().size();
						//if count of attendance report in DB and from Graph API of meeting are not same, 
						//then just update the attendance report of event
						//This dbAttendanceReportList collection is LAZY ! calling the below line would get the actual attendance report from DB
						List<AttendanceReport> dbAttendanceReportList = eventToBeUpdated.getAttendanceReport();
						List<AttendanceReportDto> attendanceReportFromGraphApiList = onlineMeetingDto.getAttendanceReport();
						if(dbEventAttendanceReportCount != updatedAttendanceReportCountFromGraphAPI) {
							dbAttendanceReportList.forEach(dbReport -> {
								AttendanceReportDto dto = new AttendanceReportDto();
								ObjectMapper.modelMapper.map(dbReport, dto);
								attendanceReportFromGraphApiList.remove(dto);
							});
							Iterator<AttendanceReportDto> uniqueAttendanceReportIterator = attendanceReportFromGraphApiList.iterator();
							AttendanceReport uniqueAttendanceReport =  new AttendanceReport();
							AttendanceReportDto uniAttendanceReportDto = uniqueAttendanceReportIterator.next();
							ObjectMapper.modelMapper.map(uniAttendanceReportDto, uniqueAttendanceReport);
							dbAttendanceReportList.add(uniqueAttendanceReport);
							//the above final collection (dbAttendanceReportList) now contains only the unique attendance
							//report of an existing event of UMS DB with a new attendance reports of event.
							Event updatedEvent = eventRepository.save(eventToBeUpdated);
							//update the event details in Meeting microservice too !
							updateEventinMeetingMicroservice(updatedEvent);
						}
					}
					log.info("Exlucded this Event / Meeting '"+calendarEventDto.getSubject()+"' from batch process of organizer "+userDto.getEmail()+" as it is already found in Database");
					calendarDtoIterator.remove();
					break;
				}else if(calendarEventDto.getType().equals("occurrence") && calendarEventDto.getOccurrenceId().equalsIgnoreCase(e.getOccurrenceId())) {
					log.info("Recurring Event '"+calendarEventDto.getSubject()+"' already found in db table 'event_sourcedata_tab',updating attendance report of the event...");
					Event recurringeventToBeUpdated = e;
					if(recurringeventToBeUpdated != null) {
						//get current attendance report count of event/meeting
						OnlineMeetingDto onlineMeetingDto = getOnlineMeeting(calendarEventDto.getOnlineMeeting().getJoinUrl(), userDto.getTeamsUserId());
						//if count of attendance report in DB and from Graph API of meeting are not same, 
						//then just update the attendance report of event
						//This dbAttendanceReportList collection is LAZY ! calling the below line would get the actual attendance report from DB
						List<AttendanceReportDto> attendanceReportFromGraphApiList = onlineMeetingDto.getAttendanceReport();
						var dbOccurenceEvents = eventRepository.findBySeriesMasterId(recurringeventToBeUpdated.getSeriesMasterId());
						if(dbOccurenceEvents != null) {
							int totaldbAttReportCount = 0;
							for(int i=0; i<dbOccurenceEvents.size(); i++) {
								int attreportCount = dbOccurenceEvents.get(i).getAttendanceReport().size();
								totaldbAttReportCount += attreportCount;
							}
							//compare attendance report counts, if matches remove the event from current batch process
							if(attendanceReportFromGraphApiList.size() == totaldbAttReportCount) {
								//remove from batch process
								calendarDtoIterator.remove();
								break;
							}else {
								//add the meeting into batch process and also update attendance records, bcz this meeting is already present in db
								//it will have attendance reports, you just need to insert this recurring meeting and update attendance report
								List<List<AttendanceReport>> recurringEventAttendanceReportsLists = new ArrayList<>();
								for(int i=0; i<dbOccurenceEvents.size(); i++) {
									var attendanceReportOfSingleOccurrence = dbOccurenceEvents.get(i).getAttendanceReport();
									recurringEventAttendanceReportsLists.add(attendanceReportOfSingleOccurrence);
								}
								//prepare a single list collection with all attendance reports of a recurring event from multiple collections
								 List<AttendanceReport> singleAttendanceReportsListOfRecurringEvent = new ArrayList<>();
								for (List<AttendanceReport> list : recurringEventAttendanceReportsLists) {
									singleAttendanceReportsListOfRecurringEvent.addAll(list);
						        }
								//compare the attendance reports from teams and Db and removed matched repots, 
								//if found any unique attendance report then attach that report to this recurring event
								//Iterator<AttendanceReport> itrE = singleAttendanceReportsListOfRecurringEvent.iterator();
								singleAttendanceReportsListOfRecurringEvent.forEach(report -> {
									Iterator<AttendanceReportDto> itrDto = attendanceReportFromGraphApiList.iterator();
									while(itrDto.hasNext()) {
										AttendanceReportDto reportDto = itrDto.next();
										if(reportDto.getAttendanceReportId().equalsIgnoreCase(report.getAttendanceReportId())) {
											itrDto.remove();
										}
									}
								});
								var dbAttReport = recurringeventToBeUpdated.getAttendanceReport();
								attendanceReportFromGraphApiList.forEach(reportDto -> {
									AttendanceReport newReport = new AttendanceReport();
									ObjectMapper.modelMapper.map(reportDto, newReport);
									dbAttReport.add(newReport);
								});
								recurringeventToBeUpdated.setAttendanceReport(dbAttReport);
							}
						}
							//the above final collection (dbAttendanceReportList) now contains only the unique attendance
							//report of an existing event of UMS DB with a new attendance reports of event.
							Event updatedEvent = eventRepository.save(recurringeventToBeUpdated);
							//update the event details in Meeting microservice too !
							updateEventinMeetingMicroservice(updatedEvent);
					}
					log.info("Excluded this Recurring Event / Meeting '"+calendarEventDto.getSubject()+"' from batch process of organizer "+userDto.getEmail()+" as it is already found in Database");
					calendarDtoIterator.remove();
					break;
				}
			}
			
		}
		List<EventDto> listCalendarViewDto2 = new CopyOnWriteArrayList<>(listCalendarViewDto);
		Iterator<EventDto> calendarDtoIterator2 = listCalendarViewDto2.iterator();
			while(calendarDtoIterator2.hasNext()) {
				EventDto  eventDto = calendarDtoIterator2.next();
				log.info("getUserCalendarView() Including this Event/Meeting '"+eventDto.getSubject()+"' in batch processing of organizer "+userDto.getEmail());
				// attach userId and principal name to event
				// attach online meeting to event
				EventDto eventWithOnlineMeeting = null;
				// get an event's online meeting and transcript, only if the event is an online
				// Event
				if (eventDto.getOnlineMeeting() != null) {
					eventWithOnlineMeeting = attachOnlineMeetingDetailsToEvent(eventDto, userDto);
					log.info(
							"getUserCalendarView() : user calendar view events updated with its respective online meeting objects.");
					//first check the meetings in the users calendar is Completed, if completed then get those meetings in this batch process
					var attendanceReportFromGraphAPI = eventWithOnlineMeeting.getAttendanceReport();
					if(eventWithOnlineMeeting.getType().equalsIgnoreCase("occurrence")) {
						//if no attendance report remove the event from cutrrent batch process
						if(attendanceReportFromGraphAPI == null || attendanceReportFromGraphAPI.size() == 0) {
							listCalendarViewDto2.remove(eventWithOnlineMeeting);
							log.info("The Recurring Event/Meeting '"+eventWithOnlineMeeting.getSubject()+"' which was tried to include in current batch process is still not attended. Excluding it from current batch process");
						}else {
							//using the master eventid of this occurence, get the attendance report size of the entire event from db,
							// compare the db attendance report size with incoming attendance report size from teams db, if equal exclude it from Batch process
							//else include it in batch process
							var dbOccurenceEvents = eventRepository.findBySeriesMasterId(eventWithOnlineMeeting.getSeriesMasterId());
							if(dbOccurenceEvents != null) {
								int totaldbAttReportCount = 0;
								for(int i=0; i<dbOccurenceEvents.size(); i++) {
									int attreportCount = dbOccurenceEvents.get(i).getAttendanceReport().size();
									totaldbAttReportCount += attreportCount;
								}
								//compare attendance report counts, if matches remove the event from current batch process
								if(attendanceReportFromGraphAPI.size() == totaldbAttReportCount) {
									//remove from batch process
									listCalendarViewDto2.remove(eventWithOnlineMeeting);
									log.info("The Recurring Event/Meeting '"+eventWithOnlineMeeting.getSubject()+"' which was tried to include in current batch process is still not attended. Excluding it from current batch process");
								}else {
									//add the meeting into batch process and also update attendance records, bcz this meeting is already present in db
									//it will have attendance reports, you just need to insert this recurring meeting and update attendance report
									List<List<AttendanceReport>> recurringEventAttendanceReportsLists = new ArrayList<>();
									for(int i=0; i<dbOccurenceEvents.size(); i++) {
										var attendanceReportOfSingleOccurrence = dbOccurenceEvents.get(i).getAttendanceReport();
										recurringEventAttendanceReportsLists.add(attendanceReportOfSingleOccurrence);
									}
									//prepare a single list collection with all attendance reports of a recurring event from multiple collections
									 List<AttendanceReport> singleAttendanceReportsListOfRecurringEvent = new ArrayList<>();
									for (List<AttendanceReport> list : recurringEventAttendanceReportsLists) {
										singleAttendanceReportsListOfRecurringEvent.addAll(list);
							        }
									//compare the attendance reports from teams and Db and removed matched repots, 
									//if found any unique attendance report then attach that report to this recurring event
									//Iterator<AttendanceReport> itr = singleAttendanceReportsListOfRecurringEvent.iterator();
									singleAttendanceReportsListOfRecurringEvent.forEach(report -> {
										Iterator<AttendanceReportDto> itrDto = attendanceReportFromGraphAPI.iterator();
										while(itrDto.hasNext()) {
											AttendanceReportDto reportDto = itrDto.next();
											if(report.getAttendanceReportId().equalsIgnoreCase(reportDto.getAttendanceReportId())) {
												itrDto.remove();
											}
										}
									});
									eventWithOnlineMeeting.setAttendanceReport(attendanceReportFromGraphAPI);
								}
							}
						}
						
					}
					else if(eventWithOnlineMeeting.getType().equalsIgnoreCase("singleInstance") && attendanceReportFromGraphAPI == null 
							|| attendanceReportFromGraphAPI.size() == 0) {
						listCalendarViewDto2.remove(eventWithOnlineMeeting);
						log.info("The Single Instance Event/Meeting '"+eventWithOnlineMeeting.getSubject()+"' which was tried to include in current batch process is still not attended. Excluding it from current batch process");
					}
					
					//finally update the unique event from todays calendar of a user with its online meeting and transcript, 
					//then insert it into DB.
					if(listCalendarViewDto2.contains(eventWithOnlineMeeting)) {
						EventDto updatedEventWithOnlineMeetingAndTranscript = null;
						updatedEventWithOnlineMeetingAndTranscript = attachTranscriptsToOnlineMeeting(
								eventWithOnlineMeeting, userDto);
						log.info(
								"getUserCalendarView() : user calendar view events with online meeting objects updated with its respective trascripts.");
						updateEventsListDto.add(updatedEventWithOnlineMeetingAndTranscript);
					}
				}
			}
			log.info(
					"getUserCalendarView() : updated user calendar events with its online meetings and transcripts returned");
			log.info(
					"getUserCalendarView() : executed successfully.");
		return updateEventsListDto;
	}

	// attach online meeting to event, so that event will  contain its online meeting details
	private EventDto attachOnlineMeetingDetailsToEvent(EventDto eventDto, EmployeeVO user) {
		log.info("attachOnlineMeetingDetailsToEvent() : entered with args : event object , employee object");
		log.info("attachOnlineMeetingDetailsToEvent() : is under execution.");
		// if joinurl is not null, then the event is an online meeting, proceed to get
		// and insert online meeting object
		if (eventDto.getOnlineMeeting() != null) {
			String meetingJoinUrl = eventDto.getOnlineMeeting().getJoinUrl();

			// get online meeting objects of the user one by one and attach to event
			OnlineMeetingDto onlineMeeting = getOnlineMeeting(meetingJoinUrl, user.getTeamsUserId());
			if (onlineMeeting != null) {
				eventDto.getOnlineMeeting().setOnlineMeetingId(onlineMeeting.getOnlineMeetingId());
				eventDto.getOnlineMeeting().setSubject(onlineMeeting.getSubject());
				eventDto.getOnlineMeeting().setOnlineMeetingType(eventDto.getType());
				if (eventDto.getOccurrenceId() != null) {
					eventDto.getOnlineMeeting().setOccurrenceId(eventDto.getOccurrenceId());
				}
				eventDto.setAttendanceReport(onlineMeeting.getAttendanceReport());
			}
		}
		log.info("attachOnlineMeetingDetailsToEvent() : online meeting details attached to its event object.");
		log.info("attachOnlineMeetingDetailsToEvent() executed successfully.");
		return eventDto;
	}

	// attach transcripts to respective online meetings
	private EventDto attachTranscriptsToOnlineMeeting(EventDto eventDto, EmployeeVO user) {
		log.info("attachTranscriptsToOnlineMeeting() : entered with args : event object , employee object");
		// Retrieve transcripts of particular instance of the recurring meeting and
		// insert into db
		if (eventDto.getOccurrenceId() != null) {
			List<TranscriptDto> transcriptsListDto = getOnlineMeetingTranscriptDetails(user.getTeamsUserId(),
					eventDto.getOnlineMeeting().getOnlineMeetingId());
			List<TranscriptDto> actualMeetingTranscriptsListDto = new ArrayList<>();
			transcriptsListDto.forEach(transcriptDetail -> {
				// get created date of transcripts and compare with meeting occurrence date
				String timestamp = transcriptDetail.getCreatedDateTime();
				ZonedDateTime zonedDateTime = ZonedDateTime.parse(timestamp);
				LocalDate dateOnly = zonedDateTime.toLocalDate();
				DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				String transcriptGeneratedDate = dateOnly.format(dateFormatter);

				// if transcript date matches with occurrence date, add all those transcripts to
				// list and attach to the online meeting object
				if (eventDto.getOccurrenceId().contains(transcriptGeneratedDate)) {
					actualMeetingTranscriptsListDto.add(transcriptDetail);

					// write transcripts data into a file and store in the file loc in db
					if (actualMeetingTranscriptsListDto != null) {
						actualMeetingTranscriptsListDto.forEach(transcriptDto -> {
							var transcriptContent = getTranscriptContent(transcriptDto.getTranscriptContentUrl());

							// write transcript into file
							try (FileWriter fileWriter = new FileWriter(
									partialTranscriptFileName+" " + transcriptDto.getTranscriptId())) {
								fileWriter.write(transcriptContent);
								log.info("Transcript content has been written to the file.");

								// save file loc to db
								// Get the file path
								File file = new File(partialTranscriptFileName+" " + transcriptDto.getTranscriptId());
								var filePath = file.getAbsolutePath();
								log.info("Transcript File path: " + filePath);
								if (!filePath.equalsIgnoreCase("")) {
									// set transcript file path
									transcriptDto.setTranscriptFilePath(filePath);
									transcriptDto.setTranscriptContent(transcriptContent);
									// set transcript content into db column
									// byte[] transcriptContentBytes = transcriptContent.getBytes("UTF-8");
									// transcriptDto.setTranscriptContent(Arrays.toString(transcriptContentBytes));
								}
							} catch (IOException e) {
								log.error("attachTranscriptsToOnlineMeeting() Exception occutred while attaching transcript content to its online meeting. "+e.getMessage(), e);
								throw new TranscriptGenerationFailedException(ErrorCodeMessages.ERR_MSTEAMS_TRANSCRIPT_GENERATION_FAILED_CODE,
										ErrorCodeMessages.ERR_MSTEAMS_TRANSCRIPT_GENERATION_FAILED_MSG);
							}
						});
						eventDto.getOnlineMeeting().setMeetingTranscripts(actualMeetingTranscriptsListDto);
					}
				}
			});
		} // if
		else {
			// Retrieve transcripts of single instance meeting and insert into db
			List<TranscriptDto> transcriptsListDto = getOnlineMeetingTranscriptDetails(user.getTeamsUserId(),
					eventDto.getOnlineMeeting().getOnlineMeetingId());
			if (transcriptsListDto != null) {
				// eventDto.getOnlineMeeting().setMeetingTranscripts(transcriptsListDto);
				transcriptsListDto.forEach(transcript -> {
					var transcriptContent = getTranscriptContent(transcript.getTranscriptContentUrl());
					// write transcript into file
					try (FileWriter fileWriter = new FileWriter(partialTranscriptFileName+" " + transcript.getTranscriptId())) {
						fileWriter.write(transcriptContent);
						log.info("Transcript content has been written to the file.");
						// save file loc to db
						// Get the file path
						File file = new File("Transcript-" + transcript.getTranscriptId());
						var filePath = file.getPath();
						log.info("Transcript File path: " + filePath);
						if (!filePath.equalsIgnoreCase("")) {
							// set transcript file path
							transcript.setTranscriptFilePath(filePath);
							// byte[] transcriptContentBytes = transcriptContent.getBytes();
							// String str =
							// org.apache.commons.codec.binary.Base64.encodeBase64String(transcriptContentBytes);
							transcript.setTranscriptContent(transcriptContent);
						}
					} catch (IOException e) {
						log.error("attachTranscriptsToOnlineMeeting() Exception occutred while attaching transcript content to its online meeting. "+e.getMessage(), e);
						throw new RuntimeException(e);
					}
				});
				// set transcripts to online meeting of the event
				eventDto.getOnlineMeeting().setMeetingTranscripts(transcriptsListDto);
			}
		}
		log.info(
				"attachOnlineMeetingDetailsToEvent() : online meeting transcript details attached to its online meeting object.");
		log.info("attachOnlineMeetingDetailsToEvent() executed successfully.");
		return eventDto;
	}

	// map all event dto's to event entities to save into db
	
	private List<Event> saveAllUsersCalendarEvents(List<EventDto> eventDtoList, EmployeeVO user,
			Long currentBatchProcessId) {
		log.info("saveAllUsersCalendarEvents() entered with args : event objects, user object, currentBatchProcessId");
		log.info("saveAllUsersCalendarEvents() is under execution.");
		List<Event> eventEntities = new ArrayList<>();
		eventDtoList.forEach(eventDto -> {
			// iterate through event objects and get the online meeting object from each
			// event object one by one
			Event event = new Event();
			// map eventDto to event and add all the events to list
			ObjectMapper.modelMapper.map(eventDto, event);
			// map manual property entries
			event.setOnlineMeetingId(eventDto.getOnlineMeeting().getOnlineMeetingId());
			event.setStartDateTime(LocalDateTime.parse(eventDto.getStart().getDateTime()));
			event.setStartTimeZone(eventDto.getStart().getTimeZone());
			event.setEndDateTime(LocalDateTime.parse(eventDto.getEnd().getDateTime()));
			event.setEndTimeZone(eventDto.getEnd().getTimeZone());
			event.setLocation(eventDto.getLocation().getDisplayName());
			event.setOrganizerEmailId(eventDto.getOrganizer().getEmailAddress().getAddress().toLowerCase());
			// if(user.getDepartment() != null) {
			// event.setDepartmentId(user.getDepartment().getDepartmentId());
			// }
			event.setDepartmentId(user.getDepartmentId());
			event.setOrganizerName(eventDto.getOrganizer().getEmailAddress().getName());
			event.setJoinUrl(eventDto.getOnlineMeeting().getJoinUrl());
			event.setEmailId(user.getEmail().toLowerCase());
			List<TranscriptDto> retrivedTranscriptDtos = eventDto.getOnlineMeeting().getMeetingTranscripts();
			List<Transcript> transcripts = new ArrayList<>();
			if (retrivedTranscriptDtos != null) {
				retrivedTranscriptDtos.forEach(transcriptDto -> {
					Transcript transcript = new Transcript();
					ObjectMapper.modelMapper.map(transcriptDto, transcript);
					transcripts.add(transcript);
				});
				// set transcripts to event
				event.setMeetingTranscripts(transcripts);
			}
			// set attendees to event
			Set<Attendee> attendeesList = new HashSet<>();
			List<String> mailIds = new ArrayList<>();
			eventDto.getAttendees().forEach(attendeeDto -> {
				Attendee attendee = new Attendee();
				String attendeeEmailAddress = attendeeDto.getEmailAddress().getAddress().toLowerCase();
				// map dto to entity
				ObjectMapper.modelMapper.map(attendeeDto, attendee);
				// manually map the unmatched field
				attendee.setEmail(attendeeEmailAddress);
				attendee.setStatus(attendeeDto.getStatus().getResponse());
				// attendee mail ids list
				mailIds.add(attendeeEmailAddress);
				attendeesList.add(attendee);
				// set event to attendee
				// attendee.setEvent(event);
			});
			// set user profile for each attendee
			List<EmployeeVO> userProfilesList = this.userDtoList;
			attendeesList.forEach(attendee -> {
				for (int i = 0; i < userProfilesList.size(); i++) {
					if (attendee.getEmail().equalsIgnoreCase(userProfilesList.get(i).getEmail())) {
						// attendee.setUser(userProfilesList.get(i));
						attendee.setEmailId(userProfilesList.get(i).getEmail().toLowerCase());
						// userProfilesList.get(i).getDepartment().getDepartmentId();
					} 
				}
			});
			event.setAttendees(attendeesList);
			event.setBatchId(currentBatchProcessId);
			List<AttendanceReportDto> attendanceReportDtoList = eventDto.getOnlineMeeting().getAttendanceReport();
			List<AttendanceReport> attendanceReportList = new ArrayList<>();
			if(attendanceReportDtoList != null) {
				attendanceReportDtoList.forEach(dto -> {
					AttendanceReport attendanceReport = new AttendanceReport();
					ObjectMapper.modelMapper.map(dto, attendanceReport);
					attendanceReportList.add(attendanceReport);
				});
			}
			//set attendance reports for an event
			if(attendanceReportList.size() > 0) {
				event.setAttendanceReport(attendanceReportList);
			}
			// logic for isOnlinemeeting based on join url (optional)
			eventEntities.add(event);
			// finally save all event entities of the user in UMS DB,
			// now all the events contain (its online meeting Id and transcript details if
			// any)
		});
		// returns the saved events list
		List<Event> userEventsList = eventRepository.saveAll(eventEntities);
		log.info("saveAllUsersCalendarEvents() user calendar events raw data saved in DB.");
		log.info("saveAllUsersCalendarEvents() executed successfully.");
		return userEventsList;
	}

	private OnlineMeetingDto getOnlineMeeting(String joinWebUrl, String userId) {
		log.info("getOnlineMeeting() entered with args : joinWebUrl , teamsUserId");
		log.info("getOnlineMeeting() is under execution...");
		// prepare url to get online meeting object
		StringBuilder stringBuilder = new StringBuilder("https://graph.microsoft.com/v1.0/users/" + userId
				+ "/onlineMeetings?$filter=joinWebUrl eq '" + joinWebUrl + "'");
		var finalUrl = stringBuilder.toString();
		// prepare Http headers required for the request
		HttpHeaders headers = new HttpHeaders();
		headers.add(authHeader, tokenType + this.accessToken);
		headers.add(contentHeader, jsonContentType);
		// prepare http entity with headers
		HttpEntity<String> hentity = new HttpEntity<>(headers);
		// prepare rest template and hit meeting end point
		ResponseEntity<OnlineMeetingResponseWrapper> response = graphRestTemplate.exchange(finalUrl, HttpMethod.GET, hentity,
				OnlineMeetingResponseWrapper.class);
		List<OnlineMeetingDto> onlineMeetingsList = response.getBody().getValue();
		// however there will be only single meeting object, even if it returns a list,
		// get the 0th index from list which will give the meeting object
		OnlineMeetingDto onlineMeetingDto = onlineMeetingsList.get(0);
		List<AttendanceReportDto> basicAttendanceReportList = getBasicAttendanceReportsOfOnlineMeeting(userId, onlineMeetingDto.getOnlineMeetingId());
		//send basic attendance report list and get a detailed attendance report list
		List<AttendanceReportDto> detailedAttendanceReportList = getDetailedAttendanceOfEachAttendanceReportByReportId(userId, onlineMeetingDto.getOnlineMeetingId(), basicAttendanceReportList);
		detailedAttendanceReportList.forEach(report -> {
			report.getAttendanceRecords().forEach(record -> {
				record.setEmailAddress(record.getEmailAddress().toLowerCase());
			});
		});
		onlineMeetingDto.setAttendanceReport(detailedAttendanceReportList);
		log.info("getOnlineMeeting() executed successfully");
		return onlineMeetingDto;
	}
	
	private List<AttendanceReportDto> getBasicAttendanceReportsOfOnlineMeeting(String userId,String onlineMeetingId) {
		log.info("getAttendanceReportOfOnlineMeeting() entered with args : onlineMeetingId");
		log.info("getAttendanceReportOfOnlineMeeting() is under execution...");
		StringBuilder urlBuilder = new StringBuilder("https://graph.microsoft.com/v1.0/users/" + userId+"/onlineMeetings/"+onlineMeetingId+"/attendanceReports");
	    var finalUrl = urlBuilder.toString();
		// prepare headers for the request
		HttpHeaders headers = new HttpHeaders();
		headers.add(authHeader, tokenType + this.accessToken);
		headers.add(contentHeader, jsonContentType);
		// prepare http entity object
		HttpEntity<String> hentity = new HttpEntity<>(headers);
		ResponseEntity<AttendanceReportResponseWrapper> response = graphRestTemplate.exchange(finalUrl, HttpMethod.GET, hentity, AttendanceReportResponseWrapper.class);
		var basicAttendanceReportList = response.getBody().getValue();
		log.info("getAttendanceReportOfOnlineMeeting() executed successfully");
		return basicAttendanceReportList;
	}
	
	private List<AttendanceReportDto> getDetailedAttendanceOfEachAttendanceReportByReportId(String userId, String onlineMeetingId, List<AttendanceReportDto> attendanceReportList) {
		log.info("getDetailedAttendanceOfEachAttendanceReportByReportId() entered with args : userId, onlineMeetingId, attendanceReportsList");
		log.info("getDetailedAttendanceOfEachAttendanceReportByReportId() is under execution...");
		List<AttendanceReportDto> detailedAttendanceReportList = new ArrayList<>();
		//for each report of the meeting get details 
		//# an online meeting could have multiple attendance reports, so we get a list of attendance report details
		//for each report send request and get details of the attendance report of the meeting Id
		if(attendanceReportList.size() > 0) {
			attendanceReportList.forEach(report -> {
				StringBuilder urlBuilder = new StringBuilder("https://graph.microsoft.com/v1.0/users/" + userId+"/onlineMeetings/"+onlineMeetingId+"/attendanceReports/"+report.getAttendanceReportId()+"?$expand=attendanceRecords");
			    var finalUrl = urlBuilder.toString();
				// prepare headers for the request
				HttpHeaders headers = new HttpHeaders();
				headers.add(authHeader, tokenType + this.accessToken);
				headers.add(contentHeader, jsonContentType);
				// prepare http entity object
				HttpEntity<String> hentity = new HttpEntity<>(headers);
				ResponseEntity<AttendanceReportDto> response = graphRestTemplate.exchange(finalUrl, HttpMethod.GET, hentity, AttendanceReportDto.class);
				AttendanceReportDto detailedAttendanceReport = response.getBody();
				detailedAttendanceReportList.add(detailedAttendanceReport);
			});
		}
		log.info("getDetailedAttendanceOfEachAttendanceReportByReportId() executed successfully");
		return detailedAttendanceReportList;
	}

	private List<TranscriptDto> getOnlineMeetingTranscriptDetails(String userId, String onlineMeetingId) {
		log.info("getOnlineMeetingTranscriptDetails() entered with args : transcriptContentURL");
		log.info("getOnlineMeetingTranscriptDetails() is under execution...");
		List<TranscriptDto> meetingTranscriptsList = new ArrayList<>();
		StringBuilder stringBuilder = new StringBuilder("https://graph.microsoft.com/v1.0/users/" + userId
				+ "/onlineMeetings('" + onlineMeetingId + "')/transcripts");
		var url = stringBuilder.toString();
		// prepare headers for the request
		HttpHeaders headers = new HttpHeaders();
		headers.add(authHeader, tokenType + this.accessToken);
		headers.add(contentHeader, jsonContentType);
		// prepare http entity object
		HttpEntity<String> hentity = new HttpEntity<>(headers);
		// prepare rest template and hit transcript end point to fetch transcripts for the meeting
		ResponseEntity<TranscriptsResponseWrapper> response = graphRestTemplate.exchange(url, HttpMethod.GET, hentity,
				TranscriptsResponseWrapper.class);
		// check whether transcript are present for the meeting, the odataCount gives
		// the count of transcripts available for the meeting.
		// if odataCount is 0, i.e there is no transcript available for the meeting
		if (response.getBody().getOdataCount() > 0) {
			meetingTranscriptsList = response.getBody().getValue();
			log.info("getOnlineMeetingTranscriptDetails() executed successfully");
			return meetingTranscriptsList;
		} else {
			// return empty meeting transcripts list and handle it near the caller of this
			// method
			log.info("getOnlineMeetingTranscriptDetails() executed successfully");
			return meetingTranscriptsList;
		}
	}

	@Override
	public BatchDetailsDto getLatestSourceDataBatchProcessingRecordDetails() {
		log.info("getLatestSourceDataBatchProcessingRecordDetails() entered");
		log.info("getLatestSourceDataBatchProcessingRecordDetails() is under execution...");
		Optional<BatchDetails> optBatchDetails = batchDetailsRepository.getLatestBatchProcessingRecord();
		BatchDetails latestBatchDetails = null;
		BatchDetailsDto latestBatchDetailsDto = null;
		if (optBatchDetails.isPresent()) {
			latestBatchDetails = optBatchDetails.get();
			latestBatchDetailsDto = new BatchDetailsDto();
			// map entity to dto
			ObjectMapper.modelMapper.map(latestBatchDetails, latestBatchDetailsDto);
			return latestBatchDetailsDto;
		}
		// for the first time there will not be any latest batch process record,
		// so just return a dummy batch process object with status as completed.
		latestBatchDetailsDto = new BatchDetailsDto();
		latestBatchDetailsDto.setStatus("COMPLETED");
		log.info("getLatestSourceDataBatchProcessingRecordDetails() executed successfully.");
		return latestBatchDetailsDto;
	}

	// will pass the transcript content URL to this method from the transcript
	// details object.
	private String getTranscriptContent(String transcriptContentUrl) {
		log.info("getTranscriptContent() entered with args : transcriptContentURL");
		log.info("getTranscriptContent() is under execution...");
		StringBuilder stringBuilder = new StringBuilder(transcriptContentUrl);
		var url = stringBuilder.toString();
		// prepare headers for the request
		HttpHeaders headers = new HttpHeaders();
		headers.add(authHeader, tokenType + this.accessToken);
		headers.add("Accept", "text/vtt");
		headers.setContentType(MediaType.TEXT_PLAIN);
		// prepare http entity object with headers
		HttpEntity<String> hentity = new HttpEntity<>(headers);
		ResponseEntity<String> rentity = graphRestTemplate.exchange(url, HttpMethod.GET, hentity, String.class);
		// body of response contains the transcript content
		log.info("getTranscriptContent() executed sucessfully.");
		return rentity.getBody();
	}

	private void copySourceDataofCurrentBatchProcessingToMeetingsMicroservice(List<List<Event>> allUsersEventList) {
		log.info(
				"copySourceDataofCurrentBatchProcessingToMeetingsMicroservice() entered with allUsersEventList of current batchprocessing");
		log.info(
				"copySourceDataofCurrentBatchProcessingToMeetingsMicroservice() is under execution...");
		var url = "http://UMS-MEETING-SERVICE/meetings/";
		var httpEntity = new HttpEntity<>(allUsersEventList);
		var response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
		var successfulCopyMessage = response.getBody();
		log.info("copySourceDataofCurrentBatchProcessingToMeetingsMicroservice() call to meeting microservice : http://localhost:8012/meetings/ is sucessfull.");
		log.info("copySourceDataofCurrentBatchProcessingToMeetingsMicroservice() : "+successfulCopyMessage);
		log.info("copySourceDataofCurrentBatchProcessingToMeetingsMicroservice() executed successfully.");
	}

	@Override
	public List<BatchDetails> getBatchProcessDetails() {
		log.info("getBatchProcessDetails() entered");
		log.info("getBatchProcessDetails() is under execution...");
		var batchDetailsList = batchDetailsRepository.findAll();
		log.info("getBatchProcessDetails() executed successfully.");
		return batchDetailsList;
	}

	@Transactional(value = TxType.REQUIRED)
	@Override
	public CronDetails updateBatchProcessTime(CronDetails cronDetails) {
		log.info("updateBatchProcessTime() entered with args CronDetails object.");
		if(cronDetails == null ) {
			throw new EmptyInputException(ErrorCodeMessages.ERR_MSTEAMS_BATCH_PROCESS_CRONTIME_EMPTY_CODE, 
					ErrorCodeMessages.ERR_MSTEAMS_BATCH_PROCESS_CRONTIME_EMPTY_MSG);
		}
		log.info("updateBatchProcessTime() is under execution...");
		//there will be however ponly one record in the list
		var dbCron = cronRepository.findAll().get(0);
		//String minute = "*";
		//if(cronDetails.getMinute().equals("0")) {
			//cronDetails.setMinute("*");
		//}
		var cronTime = "";
		var hour = cronDetails.getHour().equals("0")?"*":cronDetails.getHour();
		if(hour.equals("*")) {
		   cronTime = "0 "+"*/"+cronDetails.getMinute()+" "+hour+" * * *";
		}else {
			if(cronDetails.getMinute().equals("0")) {
				cronTime = "0 0 */"+hour+" * * *";
			}else {
				cronTime = "0 "+"*/"+cronDetails.getMinute()+" */"+hour+" * * *";
			}
		}
			dbCron.setCronTime(cronTime);
			dbCron.setHour(cronDetails.getHour());
			dbCron.setMinute(cronDetails.getMinute());
		var updatedCron = cronRepository.save(dbCron);
		log.info("updateBatchProcessTime() executed successfully.");
		return updatedCron;
	}

	@Override
	public CronDetails getCronDetails() {
		log.info("getCronDetails() entered with no args");
		log.info("getCronDetails() is under execution...");
		var cronDetails = cronRepository.findAll().get(0);
		log.info("getCronDetails() executed successfully.");
		return cronDetails;
	}
	
	private void updateEventinMeetingMicroservice(Event eventToBeUpdated) {
		log.info("updateEventinMeetingMicroservice() entered with args : eventToBeUpdated");
		log.info("updateEventinMeetingMicroservice() is under execution...");
		var url = "http://UMS-MEETING-SERVICE/meetings/update/batchevent";
		var httpEntity = new HttpEntity<>(eventToBeUpdated);
		restTemplate.exchange(url, HttpMethod.PUT, httpEntity, Event.class);
		log.info("updateEventinMeetingMicroservice() executed successfully");
	}
	public boolean sendBatchProcessEmail(String email, String Status, LocalDateTime startDate, LocalDateTime endDate) {
		String Subject = "Batch Job Status";
    	String emailBody = null;
    	LocalDateTime timestampUtc = LocalDateTime.parse(startDate.toString());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timestampIstStr = timestampUtc.format(formatter);
        
        LocalDateTime timestampUtc1 = LocalDateTime.parse(endDate.toString());
        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timestampIstStr1 = timestampUtc1.format(formatter1);
    	if(Status == "COMPLETED") {
    		emailBody = "The scheduled batch process has been successfully executed within the designated timeframe."+"\r \n"+"Start Date Time : "+timestampIstStr+"\r \n"+
    				 "End Date Time : "+timestampIstStr1+ "\r \n";
    		emailService.sendMail(email, Subject, emailBody, false);
    	}
    	if(Status == "FAILED"){
    		emailBody = "The scheduled batch process did not execute successfully within the assigned timeframe."+"\r \n"+"Start Date Time : "+timestampIstStr+"\r \n"+
    				 "End Date Time : "+timestampIstStr1+ "\r \n";
    		emailService.sendMail(email, Subject, emailBody, false);
    	}
		return true;
	    	
	}
}
