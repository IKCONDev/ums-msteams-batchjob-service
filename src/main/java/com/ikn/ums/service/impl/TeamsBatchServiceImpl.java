package com.ikn.ums.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties.MatchingStrategy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.ikn.ums.dto.EventDto;
import com.ikn.ums.dto.OnlineMeetingDto;
import com.ikn.ums.dto.TranscriptDto;
import com.ikn.ums.dto.UserProfileDto;
import com.ikn.ums.entity.Event;
import com.ikn.ums.entity.Transcript;
import com.ikn.ums.model.CalendarViewResponseWrapper;
import com.ikn.ums.model.OnlineMeetingResponseWrapper;
import com.ikn.ums.model.TranscriptsResponseWrapper;
import com.ikn.ums.model.UserProfilesResponseWrapper;
import com.ikn.ums.repo.EventRepository;
import com.ikn.ums.service.ITeamsBatchService;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.models.User;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.EventCollectionPage;
import com.microsoft.graph.requests.GraphServiceClient;
import com.microsoft.graph.requests.UserRequestBuilder;

import javax.transaction.Transactional;
import okhttp3.Request;

@SuppressWarnings("unused")
@Service
public class TeamsBatchServiceImpl implements ITeamsBatchService {

	// private static final Logger logger =
	// LoggerFactory.getLogger(TeamsBatchServiceImpl.class);

	@Autowired
	private Environment environment;
	private ClientSecretCredential clientSecretCredential;
	private GraphServiceClient<Request> _graphServiceClient;
	private String accessToken = null;

	@Autowired
	private EventRepository eventRepository;

	// initialize Microsoft graph API and get access token
	@Override
	public String initializeMicrosoftGraph() {
		if (clientSecretCredential == null) {
			final String clientId = environment.getProperty("app.clientId");
			final String clientSecret = environment.getProperty("app.clientSecret");
			final String tenantId = environment.getProperty("app.tenantId");
			this.clientSecretCredential = new ClientSecretCredentialBuilder().clientId(clientId).tenantId(tenantId)
					.clientSecret(clientSecret).build();
		}
		final TokenCredentialAuthProvider authProvider = new TokenCredentialAuthProvider(
				List.of("https://graph.microsoft.com/.default"), clientSecretCredential);
		this._graphServiceClient = GraphServiceClient.builder().authenticationProvider(authProvider).buildClient();
		return this.accessToken = getAccessToken();
	}

	// helper method
	private String getAccessToken() {
		final String[] graphscopes = new String[] { "https://graph.microsoft.com/.default" };
		final TokenRequestContext context = new TokenRequestContext();
		context.addScopes(graphscopes);
		final AccessToken token = this.clientSecretCredential.getToken(context).block();
		return token.getToken();
	}

