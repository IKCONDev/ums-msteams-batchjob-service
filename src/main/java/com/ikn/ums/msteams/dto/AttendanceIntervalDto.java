package com.ikn.ums.msteams.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceIntervalDto {
	@JsonProperty("joinDateTime")
	private String joinDateTime;
	@JsonProperty("leaveDateTime")
	private String leaveDateTime;
	@JsonProperty("durationInSeconds")
	private int attendeeDurationInSeconds;

}
