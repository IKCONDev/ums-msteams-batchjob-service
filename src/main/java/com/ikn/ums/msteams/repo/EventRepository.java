package com.ikn.ums.msteams.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ikn.ums.msteams.entity.Attendee;
import com.ikn.ums.msteams.entity.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {

	//used by NLP
	@Query("FROM Event e WHERE e.organizerEmailId=:email AND e.isActionItemsGenerated=:isActionItemsGenerated")
	List<Event> findAllEvents(String email, boolean isActionItemsGenerated);
	
	@Modifying
	@Query("UPDATE Event e SET e.isActionItemsGenerated=:isActionItemsGenerated WHERE e.id IN (:eventIds)")
	//@Query("UPDATE Event e SET e.isActionItemsGenerated=:isActionItemsGenerated WHERE e.id=:eventId")
	Integer updateStatusOfActionItem(boolean isActionItemsGenerated, List<Integer> eventIds);

}
