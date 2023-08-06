package com.ikn.ums.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "pattern_tab")
public class Pattern {
	
	@Id
	@SequenceGenerator(name = "pattern_gen", initialValue = 1, allocationSize = 1)
	@GeneratedValue(generator = "pattern_gen")
	private Integer id;
	private String type;
	private Integer interval;
	private Integer month;
	private Integer dayOfMonth;
	private String firstdayOfWeek;
	@Column(name = "index_col")
	private String index;
	private String[] daysOfWeek;

}
