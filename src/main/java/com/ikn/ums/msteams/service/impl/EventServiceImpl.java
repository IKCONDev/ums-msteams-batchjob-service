package com.ikn.ums.msteams.service.impl;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.common.base.Strings;
import com.ikn.ums.msteams.VO.ActionItemsListVO;
import com.ikn.ums.msteams.VO.ActionsItemsVO;
import com.ikn.ums.msteams.entity.Event;
import com.ikn.ums.msteams.exception.EmptyInputException;
import com.ikn.ums.msteams.exception.ErrorCodeMessages;
import com.ikn.ums.msteams.repo.EventRepository;
import com.ikn.ums.msteams.service.EventService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EventServiceImpl implements EventService {
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private EventRepository eventRepository;

		@Override
		public List<ActionsItemsVO> getActionItemsOfEvent(Integer eventId) {
			log.info("getActionItemsOfEvent() entered with args : eventId");
			if(eventId <= 0) {
				throw new EmptyInputException(ErrorCodeMessages.ERR_MSTEAMS_EVENTID_EMPTY_CODE, 
						ErrorCodeMessages.ERR_MSTEAMS_EVENTID_EMPTY_MSG);
			}
			log.info("getActionItemsOfEvent() is under execution...");
			ActionItemsListVO response = restTemplate.getForObject("http://UMS-MEETING-SERVICE/api/actions/ac-items/"+eventId
					,ActionItemsListVO.class);
			List<ActionsItemsVO> actionItemsListOfEvent = response.getActionItemList(); 
			log.info("getActionItemsOfEvent() executed succesfully.");
			return actionItemsListOfEvent;
		}
		
		@Override
		public List<ActionsItemsVO> getActionItems() {
			log.info("getActionItems() entered with no args");
			log.info("getActionItems() is under execution...");
			ActionItemsListVO response = restTemplate.getForObject("http://UMS-MEETING-SERVICE/api/actions/ac-items/"
					,ActionItemsListVO.class);
			log.info("getActionItems() Call to Meeting microservice successfull.");
			List<ActionsItemsVO> actionItemsListOfEvent = response.getActionItemList(); 
			log.info("getActionItems() executed succesfully.");
			return actionItemsListOfEvent;
		}

		@Override
		public List<Event> getAllEvents(String email, boolean isActionItemsGenerated) {
			log.info("getAllEvents() entered with no args");
			if(Strings.isNullOrEmpty(email) || email.isEmpty()) {
				log.info("getAllEvents() EmptyInputException : userId / emailId is empty.");
				throw new EmptyInputException(ErrorCodeMessages.ERR_MSTEAMS_EMAIL_ID_EMPTY_CODE, 
						ErrorCodeMessages.ERR_MSTEAMS_EMAIL_ID_EMPTY_MSG);
			}
			log.info("getAllEvents() is under execution...");
			var eventList = eventRepository.findAllEvents(email, isActionItemsGenerated);
			log.info("getAllEvents() executed succesfully.");
			return eventList;
		}

		@Transactional
		@Override
		public Integer updateActionItemStatusOfEvent(boolean isActionItemsGenerated, List<Integer> eventIds) {
			log.info("updateActionItemStatusOfEvent() entered with no args");
			if(eventIds.isEmpty()) {
				log.info("updateActionItemStatusOfEvent() EmptyInputException : eventIds list is empty");
				throw new EmptyInputException(ErrorCodeMessages.ERR_MSTEAMS_EVENTID_EMPTY_CODE, 
						ErrorCodeMessages.ERR_MSTEAMS_EVENTID_EMPTY_MSG);
			}
			log.info("updateActionItemStatusOfEvent() is under execution...");
			var count = eventRepository.updateStatusOfActionItem(isActionItemsGenerated, eventIds);
			log.info("updateActionItemStatusOfEvent() executed succesfully.");
			return count;
			
		}
		
}
