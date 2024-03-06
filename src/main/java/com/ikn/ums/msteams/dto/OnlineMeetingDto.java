package com.ikn.ums.msteams.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OnlineMeetingDto {
	
	@JsonProperty("id")
	private String onlineMeetingId;
	@JsonProperty("subject")
	private String subject;
	@JsonProperty("joinUrl")
	private String joinUrl;
	@JsonProperty("occurenceId")
	private String occurrenceId;
	@JsonProperty("onlineMeetingType")
	private String onlineMeetingType;
	@JsonProperty("meetingTranscripts")
	private List<TranscriptDto> meetingTranscripts;
	private List<AttendanceReportDto> attendanceReport;
	
}
