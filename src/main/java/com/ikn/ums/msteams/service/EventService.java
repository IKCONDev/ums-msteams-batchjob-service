package com.ikn.ums.msteams.service;

import java.util.List;

import com.ikn.ums.msteams.VO.ActionsItemsVO;
import com.ikn.ums.msteams.entity.Attendee;
import com.ikn.ums.msteams.entity.Event;

public interface EventService {
	
	List<Event> getEventByUserPrincipalName(String username) throws Exception;
	Integer getUserAttendedEventsCount(String userId);
	Integer getUserOrganizedEventCount(String email);
	List<Event> getUserAttendedEvents(String email);
	List<ActionsItemsVO> getActionItemsOfEvent(Integer eventId);
	List<ActionsItemsVO> getActionItems();
	List<Event> getAllEvents(String email, boolean isActionItemsGenerated);
	Integer updateActionItemStatusOfEvent(boolean isActionItemsGenerated, List<Integer> eventId);
	

}
