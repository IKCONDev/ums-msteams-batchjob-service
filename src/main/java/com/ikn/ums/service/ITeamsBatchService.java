package com.ikn.ums.service;

import java.util.List;

import com.ikn.ums.dto.EventDto;

public interface ITeamsBatchService {
	
	void performBatchProcessing();
	String initializeMicrosoftGraph();
	
	List<EventDto> getEventByUserPrincipalName(String userPrincipalName);
	void performSingleUserBatchProcessing(String userPrincipalName);

}
