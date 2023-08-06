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
@Table(name = "organizer_tab")
public class Organizer {
	
	@Id
	@SequenceGenerator(name = "organizer_gen", initialValue = 1, allocationSize = 1)
	@GeneratedValue(generator = "organizer_gen")
	private Integer id;
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "emailaddress_fk_id", referencedColumnName = "id", unique = true)
	private EmailAddress emailAddress;
	
	
}
