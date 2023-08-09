package com.ikn.ums.entity;

import java.sql.Timestamp;
import java.time.ZonedDateTime;

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
	private ZonedDateTime  startDateTime;
	private ZonedDateTime  endDateTime;
	private ZonedDateTime  lastExecutionDateTime;
	private ZonedDateTime  lastSuccessfullExecutionDateTime;
	private String status;

}
