package com.ikn.ums.msteams.service;

import com.ikn.ums.msteams.dto.BatchDetailsDto;


public interface ITeamsBatchService {
	
	void performBatchProcessing(BatchDetailsDto batchDetails) throws Exception;
	BatchDetailsDto getLatestBatchProcessingRecordDetails();
}

