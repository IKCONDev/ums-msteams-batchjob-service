package com.ikn.ums.dto;

import com.ikn.ums.entity.UserProfile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatternDto {
	
	private String type;
	private int interval;
	private int month;
	private int dayOfMonth;
	private String firstdayOfWeek;
	private String index;
	private String[] daysOfWeek;

}
