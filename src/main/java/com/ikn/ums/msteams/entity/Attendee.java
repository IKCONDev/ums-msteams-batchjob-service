package com.ikn.ums.msteams.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "attendee_sourcedata_tab")
@AllArgsConstructor
@NoArgsConstructor
public class Attendee {
	
	@Id
	@SequenceGenerator(name = "attendeeId_gen", initialValue = 1, allocationSize = 1)
	@GeneratedValue(generator = "attendeeId_gen")
	@Column(name = "attendeeId")
	private Long attendeeId;
	
	@Column(name = "type")
	private String type;
	
	@Column(name = "status")
	private String status;
	
	@Column(name = "attendeeEmail")
	private String email;
	
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "meet_id", referencedColumnName = "meetingId", nullable = true)
    private Event event;
	/*
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "user_fk_id",nullable = true)
	*/
	
	@Column(name = "user_id")
	private String emailId;
 
}
