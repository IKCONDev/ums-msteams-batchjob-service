package com.ikn.ums.msteams.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "transcripts_sourcedata_tab")
@AllArgsConstructor
@NoArgsConstructor
public class Transcript {
	
	@Id
	@SequenceGenerator(name = "transcriptId_gen", initialValue = 1, allocationSize = 1)
	@GeneratedValue(generator = "transcriptId_gen")
	@Column(name = "transcriptId")
	private Long Id;
	
	@Column(name = "meetingTranscriptId")
	private String transcriptId;
	
	@Column(name = "onlineMeetingId")
	private String meetingId;
	
	@Column(name = "meetingOrganizerId")
	private String meetingOrganizerId;
	
	@Column(name = "transcriptContentUrl",length = 500)
	private String transcriptContentUrl;
	
	@Column(name = "createdDateTime")
	private String createdDateTime;
	
	@Column(name = "transcriptFilePath")
	private String transcriptFilePath;
	
	@Column(name = "transcriptContent",length = 10485760) //sets column to varchar(max)
	private String transcriptContent;
	
}
