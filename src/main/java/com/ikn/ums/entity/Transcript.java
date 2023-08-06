package com.ikn.ums.entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
public class Transcript {
	
	@Id
	@SequenceGenerator(name = "transcript_gen", initialValue = 1, allocationSize = 1)
	@GeneratedValue(generator = "transcript_gen")
	private Integer id;
	private String transcriptId;
	private String meetingId;
	private String meetingOrganizerId;
	@Column(length = 500)
	private String transcriptContentUrl;
	private String createdDateTime;
	private String transcriptFilePath;
	
}
