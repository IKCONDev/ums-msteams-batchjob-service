package com.ikn.ums.msteams.service;

import java.util.List;

import com.ikn.ums.msteams.dto.BatchDetailsDto;
import com.ikn.ums.msteams.entity.Event;


public interface ITeamsBatchService {
	
	void performBatchProcessing(BatchDetailsDto batchDetails) throws Exception;
	List<Event> getEventByUserPrincipalName(String username) throws Exception;
	BatchDetailsDto getLatestBatchProcessingRecordDetails();
	List<Event> getUserAttendedEvents(String username);
	Long getUserOrganizedEmailCount(String email);
}

