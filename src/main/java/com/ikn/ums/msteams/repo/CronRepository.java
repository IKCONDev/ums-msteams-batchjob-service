package com.ikn.ums.msteams.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ikn.ums.msteams.entity.CronDetails;

@Repository
public interface CronRepository extends JpaRepository<CronDetails, Integer> {
	
	@Query("FROM CronDetails WHERE id=:id")
	CronDetails getCronTime(Integer id);


}
