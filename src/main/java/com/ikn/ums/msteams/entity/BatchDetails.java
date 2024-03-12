package com.ikn.ums.msteams.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "batch_tab")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchDetails {
	
	@Id
	@SequenceGenerator(name = "batchId_gen", initialValue = 1, allocationSize = 1)
	@GeneratedValue(generator = "batchId_gen")
	@Column(name = "batchId")
	private Long batchId;
	
	@Column(name = "startDateTime")
	private LocalDateTime  startDateTime;
	
	@Column(name = "endDateTime")
	private LocalDateTime  endDateTime;
	
	@Column(name = "lastSuccessfullExecutionDateTime")
	private LocalDateTime  lastSuccessfullExecutionDateTime;
	
	@Column(name = "status")
	private String status;
	
	@Column(name = "batchProcessFailureReason")
	private String batchProcessFailureReason;

}
