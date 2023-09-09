package com.ikn.ums.msteams.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ikn.ums.msteams.VO.ActionItemsListVO;
import com.ikn.ums.msteams.VO.ActionsItemsVO;
import com.ikn.ums.msteams.entity.Attendee;
import com.ikn.ums.msteams.entity.Event;
import com.ikn.ums.msteams.repo.EventRepository;
import com.ikn.ums.msteams.service.EventService;

@Service
public class EventServiceImpl implements EventService {
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private EventRepository eventRepository;
	
	@Override
	public Integer getUserOrganizedEventCount(String email) {
		Integer count = eventRepository.findUserOrganizedEventCount(email);
		return count;
	}

	@Override
	public Integer getUserAttendedEventsCount(Integer userId) {
		Integer attendedMeetingsCount = eventRepository.findUserAttendedEventCount(userId);
		return attendedMeetingsCount;
	}

	
	 // get events of a single user
	  
	  @Override 
	  public List<Event> getEventByUserPrincipalName(String userPrincipalName) throws Exception {
	  
	  //check whether the user exists or not int count =
	  List<Event> dbEventsList = eventRepository.findUserEvents(userPrincipalName);
	  
	  dbEventsList.forEach(dbEvent -> {
		  dbEvent.getMeetingTranscripts().forEach(transcript->{
			  String transcriptContentByteArrayString = transcript.getTranscriptContent();			  
			  byte[] bytes = org.apache.commons.codec.binary.Base64.decodeBase64(transcriptContentByteArrayString);
			  transcript.setTranscriptContent(new String(bytes));
			  
		  });	   
	  });
	  return dbEventsList; 
	 }
	  
	  
		@Override
		public List<Attendee> getUserAttendedEvents(Integer userId) {
			List<Attendee> userAttendedEvents = null;
			List<Attendee> dbUserAttendedEvents = eventRepository.findUserAttendedEvents(userId);
			if(dbUserAttendedEvents.size()<0) {
				userAttendedEvents = new ArrayList<>();
				return userAttendedEvents;
			}
			return dbUserAttendedEvents;
		}

		@Override
		public List<ActionsItemsVO> getActionItemsOfEvent(Integer eventId) {
			ActionItemsListVO response = restTemplate.getForObject("http://UMS-ACTIONITEMS-SERVICE/api/actions/ac-items/"+eventId
					,ActionItemsListVO.class);
			List<ActionsItemsVO> actionItemsListOfEvent = response.getActionItems(); 
			return actionItemsListOfEvent;
		}
		
		@Override
		public List<ActionsItemsVO> getActionItems() {
			ActionItemsListVO response = restTemplate.getForObject("http://UMS-ACTIONITEMS-SERVICE/api/actions/ac-items/"
					,ActionItemsListVO.class);
			List<ActionsItemsVO> actionItemsListOfEvent = response.getActionItems(); 
			return actionItemsListOfEvent;
		}
		
}
