package com.ikn.ums.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class TranscriptDto {
	
	@JsonProperty("id")
	private String transcriptId;
	private String meetingId;
	private String meetingOrganizerId;
	private String transcriptContentUrl;
	private String createdDateTime;
	private String transcriptFilePath;

	
}
