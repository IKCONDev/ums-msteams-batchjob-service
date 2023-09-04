package com.ikn.ums.msteams.dto;

import com.ikn.ums.msteams.entity.UserProfile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationDto {
	
	private String displayName;
	private String locationType;
	private String uniquerId;
	private String uniqueIdType;
	
}
