package com.ikn.ums.dto;

import com.ikn.ums.entity.UserProfile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RangeDto {
	
	private String type;
	private String startDate;
	private String endDate;
	private String recurrenceTimeZone;
	private String numberOfOccurrences;
	
}
