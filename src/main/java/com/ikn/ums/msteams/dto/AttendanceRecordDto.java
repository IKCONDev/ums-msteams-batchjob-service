package com.ikn.ums.msteams.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceRecordDto {

	//@JsonProperty("id")
	//private String AttendanceId;
	@JsonProperty("emailAddress")
	private String emailAddress;
	@JsonProperty("totalAttendanceInSeconds")
	private int totalAttendanceInSeconds;
	@JsonProperty("role")
	private String meetingRole;
	@JsonProperty("attendanceIntervals")
	private List<AttendanceIntervalDto> attendanceIntervals;
	
}
