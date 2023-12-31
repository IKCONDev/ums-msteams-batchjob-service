package com.ikn.ums.msteams.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ikn.ums.msteams.entity.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
	
	//@Query("SELECT COUNT(*) FROM Event WHERE userPrinicipalName=:userPrincipalName")
	//Integer findUserPrinicipalName(String userPrincipalName);
	
	@Query("SELECT e FROM Event e JOIN e.user u WHERE u.mail=:email")
	List<Event> findUserEvents(String email);
	
	@Query("SELECT e FROM Event e JOIN e.attendees a JOIN e.meetingTranscripts t WHERE a.email=:email")
	List<Event> findUserAttendedEvents(String email);
	
	@Query("SELECT COUNT(*) FROM Event e JOIN e.user u Where e.user.mail=:email")
	Long findUserOrganizedMeetingsCount(String email);

}
