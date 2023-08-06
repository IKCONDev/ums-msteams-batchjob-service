package com.ikn.ums.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ikn.ums.dto.TranscriptDto;

import lombok.Data;

@Data
public class TranscriptsResponseWrapper {
	
	@JsonProperty("@odata.context")
	private String odataContext;
	
	@JsonProperty("@odata.count")
	private Integer odataCount;
	
	private List<TranscriptDto> value;
}
