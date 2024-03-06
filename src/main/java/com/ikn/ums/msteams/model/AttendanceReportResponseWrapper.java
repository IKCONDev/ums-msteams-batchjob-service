package com.ikn.ums.msteams.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ikn.ums.msteams.dto.AttendanceReportDto;

import lombok.Data;

@Data
public class AttendanceReportResponseWrapper {
	
	@JsonProperty("@odata.context")
	private String odataContext;
	@JsonProperty("value")
	private List<AttendanceReportDto> value;

}
