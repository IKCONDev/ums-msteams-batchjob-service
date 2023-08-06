package com.ikn.ums.entity;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "online_meeting_tab")
public class OnlineMeeting {
	
	@Id
	@SequenceGenerator(name = "onlinemeeting_gen", initialValue = 1, allocationSize = 1)
	@GeneratedValue(generator = "onlinemeeting_gen")
	private Integer id;
	
	private String onlineMeetingId;
	
	private String subject;
	
	private String joinUrl;
	
	private String occurrenceId;
	
	private String onlineMeetingType;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "meeting_fk_id",nullable = true)
	private Set<Transcript> meetingTranscripts;
}
