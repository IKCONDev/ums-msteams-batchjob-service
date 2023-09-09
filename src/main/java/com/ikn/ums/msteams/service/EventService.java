package com.ikn.ums.msteams.service;

import java.util.List;

import com.ikn.ums.msteams.VO.ActionsItemsVO;
import com.ikn.ums.msteams.entity.Attendee;
import com.ikn.ums.msteams.entity.Event;

public interface EventService {
	
	List<Event> getEventByUserPrincipalName(String username) throws Exception;
	Integer getUserAttendedEventsCount(Integer userId);
	Integer getUserOrganizedEventCount(String email);
	List<Attendee> getUserAttendedEvents(Integer userId);
	List<ActionsItemsVO> getActionItemsOfEvent(Integer eventId);
	List<ActionsItemsVO> getActionItems();
	

}
