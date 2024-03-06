package com.ikn.ums.msteams.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceReportDto {
	
	@JsonProperty("@odata.context")
	private String odataContext;
	@JsonProperty("id")
	private String attendanceReportId;
	@JsonProperty("totalParticipantCount")
	private String totalParticipantCount;
	@JsonProperty("meetingStartDateTime")
	private String meetingStartDateTime;
	@JsonProperty("meetingEndDateTime")
	private String meetingEndDateTime;
	@JsonProperty("attendanceRecords")
	private List<AttendanceRecordDto> attendanceRecords;
	
	
}
