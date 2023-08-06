package com.ikn.ums.entity;

import java.time.LocalDateTime;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "event_tab")
public class Event {

	@Id
	@SequenceGenerator(name = "events_gen", initialValue = 1, allocationSize = 1)
	@GeneratedValue(generator = "events_gen")
	private Integer id;
	
	private String userId;
	
	private String userPrinicipalName;
	
	private String eventId;
	
	private String eventCreatedDateTime;
	
	private String originalStartTimeZone;
	
	private String originalEndTimeZone;
	
	private String subject;
	
	private String type;
	
	private String occurrenceId;
	
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "start_fk_id", referencedColumnName = "id",unique = true)
	private Start start;
	
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "end_fk_id", referencedColumnName = "id",unique = true)
	private End end;
	
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "location_fk_id", referencedColumnName = "id", unique = true, nullable = true)
	private Location location;
	
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "attendee_id", referencedColumnName = "id",unique = true, nullable = false)
	private Set<Attendee> attendee;
    
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "organizer_fk_id", referencedColumnName = "id", unique = true)
	private Organizer organizer;
    
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "onlineMeeting_fk_id", referencedColumnName = "id", unique = true)
	private OnlineMeeting onlineMeeting;
    
	private String onlineMeetingProvider;
	
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "recurrence_fk_id", referencedColumnName = "id", unique = true, nullable = true)
	private Recurrence recurrence;
	
	private String seriesMasterId;
	
	private String insertedBy = "IKCON UMS";
    
    private String insertedDate = LocalDateTime.now().toString();

}
