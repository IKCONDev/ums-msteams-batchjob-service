package com.ikn.ums.entity;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
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
