package com.ikn.ums.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Data;

//@Entity
@Data
public class TranscriptContent {

	@Id
	@SequenceGenerator(name = "transcript_content_gen", initialValue = 1, allocationSize = 1)
	@GeneratedValue(generator = "transcript_content_gen")
	private Integer id;
	private String transcriptFilePath;

}
