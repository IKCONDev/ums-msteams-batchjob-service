package com.ikn.ums.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "cron_tab")
public class CronDetails {
	
	@Id
	@SequenceGenerator(name = "cron_gen", initialValue = 1, allocationSize = 1)
	@GeneratedValue(generator = "cron_gen")
	private Integer id;
	private String cronTime;

}