	@Transactional
	@SuppressWarnings("rawtypes")
	@Override
	public void performBatchProcessing() {
		String userProfileUrl = "https://graph.microsoft.com/v1.0/users?$filter=accountEnabled eq true and userType eq 'Member'";
		List<Object> onlineMeetingList = new ArrayList<>();

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

		// iterate each user and get their events and save to UMS db
		userDtoList.forEach(userDto -> {
			if(!userDto.getUserPrincipalName().equalsIgnoreCase("hr@ikcontech.com") &&
					!userDto.getUserPrincipalName().equalsIgnoreCase("Uday.Jakkula@ikcontech.com")&&
					!userDto.getUserPrincipalName().equalsIgnoreCase("Saisubrahmanyam@ikcontech.com")&&
					!userDto.getUserPrincipalName().equalsIgnoreCase("admin@ikcontech.com")&&
					!userDto.getUserPrincipalName().equalsIgnoreCase("r.battula@ikcontech.com")&&
					!userDto.getUserPrincipalName().equalsIgnoreCase("deepthi.a@ikcontech.com")&&
					!userDto.getUserPrincipalName().equalsIgnoreCase("Lead2@ikcontech.com")&&
					!userDto.getUserPrincipalName().equalsIgnoreCase("Parmeena.Gopalakrishnan@ikcontech.com")){
				
				String userId = userDto.getUserId();
				// get userprincipalName and pass it to calendarView method to fetch the
				// calendar events if the user
				String userPrincipalName = userDto.getUserPrincipalName();
				System.out.println(userId + " " + userPrincipalName);

				// get users calendar view of events using principal name
				List<EventDto> calendarEventsDtolist = getUserCalendarView(userPrincipalName);

				// iterate through event objects and get the online meeting object from each
				// event object one by one
				List<Event> eventEntities = new ArrayList<>();

				// iterate eventDto and convert them into event enties and save the entities
				// into db
				calendarEventsDtolist.forEach(eventDto -> {
					eventDto.setUserId(userId);
					eventDto.setUserPrinicipalName(userPrincipalName);

					System.out.println("Event => " + eventDto);

					// if joinurl is not null, then the event is an online meeting, proceed to get
					// and insert online meeting object
					if (eventDto.getOnlineMeeting().getJoinUrl() != null
							|| eventDto.getOnlineMeeting().getJoinUrl() != "") {
						String meetingJoinUrl = eventDto.getOnlineMeeting().getJoinUrl();

						// get online meeting objects of the user one by one and attach to event
						OnlineMeetingDto onlineMeeting = getOnlineMeeting(meetingJoinUrl, userId);
						if (onlineMeeting != null) {
							eventDto.getOnlineMeeting().setOnlineMeetingId(onlineMeeting.getOnlineMeetingId());
							eventDto.getOnlineMeeting().setSubject(onlineMeeting.getSubject());
							eventDto.getOnlineMeeting().setOnlineMeetingType(eventDto.getType());
							if (eventDto.getOccurrenceId() != null || eventDto.getOccurrenceId() != "") {
								eventDto.getOnlineMeeting().setOccurrenceId(eventDto.getOccurrenceId());
							}

							// Retrieve transcripts of particular instance of the recurring meeting and
							// insert into db
							if (eventDto.getOnlineMeeting().getOccurrenceId() != null) {
								List<TranscriptDto> transcriptsListDto = getOnlineMeetingTranscriptDetails(userId,
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
												String transcriptContent = getTranscriptContent(
														transcriptDto.getTranscriptContentUrl());

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
													}

												} catch (IOException e) {
													e.printStackTrace();
												}
											});
											eventDto.getOnlineMeeting()
													.setMeetingTranscripts(actualMeetingTranscriptsListDto);
										}

									}
								});
							} // if
							else {

								// Retrieve transcripts of single instance meeting and insert into db
								List<TranscriptDto> transcriptsListDto = getOnlineMeetingTranscriptDetails(userId,
										eventDto.getOnlineMeeting().getOnlineMeetingId());
								if (transcriptsListDto != null) {
									// eventDto.getOnlineMeeting().setMeetingTranscripts(transcriptsListDto);
									transcriptsListDto.forEach(transcript -> {
										String transcriptContent = getTranscriptContent(
												transcript.getTranscriptContentUrl());

										// write transcript into file
										try (FileWriter fileWriter = new FileWriter(
												"Transcript " + transcript.getTranscriptId())) {
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
											}

										} catch (IOException e) {
											e.printStackTrace();
										}
									});
									eventDto.getOnlineMeeting().setMeetingTranscripts(transcriptsListDto);
								}
							} // else
						}

						Event event = new Event();

						// map eventDto to event and add all the events to list
						ModelMapper mapper = new ModelMapper();
						mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
						mapper.map(eventDto, event);
						/// logic for isOnlinemeeting based on join url (optional)
						eventEntities.add(event);

						// finally save all event entities of the user in UMS DB
						List<Event> eventsList = eventRepository.saveAll(eventEntities);

					}
				});
			}
		});
	}

	@Transactional
	@SuppressWarnings("rawtypes")
	@Override
	public void performSingleUserBatchProcessing(String upn) {
		String userProfileUrl = "https://graph.microsoft.com/v1.0/users/" + upn;
		List<Object> onlineMeetingList = new ArrayList<>();

		// prepare headers
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Authorization", "Bearer " + this.accessToken);
		httpHeaders.add("content-type", "application/json");

		// prepare http entity with headers
		HttpEntity httpEntity = new HttpEntity<>(httpHeaders);

		// prepare the rest template and hit the graph api user end point
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<UserProfileDto> userProfilesResponse = restTemplate.exchange(userProfileUrl, HttpMethod.GET,
				httpEntity, UserProfileDto.class);

		// get all user profiles from reponse object
		UserProfileDto userDto = userProfilesResponse.getBody();

		// iterate each user and get their events and save to UMS db
		String userId = userDto.getUserId();
		// get userprincipalName and pass it to calendarView method to fetch the
		// calendar events if the user
		String userPrincipalName = userDto.getUserPrincipalName();
		System.out.println(userId + " " + userPrincipalName);

		// get users calendar view of events using principal name
		List<EventDto> calendarEventsDtolist = getUserCalendarView(userPrincipalName);

		// iterate through event objects and get the online meeting object from each
		// event object one by one
		List<Event> eventEntities = new ArrayList<>();

		// iterate eventDto and convert them into event enties and save the entities
		// into db
		calendarEventsDtolist.forEach(eventDto -> {
			eventDto.setUserId(userId);
			eventDto.setUserPrinicipalName(userPrincipalName);

			System.out.println("Event => " + eventDto);

			// if joinurl is not null, then the event is an online meeting, proceed to get
			// and insert online meeting object
			if (eventDto.getOnlineMeeting().getJoinUrl() != null || eventDto.getOnlineMeeting().getJoinUrl() != "") {
				String meetingJoinUrl = eventDto.getOnlineMeeting().getJoinUrl();

				// get online meeting objects of the user one by one and attach to event
				OnlineMeetingDto onlineMeeting = getOnlineMeeting(meetingJoinUrl, userId);
				if (onlineMeeting != null) {
					eventDto.getOnlineMeeting().setOnlineMeetingId(onlineMeeting.getOnlineMeetingId());
					eventDto.getOnlineMeeting().setSubject(onlineMeeting.getSubject());
					eventDto.getOnlineMeeting().setOnlineMeetingType(eventDto.getType());
					if (eventDto.getOccurrenceId() != null || eventDto.getOccurrenceId() != "") {
						eventDto.getOnlineMeeting().setOccurrenceId(eventDto.getOccurrenceId());
					}

					// Retrieve transcripts of particular instance of the recurring meeting and
					// insert into db
					if (eventDto.getOnlineMeeting().getOccurrenceId() != null) {
						List<TranscriptDto> transcriptsListDto = getOnlineMeetingTranscriptDetails(userId,
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
										String transcriptContent = getTranscriptContent(
												transcriptDto.getTranscriptContentUrl());

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
											}

										} catch (IOException e) {
											e.printStackTrace();
										}
									});
									eventDto.getOnlineMeeting().setMeetingTranscripts(actualMeetingTranscriptsListDto);
								}

							}
						});
					} // if
					else {

						// Retrieve transcripts of single instance meeting and insert into db
						List<TranscriptDto> transcriptsListDto = getOnlineMeetingTranscriptDetails(userId,
								eventDto.getOnlineMeeting().getOnlineMeetingId());
						if (transcriptsListDto != null) {
							// eventDto.getOnlineMeeting().setMeetingTranscripts(transcriptsListDto);
							transcriptsListDto.forEach(transcript -> {
								String transcriptContent = getTranscriptContent(transcript.getTranscriptContentUrl());

								// write transcript into file
								try (FileWriter fileWriter = new FileWriter(
										"Transcript " + transcript.getTranscriptId())) {
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
									}

								} catch (IOException e) {
									e.printStackTrace();
								}
							});
							eventDto.getOnlineMeeting().setMeetingTranscripts(transcriptsListDto);
						}
					} // else
				}

				Event event = new Event();

				// map eventDto to event and add all the events to list
				ModelMapper mapper = new ModelMapper();
				mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
				mapper.map(eventDto, event);
				/// logic for isOnlinemeeting based on join url (optional)
				eventEntities.add(event);

				// finally save all event entities of the user in UMS DB
				List<Event> eventsList = eventRepository.saveAll(eventEntities);

			}
		});
	}

	private List<EventDto> getUserCalendarView(String userPrincipalName) {
		// Get the current date in the system's default time zone
		LocalDate currentStartDate = LocalDate.now();

		// Set the time to 12:00 AM (midnight)
		LocalDateTime startDateTime = currentStartDate.atTime(LocalTime.MIDNIGHT);

		// Convert to UTC or any specific time zone if needed
		// LocalDateTime startDateTimeUTC =
		// startDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();

		// Get the current date in the system's default time zone
		LocalDate currentEndDate = LocalDate.now();

		// Set the time to 11:59:59 PM (23:59:59)
		LocalDateTime endDateTime = currentEndDate.atTime(23, 59, 59);

		// Convert to UTC or any specific time zone if needed
		// LocalDateTime lastSecondOfDayUTC =
		// lastSecondOfDay.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();

		StringBuilder calendarViewBaseUrl = new StringBuilder("https://graph.microsoft.com/v1.0/users/"
				+ userPrincipalName + "/calendarView?startDateTime=" + startDateTime + "&endDateTime=" + endDateTime);
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
		List<EventDto> listCalendarView = calendarViewWrapper.getValue();
		return listCalendarView;
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

	// get events of a particular user
	@Override
	public List<EventDto> getEventByUserPrincipalName(String userPrincipalName) {
		List<Event> eventsList = eventRepository.findByUserPrinicipalName(userPrincipalName);
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		List<EventDto> eventsListDto = new ArrayList<>();
		eventsList.forEach(event -> {
			EventDto eventDto = new EventDto();
			mapper.map(event, eventDto);
			eventsListDto.add(eventDto);
		});
		return eventsListDto;
	}
}
