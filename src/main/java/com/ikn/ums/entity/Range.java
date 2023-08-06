package com.ikn.ums.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "range_tab")
public class Range {
	
	@Id
	@SequenceGenerator(name = "range_gen", initialValue = 1, allocationSize = 1)
	@GeneratedValue(generator = "range_gen")
	private Integer id;
	private String type;
	private String startDate;
	private String endDate;
	private String recurrenceTimeZone;
	private String numberOfOccurrences;
	
}
