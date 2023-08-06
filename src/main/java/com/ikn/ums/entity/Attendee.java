package com.ikn.ums.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import lombok.Data;

@Data
@Entity
public class Attendee {
	
	@Id
	@SequenceGenerator(name = "attendess_gen", initialValue = 1, allocationSize = 1)
	@GeneratedValue(generator = "attendess_gen")
	private Integer id;
	private String type;
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER,optional = false, targetEntity = EmailAddress.class)
	@JoinColumn(name = "status_fk_id", nullable = true, referencedColumnName = "id", unique = true)
	private Status status;
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER,optional = false, targetEntity = EmailAddress.class)
	@JoinColumn(name = "email_fk_id", nullable = false, referencedColumnName = "id", unique = true)
	private EmailAddress emailAddress;

}
