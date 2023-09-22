package com.ikn.ums.msteams.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ikn.ums.msteams.entity.BatchDetails;

@Repository
public interface BatchDetailsRepository extends JpaRepository<BatchDetails, Integer> {
	
	@Query("SELECT bd FROM BatchDetails bd WHERE bd.startDateTime = (SELECT MAX(bd2.startDateTime) FROM BatchDetails bd2)")
	Optional<BatchDetails> getLatestBatchProcessingRecord();
	
}
