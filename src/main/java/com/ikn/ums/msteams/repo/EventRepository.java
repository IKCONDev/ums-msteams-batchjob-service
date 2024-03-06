package com.ikn.ums.msteams.repo;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.ikn.ums.msteams.entity.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {

	//used by NLP
	@Query("FROM Event e WHERE e.organizerEmailId=:email AND e.isActionItemsGenerated=:isActionItemsGenerated")
	List<Event> findAllEvents(String email, boolean isActionItemsGenerated);
	
	@Modifying
	@Query("UPDATE Event e SET e.isActionItemsGenerated=:isActionItemsGenerated WHERE e.id IN (:eventIds)")
	Integer updateStatusOfActionItem(boolean isActionItemsGenerated, List<Integer> eventIds);
	
	@Query(value="FROM Event e WHERE DATE(createdDateTime)=:date")
	//@Query(value = "select * from event_sourcedata_tab where DATE(created_date_time)=:date", nativeQuery = true)
	List<Event> getCurrentDayEvents(LocalDate date);
	
	Event findByEventId(String eventId);
	
	Event findByOccurrenceId(String occurrenceId);

}
