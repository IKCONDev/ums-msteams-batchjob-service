package com.ikn.ums.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ikn.ums.entity.CronDetails;

public interface CronRepository extends JpaRepository<CronDetails, Integer> {
	
	@Query("FROM CronDetails WHERE id=:id")
	CronDetails getCronTime(Integer id);


}
