package com.ikn.ums.msteams.dto;

import com.ikn.ums.msteams.entity.UserProfile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecurrenceDto {
	
	private PatternDto pattern;
	private RangeDto range;


}
