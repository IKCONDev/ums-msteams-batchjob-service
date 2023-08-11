package com.ikn.ums.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ikn.ums.entity.UserProfile;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {
		
	Optional<UserProfile> findByUserPrincipalName(String userPrincipalName);
	
	List<UserProfile> findByMailIn(List<String> mailIds);

}
