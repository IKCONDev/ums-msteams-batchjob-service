package com.ikn.ums.msteams.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StartDto {
	
	@JsonProperty("timeZone")
	private String timeZone;
	@JsonProperty("dateTime")
	private String dateTime;
	

}
