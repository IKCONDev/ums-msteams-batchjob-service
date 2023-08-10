package com.ikn.ums.entity;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "batch_tab")
@Data
public class BatchDetails {
	
	@Id
	@SequenceGenerator(name = "attendess_gen", initialValue = 1, allocationSize = 1)
	@GeneratedValue(generator = "attendess_gen")
	private Integer id;
	private LocalDateTime  startDateTime;
	private LocalDateTime  endDateTime;
	private LocalDateTime  lastSuccessfullExecutionDateTime;
	private String status;

}
