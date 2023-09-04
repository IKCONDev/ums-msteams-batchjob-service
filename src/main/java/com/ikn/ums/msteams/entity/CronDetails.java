package com.ikn.ums.msteams.entity;

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
	@SequenceGenerator(name = "cron_gen", initialValue = 1, allocationSize = 1)
	@GeneratedValue(generator = "cron_gen")
	private Integer id;
	private String cronTime;

}
