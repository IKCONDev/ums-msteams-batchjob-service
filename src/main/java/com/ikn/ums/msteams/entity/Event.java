package com.ikn.ums.msteams.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "event_tab")
@AllArgsConstructor
@NoArgsConstructor
public class Event {
	
	private static Logger logger = LoggerFactory.getLogger(Event.class);

	@Id
	@SequenceGenerator(name = "events_gen", initialValue = 1, allocationSize = 1)
	@GeneratedValue(generator = "events_gen")
	@Column(name = "Id", nullable = false)
	private Integer id;
	
	@Column(name = "EventId", nullable = false)
	private String eventId;
	
	@Column(name = "CreatedDateTime", nullable = false)
	private String createdDateTime;
	
	@Column(name = "OriginalStartTimeZone")
	private String originalStartTimeZone;
	
	@Column(name = "OriginalEndTimeZone")
	private String originalEndTimeZone;
	
	@Column(name = "Subject")
	private String subject;
	
	@Column(name = "Type")
	private String type;
	
	@Column(name = "OccurrenceId")
	private String occurrenceId;
	
	@Column(name = "StartDateTime")
	private LocalDateTime startDateTime;
	
	private LocalDateTime endDateTime;
	
	private String startTimeZone;
	
	private String endTimeZone;
		
	private String location;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER,orphanRemoval = true)
	@JoinColumn(name = "event_fk_id", referencedColumnName = "id", nullable = true)
    private Set<Attendee> attendees;
    
    private String organizerEmailId;
    
    private String organizerName;
    
	private String onlineMeetingId;
    
	private String onlineMeetingProvider;
	
	/*
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "recurrence_fk_id", referencedColumnName = "id", unique = true, nullable = true)
	private Recurrence recurrence;
	*/
	
	private String seriesMasterId;
	
	private String joinUrl;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "event_fk_id",nullable = true)
	private List<Transcript> meetingTranscripts;
	                 
	private String insertedBy = "IKCON UMS";
    
    private String insertedDate = LocalDateTime.now().toString();
    
    private Integer userId;

}
