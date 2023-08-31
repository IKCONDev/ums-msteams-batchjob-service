package com.ikn.ums.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ikn.ums.entity.UserProfile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TranscriptDto {
	
	@JsonProperty("id")
	private String transcriptId;
	private String meetingId;
	private String meetingOrganizerId;
	private String transcriptContentUrl;
	private String createdDateTime;
	private String transcriptFilePath;

	
}
