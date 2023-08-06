package com.ikn.ums.dto;

import lombok.Data;

@Data
public class PatternDto {
	
	private String type;
	private int interval;
	private int month;
	private int dayOfMonth;
	private String firstdayOfWeek;
	private String index;
	private String[] daysOfWeek;

}
