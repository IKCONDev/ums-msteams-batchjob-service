package com.ikn.ums.msteams.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ikn.ums.msteams.dto.UserProfileDto;

import lombok.Data;

@Data
public class UserProfilesResponseWrapper {
	
	@JsonProperty("odata.context")
	private String odataContext;
	
	private List<UserProfileDto> value;


}
