package com.ikn.ums.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ikn.ums.entity.UserProfile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnlineMeetingDto {
	
	@JsonProperty("id")
	private String onlineMeetingId;
	private String subject;
	private String joinUrl;
	private String occurrenceId;
	private String onlineMeetingType;
	private List<TranscriptDto> meetingTranscripts;
	
}
