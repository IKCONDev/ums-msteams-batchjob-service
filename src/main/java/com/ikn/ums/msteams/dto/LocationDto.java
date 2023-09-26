package com.ikn.ums.msteams.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationDto {
	
	@JsonProperty("displayName")
	private String displayName;
	@JsonProperty("locationType")
	private String locationType;
	@JsonProperty("uniqueId")
	private String uniqueId;
	@JsonProperty("uniqueIdType")
	private String uniqueIdType;
	
}
