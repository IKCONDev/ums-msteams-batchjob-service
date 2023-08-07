package com.ikn.ums.service;

import java.util.List;

import com.ikn.ums.dto.EventDto;

public interface ITeamsBatchService {
	
	void performBatchProcessing() throws Exception;
	String initializeMicrosoftGraph();
	
	List<EventDto> getEventByUserPrincipalName(String userPrincipalName) throws Exception;
	void performSingleUserBatchProcessing(String userPrincipalName) throws Exception;

}
