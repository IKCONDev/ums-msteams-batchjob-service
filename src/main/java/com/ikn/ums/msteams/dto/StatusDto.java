package com.ikn.ums.msteams.dto;

import com.ikn.ums.msteams.entity.UserProfile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusDto {
	
	private String response;
	private String time;

}
