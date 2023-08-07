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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Data;

@Data
@Entity
@Table(name = "event_tab")
public class Event {
	
	private static Logger logger = LoggerFactory.getLogger(Event.class);
	
	public Event() {
		logger.info("Event class no-param constructor executed");
	}
	
	public Event(String userId, String userPrinicipalName, String eventId, String eventCreatedDateTime,
            String originalStartTimeZone, String originalEndTimeZone, String subject, String type,
            String occurrenceId, Start start, End end, Location location, Set<Attendee> attendee,
            Organizer organizer, OnlineMeeting onlineMeeting, String onlineMeetingProvider,
            Recurrence recurrence, String seriesMasterId) {
		
		logger.info("Event class parameterized constructor executed");
		
        this.userId = userId;
        this.userPrinicipalName = userPrinicipalName;
        this.eventId = eventId;
        this.eventCreatedDateTime = eventCreatedDateTime;
        this.originalStartTimeZone = originalStartTimeZone;
        this.originalEndTimeZone = originalEndTimeZone;
        this.subject = subject;
        this.type = type;
        this.occurrenceId = occurrenceId;
        this.start = start;
        this.end = end;
        this.location = location;
        this.attendee = attendee;
        this.organizer = organizer;
        this.onlineMeeting = onlineMeeting;
        this.onlineMeetingProvider = onlineMeetingProvider;
        this.recurrence = recurrence;
        this.seriesMasterId = seriesMasterId;
        this.insertedBy = "IKCON UMS";
        this.insertedDate = LocalDateTime.now().toString();
    }

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
