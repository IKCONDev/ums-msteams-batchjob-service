package com.ikn.ums.msteams.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailAddressDto {
	
	@JsonProperty("name")
	private String name;
	@JsonProperty("address")
	private String address;

	
}
