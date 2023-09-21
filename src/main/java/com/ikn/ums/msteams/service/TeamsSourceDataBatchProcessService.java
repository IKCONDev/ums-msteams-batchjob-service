package com.ikn.ums.msteams.service;

import com.ikn.ums.msteams.dto.BatchDetailsDto;


public interface TeamsSourceDataBatchProcessService {
	
	void performSourceDataBatchProcessing(BatchDetailsDto batchDetails) throws Exception;
	BatchDetailsDto getLatestSourceDataBatchProcessingRecordDetails();
}

