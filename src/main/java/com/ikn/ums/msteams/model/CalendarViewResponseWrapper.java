package com.ikn.ums.msteams.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ikn.ums.msteams.dto.EventDto;

import lombok.Data;

@Data
public class CalendarViewResponseWrapper {
	
    @JsonProperty("@odata.context")
    private String odataContext;
    
    @JsonProperty("value")
    private List<EventDto> value;

	public String getOdataContext() {
		return odataContext;
	}

}

