package com.ikn.ums.msteams.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.azure.core.credential.AccessToken;
import com.ikn.ums.msteams.VO.EmployeeListVO;
import com.ikn.ums.msteams.VO.EmployeeVO;
import com.ikn.ums.msteams.dto.BatchDetailsDto;
import com.ikn.ums.msteams.dto.EventDto;
import com.ikn.ums.msteams.dto.OnlineMeetingDto;
import com.ikn.ums.msteams.dto.TranscriptDto;
import com.ikn.ums.msteams.entity.Attendee;
import com.ikn.ums.msteams.entity.BatchDetails;
import com.ikn.ums.msteams.entity.Event;
import com.ikn.ums.msteams.entity.Transcript;
import com.ikn.ums.msteams.exception.BusinessException;
import com.ikn.ums.msteams.exception.ErrorCodeMessages;
import com.ikn.ums.msteams.exception.UsersNotFoundException;
import com.ikn.ums.msteams.model.CalendarViewResponseWrapper;
import com.ikn.ums.msteams.model.OnlineMeetingResponseWrapper;
import com.ikn.ums.msteams.model.TranscriptsResponseWrapper;
import com.ikn.ums.msteams.repo.BatchDetailsRepository;
import com.ikn.ums.msteams.repo.EventRepository;
import com.ikn.ums.msteams.service.TeamsSourceDataBatchProcessService;
import com.ikn.ums.msteams.utils.InitializeMicrosoftGraph;
import com.ikn.ums.msteams.utils.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unused")
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
	private RestTemplate rt;

	@Autowired
	private InitializeMicrosoftGraph microsoftGraph;

	private LocalDateTime lastBatchProcessingStartTime;

	private List<List<EventDto>> allUsersEventList;

	private ObjectMapper mapper;
	
	AccessToken acToken = new AccessToken(this.accessToken,OffsetDateTime.now() );
	
	List<EmployeeVO> userDtoList = null;
	
	// constructor
	@Autowired
	public TeamsSourceDataBatchProcessServiceImpl(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	@Transactional
	@Override
	public void performSourceDataBatchProcessing(BatchDetailsDto lastBatchDetails) throws IOException, Exception {

		// get access token from MS teams server , only if existing access token  is expired
		if (this.acToken.isExpired()) {
			log.info("Access Token expired");
			 this.acToken = this.microsoftGraph.initializeMicrosoftGraph();
			 log.info("Access Token Refreshed");
			 this.accessToken = this.acToken.getToken();
		}
	
		//get all employees data from employee microservice to perform batch processing
		//TODO: later the code will be changed to get the active user from UMS Database
		EmployeeListVO empListVO = rt.getForObject("http://UMS-EMPLOYEE-SERVICE/employees/get-all", EmployeeListVO.class);
		
		userDtoList = empListVO.getEmployee();
		
		if (!userDtoList.isEmpty()) {

			//start batch process
			// set current batch processing details
			BatchDetailsDto currentBatchDetailsDto = new BatchDetailsDto();
			BatchDetails batchDetails = new BatchDetails();

			// get current time which is taken as batch processing start date time for
			// current batch process
			LocalDateTime currentbatchStartTime = LocalDateTime.now();

			// save current status of batch process in DB, and this will be latest record in
			// DB
			currentBatchDetailsDto.setStatus("RUNNING");
			currentBatchDetailsDto.setStartDateTime(currentbatchStartTime);

			// map dto to entity
			this.mapper.modelMapper.map(currentBatchDetailsDto, batchDetails);

			// save current batch object
			BatchDetails currentDbBatchDetails = batchDetailsRepository.save(batchDetails);
			log.info("Current batch processing details "+currentDbBatchDetails.toString());
			

			// get last batch processing time
			try {
				if (lastBatchDetails.getStartDateTime() != null) {
					lastBatchProcessingStartTime = lastBatchDetails.getStartDateTime();
				}

				// iterate each user and get their events and save to UMS db
				userDtoList.forEach(userDto -> {
					if (!environment.getProperty("userprincipal.exclude.users")
							.contains(userDto.getEmail())) {

						String userId = userDto.getTeamsUserId();
						// get userprincipalName and pass it to calendarView method to fetch the
						// calendar events if the user
						String userPrincipalName = userDto.getEmail();
						System.out.println(userId + " " + userPrincipalName);

						// get users calendar view of events using principal name
						List<EventDto> calendarEventsDtolist = getUserCalendarView(userDto,
								lastBatchProcessingStartTime);

						// save user events
						saveAllUsersCalendarEvents(calendarEventsDtolist, userDto, currentDbBatchDetails.getBatchId());

					}
				});
				// set current batch processing details, if passed
				currentDbBatchDetails.setStatus("COMPLETED");
				currentDbBatchDetails.setEndDateTime(LocalDateTime.now());
				currentDbBatchDetails.setLastSuccessfullExecutionDateTime(currentbatchStartTime);
				batchDetailsRepository.save(currentDbBatchDetails);
				log.info("Current batch processing details after completion "+currentDbBatchDetails);
			} catch (Exception e) {
				// set current batch processing details, if failed
				currentDbBatchDetails.setStatus("FAILED");
				currentDbBatchDetails.setEndDateTime(LocalDateTime.now());
				//save and flush the changes instantly in db, bcz when exception is raised , 
				//the normal save method will not work to save changes instantly in db, within @Transactional method
				batchDetailsRepository.saveAndFlush(currentDbBatchDetails);
				log.info("Current batch processing details after exception "+currentDbBatchDetails);
				log.info("Exception occured while batch processing in Business layer "+e.getMessage());
				BusinessException umsBusinessException = new BusinessException(ErrorCodeMessages.ERR_UNKNOWN_BATCH_CODE,
						ErrorCodeMessages.ERR_UNKNOWN_BATCH_MSG+" "+e.getMessage());
				throw umsBusinessException;
			}
		} else {
			//TODO: chnage to empoloyees not found exception
			  throw new UsersNotFoundException(ErrorCodeMessages.ERR_EVENTS_NOT_FOUND_BATCH_CODE, 
					  ErrorCodeMessages.ERR_EVENTS_NOT_FOUND_BATCH_MSG);
		}
	}

	private List<EventDto> getUserCalendarView(EmployeeVO userDto, LocalDateTime lastSuccessfulBatchStartTime) {
		// Get the current date in the system's default time zone
		LocalDateTime currentStartDateTime = LocalDateTime.now();

		// Set the datetime to 1 hour ago for the first time execution of batch process
		LocalDateTime dateTimeOneHourAgo = currentStartDateTime.minus(3, ChronoUnit.MINUTES);

		// Convert to UTC
		ZonedDateTime zonedStartDateTime = dateTimeOneHourAgo.atZone(ZoneId.systemDefault());
		ZonedDateTime utcZonedStartDateTime = zonedStartDateTime.withZoneSameInstant(ZoneId.of("UTC"));

		// Extract UTC LocalDateTime
		LocalDateTime dateTimeOneHourAgoUTC = utcZonedStartDateTime.toLocalDateTime();

		// Get the current date in the system's default time zone
		LocalDateTime currentEndDateTime = LocalDateTime.now();

		// Convert to UTC
		ZonedDateTime zonedEndDateTime = currentEndDateTime.atZone(ZoneId.systemDefault());
		ZonedDateTime utcZonedEndDateTime = zonedEndDateTime.withZoneSameInstant(ZoneId.of("UTC"));

		// Extract UTC LocalDateTime
		LocalDateTime currentEndDateTimeUTC = utcZonedEndDateTime.toLocalDateTime();

		LocalDateTime batchStartTimeUTC = null;
		// convert lastsuccessfulbatchtime to UTC
		if (lastSuccessfulBatchStartTime != null) {
			ZonedDateTime zonedLastSuccessfulBatchTime = lastSuccessfulBatchStartTime.atZone(ZoneId.systemDefault());
			ZonedDateTime utcZonedLastSuccessfulBatchTime = zonedLastSuccessfulBatchTime
					.withZoneSameInstant(ZoneId.of("UTC"));

			// Extract UTC LocalDateTime
			batchStartTimeUTC = utcZonedLastSuccessfulBatchTime.toLocalDateTime();
		}

		StringBuilder calendarViewBaseUrl = null;
		if (lastSuccessfulBatchStartTime == null) {
			calendarViewBaseUrl = new StringBuilder("https://graph.microsoft.com/v1.0/users/"
					+ userDto.getEmail() + "/calendarView?startDateTime=" + dateTimeOneHourAgoUTC
					+ "&endDateTime=" + currentEndDateTimeUTC);
		} else {
			calendarViewBaseUrl = new StringBuilder("https://graph.microsoft.com/v1.0/users/"
					+ userDto.getEmail() + "/calendarView?startDateTime=" + batchStartTimeUTC
					+ "&endDateTime=" + currentEndDateTimeUTC);
		}
		// String select =
		// environment.getProperty("calendarview.url.queryparam.select");
		String filter = environment.getProperty("calendarview.url.queryparam.filter");
		String skip = environment.getProperty("calendarview.url.queryparam.skip");

		// append select, filter and skip to calendarViewBaseUrl
		calendarViewBaseUrl = calendarViewBaseUrl.append(filter).append(skip);

		// prepare final URL
		String finalCalendarViewUrl = calendarViewBaseUrl.toString();

		// prepare required http headers for the request
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + this.accessToken);
		headers.add("content-type", "application/json");

		// prepare http entity object with headers
		HttpEntity<String> hentity = new HttpEntity<>(headers);

		// prepare the rest template to hit the graph api calendar view url
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<CalendarViewResponseWrapper> response = restTemplate.exchange(finalCalendarViewUrl,
				HttpMethod.GET, hentity, CalendarViewResponseWrapper.class);

		// get the response (list of calendar event objects)
		CalendarViewResponseWrapper calendarViewWrapper = response.getBody();
		List<EventDto> listCalendarViewDto = calendarViewWrapper.getValue();

		ListIterator<EventDto> iterator = listCalendarViewDto.listIterator();

		// prevent concurrent modifications on events list by using iterator instead of
		// for each loop.
		while (iterator.hasNext()) {
			EventDto eventDto = iterator.next();
			String eventEndTimeUTC = eventDto.getEnd().getDateTime();
			LocalDateTime originalEventEndDateTimeUTC = LocalDateTime.parse(eventEndTimeUTC);
			System.out.println(originalEventEndDateTimeUTC);
			if (originalEventEndDateTimeUTC.compareTo(currentEndDateTimeUTC) > 0) {
				iterator.remove();
			}
		}

		// get user events with its attached online meetings
		// for each event attach corresponding online meeting
		List<EventDto> updateEventsListDto = new ArrayList<>();
		listCalendarViewDto.forEach(eventDto -> {
			System.out.println(eventDto);
			// attach userId and principal name to event
			// eventDto.setUser(userDto);
			System.out.println("Dynamic allocation " + userDto);
			// attach online meeting to event
			EventDto updatedEventWithOnlineMeeting = null;
			EventDto updatedEventWithOnlineMeetingAndTranscript = null;
			
			//get an event's online meeting and transcript, only if the event is an online Event
			if(eventDto.getOnlineMeeting() != null) {
				updatedEventWithOnlineMeeting = attachOnlineMeetingDetailsToEvent(eventDto, userDto);
				updatedEventWithOnlineMeetingAndTranscript = attachTranscriptsToOnlineMeeting(
						updatedEventWithOnlineMeeting, userDto);
				updateEventsListDto.add(updatedEventWithOnlineMeetingAndTranscript);
			}

			// log.debug("Event => "+updatedEventWithOnlineMeetingAndTranscript);
			// display
			System.out.println("Event => " + updatedEventWithOnlineMeetingAndTranscript);
		});
		return updateEventsListDto;
	}

	// attach online meeting to event, so that event will now contain its online
	// meeting details
	private EventDto attachOnlineMeetingDetailsToEvent(EventDto eventDto, EmployeeVO user) {

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
				if(eventDto.getOccurrenceId()!=null) {
					eventDto.getOnlineMeeting().setOccurrenceId(eventDto.getOccurrenceId());
				}
			}
		}
		return eventDto;
	}

	// attach transcripts to respective online meetings
	private EventDto attachTranscriptsToOnlineMeeting(EventDto eventDto, EmployeeVO user) {

//		if (eventDto.getOnlineMeeting() != null) {
//			eventDto.getOnlineMeeting().setOccurrenceId(eventDto.getOccurrenceId());
//		}

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
							String transcriptContent = getTranscriptContent(transcriptDto.getTranscriptContentUrl());

							// write transcript into file
							try (FileWriter fileWriter = new FileWriter(
									"Transcript " + transcriptDto.getTranscriptId())) {
								fileWriter.write(transcriptContent);
								System.out.println("Transcript content has been written to the file.");

								// save file loc to db
								// Get the file path
								File file = new File("Transcript " + transcriptDto.getTranscriptId());
								String filePath = file.getAbsolutePath();
								System.out.println("File path: " + filePath);
								if (!filePath.equalsIgnoreCase("")) {

									// set transcript file path
									transcriptDto.setTranscriptFilePath(filePath);
									transcriptDto.setTranscriptContent(transcriptContent);
									//set transcript content into db column
									//byte[] transcriptContentBytes = transcriptContent.getBytes("UTF-8");
									//transcriptDto.setTranscriptContent(Arrays.toString(transcriptContentBytes));
									//System.out.println(Arrays.toString(transcriptContentBytes));
								}

							} catch (IOException e) {
								e.printStackTrace();
								throw new RuntimeException(e);
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
					String transcriptContent = getTranscriptContent(transcript.getTranscriptContentUrl());

					// write transcript into file
					try (FileWriter fileWriter = new FileWriter("Transcript " + transcript.getTranscriptId())) {
						fileWriter.write(transcriptContent);
						System.out.println("Transcript content has been written to the file.");
						
						// save file loc to db
						// Get the file path
						File file = new File("Transcript-" + transcript.getTranscriptId());
						String filePath = file.getPath();
						System.out.println("File path: " + filePath);
						if (!filePath.equalsIgnoreCase("")) {
							// set transcript file path
							transcript.setTranscriptFilePath(filePath);
							//byte[] transcriptContentBytes = transcriptContent.getBytes();
							//String str = org.apache.commons.codec.binary.Base64.encodeBase64String(transcriptContentBytes);
							transcript.setTranscriptContent(transcriptContent);
							//System.out.println(Arrays.toString(transcriptContentBytes));
						}

					} catch (IOException e) {
						e.printStackTrace();
						throw new RuntimeException(e);

					}
				});
				// set transcripts to online meeting of the event
				eventDto.getOnlineMeeting().setMeetingTranscripts(transcriptsListDto);
			}
		}
		return eventDto;
	}

	// map all event dto's to event entities to save into db
	private void saveAllUsersCalendarEvents(List<EventDto> eventsListDto, EmployeeVO user, Long currentBatchProcessId) {
		eventsListDto.forEach(eventDto -> {
			// iterate through event objects and get the online meeting object from each
			// event object one by one
			List<Event> eventEntities = new ArrayList<>();
			Event event = new Event();

			// map eventDto to event and add all the events to list
			mapper.modelMapper.map(eventDto, event);

			// map manual property entries
			event.setOnlineMeetingId(eventDto.getOnlineMeeting().getOnlineMeetingId());
			event.setStartDateTime(LocalDateTime.parse(eventDto.getStart().getDateTime()));
			event.setStartTimeZone(eventDto.getStart().getTimeZone());
			event.setEndDateTime(LocalDateTime.parse(eventDto.getEnd().getDateTime()));
			event.setEndTimeZone(eventDto.getEnd().getTimeZone());
			event.setLocation(eventDto.getLocation().getDisplayName());
			event.setOrganizerEmailId(eventDto.getOrganizer().getEmailAddress().getAddress());
			event.setOrganizerName(eventDto.getOrganizer().getEmailAddress().getName());
			event.setJoinUrl(eventDto.getOnlineMeeting().getJoinUrl());
			event.setEmailId(user.getEmail());
			List<TranscriptDto> retrivedTranscriptDtos = eventDto.getOnlineMeeting().getMeetingTranscripts();
			List<Transcript> transcripts = new ArrayList<>();
			if (retrivedTranscriptDtos != null) {
				retrivedTranscriptDtos.forEach(transcriptDto -> {
					Transcript t = new Transcript();
					mapper.modelMapper.map(transcriptDto, t);
					transcripts.add(t);
				});
				// set transcripts to event
				event.setMeetingTranscripts(transcripts);
			}
			// set attendees to event
			Set<Attendee> attendeesList = new HashSet<>();
			List<String> mailIds = new ArrayList<>();
			eventDto.getAttendees().forEach(attendeeDto -> {
				Attendee attendee = new Attendee();
				String attendeeEmailAddress = attendeeDto.getEmailAddress().getAddress();
				// map dto to entity
				mapper.modelMapper.map(attendeeDto, attendee);
				// manually map the unmatched field
				attendee.setEmail(attendeeEmailAddress);
				attendee.setStatus(attendeeDto.getStatus().getResponse());
				//attendee mail ids list
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
						//attendee.setUser(userProfilesList.get(i));
						attendee.setEmailId(userProfilesList.get(i).getEmail());					
					}
					/*
					if(event.getOrganizerEmailId().equalsIgnoreCase(userProfilesList.get(i).getEmail())) {
						Attendee attendee = new Atte
						attendee.setUserId(userProfilesList.get(i).getId());
					}
					*/
				}
			});

			event.setAttendees(attendeesList);
			event.setBatchId(currentBatchProcessId);
			// logic for isOnlinemeeting based on join url (optional)
			eventEntities.add(event);

			// finally save all event entities of the user in UMS DB,
			// now all the events contain (its online meeting Id and transcript details if
			// any)
			List<Event> eventsList = eventRepository.saveAll(eventEntities);
		});
	}

	private OnlineMeetingDto getOnlineMeeting(String joinWebUrl, String userId) {
		// prepare url to get online meeting object
		StringBuilder stringBuilder = new StringBuilder("https://graph.microsoft.com/v1.0/users/" + userId
				+ "/onlineMeetings?$filter=joinWebUrl eq '" + joinWebUrl + "'");
		String finalUrl = stringBuilder.toString();

		// prepare Http headers required for the request
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + this.accessToken);
		headers.add("content-type", "application/json");

		// prepare http entity with headers
		HttpEntity<String> hentity = new HttpEntity<>(headers);

		// prepare rest template and hit meeting end point
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<OnlineMeetingResponseWrapper> response = restTemplate.exchange(finalUrl, HttpMethod.GET, hentity,
				OnlineMeetingResponseWrapper.class);
		List<OnlineMeetingDto> onlineMeetingsList = response.getBody().getValue();

		// however there will be only single meeting object, even if it returns a list,
		// get the 0th index from list which will give the meeting object
		return onlineMeetingsList.get(0);
	}

	private List<TranscriptDto> getOnlineMeetingTranscriptDetails(String userId, String onlineMeetingId) {
		List<TranscriptDto> meetingTranscriptsList = new ArrayList<>();
		StringBuilder stringBuilder = new StringBuilder("https://graph.microsoft.com/beta/users/" + userId
				+ "/onlineMeetings('" + onlineMeetingId + "')/transcripts");
		String url = stringBuilder.toString();

		// prepare headers for the request
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + this.accessToken);
		headers.add("content-type", "application/json");

		// prepare http entity object
		HttpEntity<String> hentity = new HttpEntity<>(headers);

		// prepare rest template and hit transcript end point to fetch transcripts for
		// the meeting
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<TranscriptsResponseWrapper> response = restTemplate.exchange(url, HttpMethod.GET, hentity,
				TranscriptsResponseWrapper.class);

		// check whether transcript are present for the meeting, the odataCount gives
		// the count of transcripts available for the meeting.
		// if odataCount is 0, i.e there is no transcript available for the meeting
		if (response.getBody().getOdataCount() > 0) {
			meetingTranscriptsList = response.getBody().getValue();
			return meetingTranscriptsList;
		} else {
			// return empty meeting transcripts list and handle it near the caller of this
			// method
			return meetingTranscriptsList;
		}
	}
	
	@Override
	public BatchDetailsDto getLatestSourceDataBatchProcessingRecordDetails() {
		Optional<BatchDetails> optBatchDetails = batchDetailsRepository.getLatestBatchProcessingRecord();
		BatchDetails latestBatchDetails = null;
		BatchDetailsDto latestBatchDetailsDto = null;
		if (optBatchDetails.isPresent()) {
			latestBatchDetails = optBatchDetails.get();
			latestBatchDetailsDto = new BatchDetailsDto();

			// map entity to dto
			this.mapper.modelMapper.map(latestBatchDetails, latestBatchDetailsDto);
			return latestBatchDetailsDto;
		}
		//for the first time there will not be any latest batch process record, 
		//so just return a dummy batch process object with status as completed.
		latestBatchDetailsDto = new BatchDetailsDto();
		latestBatchDetailsDto.setStatus("COMPLETED");
		return latestBatchDetailsDto;
	}

	// will pass the transcript content URL to this method from the transcript
	// details object.
	private String getTranscriptContent(String transcriptContentUrl) {
		StringBuilder stringBuilder = new StringBuilder(transcriptContentUrl);
		String url = stringBuilder.toString();

		// prepare headers for the request
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + this.accessToken);
		headers.add("Accept", "text/vtt");
		headers.setContentType(MediaType.TEXT_PLAIN);

		// prepare http entity object with headers
		HttpEntity<String> hentity = new HttpEntity<>(headers);

		// prepare rest template and hit meeting end point to fetch transcript content
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> rentity = restTemplate.exchange(url, HttpMethod.GET, hentity, String.class);

		// body of response contains the transcript content
		return rentity.getBody();
	}

	
}//class
