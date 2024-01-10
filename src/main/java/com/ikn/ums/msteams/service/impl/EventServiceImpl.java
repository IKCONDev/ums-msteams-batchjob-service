package com.ikn.ums.msteams.service.impl;


import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ikn.ums.msteams.VO.ActionItemsListVO;
import com.ikn.ums.msteams.VO.ActionsItemsVO;
import com.ikn.ums.msteams.entity.Event;
import com.ikn.ums.msteams.repo.EventRepository;
import com.ikn.ums.msteams.service.EventService;

@Service
public class EventServiceImpl implements EventService {
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private EventRepository eventRepository;

		@Override
		public List<ActionsItemsVO> getActionItemsOfEvent(Integer eventId) {
			ActionItemsListVO response = restTemplate.getForObject("http://UMS-MEETING-SERVICE/api/actions/ac-items/"+eventId
					,ActionItemsListVO.class);
			List<ActionsItemsVO> actionItemsListOfEvent = response.getActionItemList(); 
			return actionItemsListOfEvent;
		}
		
		@Override
		public List<ActionsItemsVO> getActionItems() {
			ActionItemsListVO response = restTemplate.getForObject("http://UMS-MEETING-SERVICE/api/actions/ac-items/"
					,ActionItemsListVO.class);
			List<ActionsItemsVO> actionItemsListOfEvent = response.getActionItemList(); 
			return actionItemsListOfEvent;
		}

		@Override
		public List<Event> getAllEvents(String email, boolean isActionItemsGenerated) {
			List<Event> eventList = eventRepository.findAllEvents(email, isActionItemsGenerated);
			return eventList;
		}

		@Transactional
		@Override
		public Integer updateActionItemStatusOfEvent(boolean isActionItemsGenerated, List<Integer> eventIds) {
			System.out.println("EventServiceImpl.updateActionItemStatusOfEvent()");
			int count = eventRepository.updateStatusOfActionItem(isActionItemsGenerated, eventIds);
			return count;
			
		}
		
}
