package com.ikn.ums.msteams.service;

import java.util.List;

import com.ikn.ums.msteams.VO.ActionsItemsVO;
import com.ikn.ums.msteams.entity.Event;

public interface EventService {

	List<ActionsItemsVO> getActionItemsOfEvent(Integer eventId);
	List<ActionsItemsVO> getActionItems();
	List<Event> getAllEvents(String email, boolean isActionItemsGenerated);
	Integer updateActionItemStatusOfEvent(boolean isActionItemsGenerated, List<Integer> eventId);
	

}
