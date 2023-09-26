package com.ikn.ums.msteams.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventDto {
	
	    @JsonProperty("id")
		private String eventId;
	      
	    @JsonProperty("createdDateTime")
		private String createdDateTime;
	    
	    @JsonProperty("originalStartTimeZone")
		private String originalStartTimeZone;
		
	    @JsonProperty("originalEndTimeZone")
		private String originalEndTimeZone;
		
	    @JsonProperty("subject")
		private String subject;

	    @JsonProperty("type")
		private String type;
		
		@JsonProperty("occurrenceId")
		private String occurrenceId;
		
		@JsonProperty("start")
		private StartDto start;
		
		@JsonProperty("end")
		private EndDto end;
		
		@JsonProperty("location")
		private LocationDto location;
		
		@JsonProperty("attendees")
		private List<AttendeeDto> attendees;
		
		@JsonProperty("organizer")
		private OrganizerDto organizer;
		
		@JsonProperty("onlineMeeting")
		private OnlineMeetingDto onlineMeeting;
		
		@JsonProperty("onlineMeetingProvider")
		private String onlineMeetingProvider;
		
	    //private RecurrenceDto recurrence;
	    
		@JsonProperty("seriesMasterId")
	    private String seriesMasterId;
	    
	    private String insertedBy = "IKCON UMS";
	    
	    private String insertedDate = LocalDateTime.now().toString();
	    
	    private UserProfileDto user;
	    
	
}
