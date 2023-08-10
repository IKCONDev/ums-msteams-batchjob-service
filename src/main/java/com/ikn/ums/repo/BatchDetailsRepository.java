package com.ikn.ums.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ikn.ums.entity.BatchDetails;

public interface BatchDetailsRepository extends JpaRepository<BatchDetails, Integer> {
	
	@Query("SELECT bd FROM BatchDetails bd WHERE bd.startDateTime = (SELECT MAX(bd2.startDateTime) FROM BatchDetails bd2)")
	Optional<BatchDetails> getLatestBatchProcessingRecord();


}
