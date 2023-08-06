package com.ikn.ums.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ikn.ums.dto.OnlineMeetingDto;

import lombok.Data;

@Data
public class OnlineMeetingResponseWrapper {
	
	@JsonProperty("odata.context")
	private String odataContext;
	
	private List<OnlineMeetingDto> value;

}
