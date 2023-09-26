package com.ikn.ums.msteams.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TranscriptDto {
	
	@JsonProperty("id")
	private String transcriptId;
	@JsonProperty("meetingId")
	private String meetingId;
	@JsonProperty("meetingOrganizerId")
	private String meetingOrganizerId;
	@JsonProperty("transcriptContentUrl")
	private String transcriptContentUrl;
	@JsonProperty("createdDateTime")
	private String createdDateTime;
	
	private String transcriptFilePath;
	private String transcriptContent;

	
}
