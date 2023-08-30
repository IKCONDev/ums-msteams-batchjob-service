package com.ikn.ums.service;

import java.util.List;

import com.ikn.ums.dto.BatchDetailsDto;
import com.ikn.ums.dto.EventDto;
import com.ikn.ums.entity.Event;
import com.ikn.ums.entity.UserProfile;

public interface ITeamsBatchService {
	
	void performBatchProcessing(BatchDetailsDto batchDetails) throws Exception;
	List<Event> getEventByUserPrincipalName(String userPrincipalName) throws Exception;
	BatchDetailsDto getLatestBatchProcessingRecordDetails();
}
