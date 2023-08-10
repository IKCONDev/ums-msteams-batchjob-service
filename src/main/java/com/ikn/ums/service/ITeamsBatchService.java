package com.ikn.ums.service;

import java.util.List;

import com.ikn.ums.dto.BatchDetailsDto;
import com.ikn.ums.dto.EventDto;

public interface ITeamsBatchService {
	
	void performBatchProcessing(BatchDetailsDto batchDetails) throws Exception;
	String initializeMicrosoftGraph();
	List<EventDto> getEventByUserPrincipalName(String userPrincipalName) throws Exception;
	BatchDetailsDto getLatestBatchProcessingRecordDetails();
}
