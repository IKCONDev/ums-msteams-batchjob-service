package com.ikn.ums.msteams.service;

import java.util.List;

import com.ikn.ums.msteams.dto.BatchDetailsDto;
import com.ikn.ums.msteams.entity.BatchDetails;


public interface TeamsSourceDataBatchProcessService {
	
	void performSourceDataBatchProcessing(BatchDetailsDto batchDetails) throws Exception;
	BatchDetailsDto getLatestSourceDataBatchProcessingRecordDetails();
	List<BatchDetails> getBatchProcessDetails();
}

