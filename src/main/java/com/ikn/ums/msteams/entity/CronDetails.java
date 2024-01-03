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
@Table(name = "cron_tab")
@AllArgsConstructor
@NoArgsConstructor
public class CronDetails {
	
	@Id
	@SequenceGenerator(name = "cronId_gen", initialValue = 1, allocationSize = 1)
	@GeneratedValue(generator = "cronId_gen")
	@Column(name = "cronId")
	private Integer cronId;
	@Column(name = "cronTime")
	private String cronTime;
	@Column(name = "hour")
	private String hour;

}
