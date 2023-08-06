package com.ikn.ums.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class EventDto {
	
	    @JsonProperty("id")
		private String eventId;
	    
	    private String userId;
	    
	    private String userPrinicipalName;
	    
	    @JsonProperty("createdDateTime")
		private String eventCreatedDateTime;
	    
		private String originalStartTimeZone;
		
		private String originalEndTimeZone;
		
		private String subject;

		private String type;
		
		@JsonProperty("occurrenceId")
		private String occurrenceId;
		
		private StartDto start;
		
		private EndDto end;
		
		private LocationDto location;
		
		private List<AttendeeDto> attendees;
		//@JsonProperty("organizer")
		private OrganizerDto organizer;
		
		private OnlineMeetingDto onlineMeeting;
		
		private String onlineMeetingProvider;
		
	    //private RecurrenceDto recurrence;
	    
	    private String seriesMasterId;
	    
	    private String insertedBy = "IKCON UMS";
	    
	    private String insertedDate = LocalDateTime.now().toString();
	    
	
}
