package com.ikn.ums.msteams.service;

import com.ikn.ums.msteams.dto.BatchDetailsDto;


public interface TeamsRawDataBatchProcessService {
	
	void performBatchProcessing(BatchDetailsDto batchDetails) throws Exception;
	BatchDetailsDto getLatestBatchProcessingRecordDetails();
}

