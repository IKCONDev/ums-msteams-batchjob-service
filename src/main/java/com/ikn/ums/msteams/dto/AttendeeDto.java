package com.ikn.ums.msteams.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendeeDto {
	
	@JsonProperty("type")
	private String type;
	@JsonProperty("status")
	private StatusDto status;
	@JsonProperty("emailAddress")
	private EmailAddressDto emailAddress;
	
}
