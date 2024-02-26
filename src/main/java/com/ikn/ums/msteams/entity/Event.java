package com.ikn.ums.msteams.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "event_sourcedata_tab")
@AllArgsConstructor
@NoArgsConstructor
public class Event {
	
	@Id
	@SequenceGenerator(name = "eventId_gen", initialValue = 1, allocationSize = 1)
	@GeneratedValue(generator = "eventId_gen")
	@Column(name = "meetingId", nullable = false)
	private Long meetingId;
	
	@Column(name = "eventId", nullable = false)
	private String eventId;
	
	@Column(name = "createdDateTime", nullable = false)
	private String createdDateTime;
	
	@Column(name = "originalStartTimeZone")
	private String originalStartTimeZone;
	
	@Column(name = "originalEndTimeZone")
	private String originalEndTimeZone;
	
	@Column(name = "subject")
	private String subject;
	
	@Column(name = "type")
	private String type;
	
	@Column(name = "occurrenceId")
	private String occurrenceId;
	
	@Column(name = "startDateTime")
	private LocalDateTime startDateTime;
	
	@Column(name = "endDateTime")
	private LocalDateTime endDateTime;
	
	@Column(name = "startTimeZone")
	private String startTimeZone;
	
	@Column(name = "endTimeZone")
	private String endTimeZone;
		
	@Column(name = "location")
	private String location;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "meeting_id" ,referencedColumnName = "meetingId", nullable = true)
    private Set<Attendee> attendees;
    
	@Column(name = "organizerEmailId")
    private String organizerEmailId;
    
	@Column(name = "organizerName")
    private String organizerName;
    
	@Column(name = "onlineMeetingId")
	private String onlineMeetingId;
    
	@Column(name = "onlineMeetingProvider")
	private String onlineMeetingProvider;
	
	/*
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "recurrence_fk_id", referencedColumnName = "id", unique = true, nullable = true)
	private Recurrence recurrence;
	*/
	
	@Column(name = "seriesMasterId")
	private String seriesMasterId;
	
	@Column(name = "joinUrl")
	private String joinUrl;
	
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
	@JoinColumn(name = "event_fk_id",nullable = true)
	private List<Transcript> meetingTranscripts;
	   
	@Column(name = "insertedBy")
	private String insertedBy = "AUTO-BATCH-PROCESS";
    
	@Column(name = "insertedDate")
    private String insertedDate = LocalDateTime.now().toString();
    
	@Column(name = "user_id")
    private String emailId;
	
	@Column(name = "departmentId")
	private Long departmentId;
    
    @Column(name = "isActionItemsGenerated",nullable = true)
    private boolean isActionItemsGenerated = false;

    @Column(name = "batchId")
    private Long batchId;
    
    //@Column(name = "isSubmitted")
    //private boolean isSubmitted;
}
