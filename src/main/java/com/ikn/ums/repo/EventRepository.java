package com.ikn.ums.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ikn.ums.entity.Event;
import java.util.List;


public interface EventRepository extends JpaRepository<Event, Integer> {
	
	List<Event> findByUserPrinicipalName(String userPrinicipalName);

}
