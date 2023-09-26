package com.ikn.ums.msteams.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EndDto {
	
	@JsonProperty("timezone")
	private String timeZone;
	@JsonProperty("dateTime")
	private String dateTime;
	
}

