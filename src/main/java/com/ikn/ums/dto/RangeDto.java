package com.ikn.ums.dto;

import lombok.Data;

@Data
public class RangeDto {
	
	private String type;
	private String startDate;
	private String endDate;
	private String recurrenceTimeZone;
	private String numberOfOccurrences;
	
}
