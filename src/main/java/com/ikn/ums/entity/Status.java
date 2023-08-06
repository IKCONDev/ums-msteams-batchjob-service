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

@Data
@Entity
@Table(name = "status_tab")
public class Status {
	
	@Id
	@SequenceGenerator(name = "status_gen", initialValue = 1, allocationSize = 1)
	@GeneratedValue(generator = "status_gen")
	private Integer id;
	private String response;
	private String time;

}
