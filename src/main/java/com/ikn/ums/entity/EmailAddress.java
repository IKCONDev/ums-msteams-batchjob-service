package com.ikn.ums.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "emailaddress_tab")
public class EmailAddress {
	
	@Id
	@SequenceGenerator(name = "emailaddress_gen", initialValue = 1, allocationSize = 1)
	@GeneratedValue(generator = "emailaddress_gen")
	private Integer id;
	private String name;
	private String address;

	
}
