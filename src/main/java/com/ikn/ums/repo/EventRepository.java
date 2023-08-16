package com.ikn.ums.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ikn.ums.entity.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
	
	//@Query("SELECT COUNT(*) FROM Event WHERE userPrinicipalName=:userPrincipalName")
	//Integer findUserPrinicipalName(String userPrincipalName);
	
	//List<Event> findByUserPrinicipalName(String userPrinicipalName);


}
