package com.ikn.ums.dto;

import lombok.Data;

@Data
public class AttendeeDto {
	
	private String type;

	private StartDto status;
	
	private EmailAddressDto emailAddress;

}
