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
	@SequenceGenerator(name = "transcripts_gen", initialValue = 1, allocationSize = 1)
	@GeneratedValue(generator = "transcripts_gen")
	private Integer id;
	private String transcriptId;
	private String meetingId;
	private String meetingOrganizerId;
	@Column(length = 500)
	private String transcriptContentUrl;
	private String createdDateTime;
	private String transcriptFilePath;
	@Column(length = 6000)
	private String transcriptContent;
	
}
