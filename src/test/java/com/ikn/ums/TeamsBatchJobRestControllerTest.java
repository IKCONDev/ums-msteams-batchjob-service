package com.ikn.ums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.ikn.ums.controller.TeamsBatchJobRestController;
import com.ikn.ums.dto.EventDto;
import com.ikn.ums.repo.EventRepository;
import com.ikn.ums.service.ITeamsBatchService;


@WebMvcTest(TeamsBatchJobRestController.class)
public class TeamsBatchJobRestControllerTest {
	
	@Autowired
    private MockMvc mockMvc;
	
	@InjectMocks
	private TeamsBatchJobRestController teamsRestController;
	
	@MockBean
	private ITeamsBatchService batchService;
       
	@MockBean
	private EventRepository eventRepo;
	
    @Test
    public void test_meetingDataBatchProcessing_Success() throws Exception {
    	
       // Mock the behavior of teamsBatchService
       doNothing().when(batchService).performBatchProcessing();
       
      // Perform the request and verify the response
       mockMvc.perform(MockMvcRequestBuilders
    			.get("/api/teams/batch-process").accept(MediaType.APPLICATION_JSON))
    	        .andDo(print())
    	        .andExpect(status().isOk())
    	        .andExpect(MockMvcResultMatchers.content().string("Batch processing successfull"));
    }
    
    @Test
    public void test_meetingDataBatchProcessing_Fail() throws Exception {
        // Mock the behavior of teamsBatchService
        doThrow(new Exception()).when(batchService).performBatchProcessing();
        
        // Perform the request and verify the response 
        // Expecting a 500 Internal Server Error for the failed scenario
        mockMvc.perform(MockMvcRequestBuilders
            .get("/api/teams/batch-process")
            .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.content().string("Error while batch processing, Check server logs for full details")); 
    }


    
    @Test
    public void test_meetingDataUserBatchProcessing_Success() throws Exception {
    	String userPrincipal = "Vaishnav.P@ikcontech.com";
    	
    	// Mock the behavior of teamsBatchService
        doNothing().when(batchService).performSingleUserBatchProcessing(userPrincipal);
    	
      // Perform the request and verify the response
      mockMvc.perform(MockMvcRequestBuilders
    			.get("/api/teams/batch-user/"+userPrincipal).accept(MediaType.APPLICATION_JSON))
    	        .andDo(print())
    	        .andExpect(status().isOk())
    	        .andExpect(MockMvcResultMatchers.content().string("Batch processing successfull for user "+userPrincipal));
    }
    
    @Test
    public void test_meetingUserDataBatchProcessing_Fail() throws Exception {
    	String userPrincipal = "Bharat@ikcontech.com";
        // Mock the behavior of teamsBatchService
        doThrow(new Exception()).when(batchService).performSingleUserBatchProcessing(userPrincipal);
        
        // Perform the request and verify the response 
        // Expecting a 500 Internal Server Error for the failed scenario
        mockMvc.perform(MockMvcRequestBuilders
            .get("/api/teams/batch-user/"+userPrincipal)
            .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.content().string("Error while batch processing for user "+userPrincipal+", Check server logs for full details")); 
    }
    
    
    @Test
    public void test_getUserEvents_Success() throws Exception {
        String userPrincipalName = "Vaishnav.P@ikcontech.com";
        List<EventDto> mockEventsList = new ArrayList<>();

        // Create an EventDto object
        EventDto e = new EventDto();
        e.setEventId("j777dd3h883=");
        e.setUserId("9e2a07ef-86ff-4814-bf01-92c6bc0a74ff");
        e.setUserPrinicipalName(userPrincipalName);

        // Add the EventDto object to the mockEventsList
        mockEventsList.add(e);

        // Mock the service response
        when(batchService.getEventByUserPrincipalName(userPrincipalName)).thenReturn(mockEventsList);

        // Perform the request and validate the response
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/teams/events/" + userPrincipalName)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }
    
    @Test
    public void test_getUserEvents_Fail() throws Exception {
        String userPrincipalName = "Vaishnav.P@ikcontech.com";
        List<EventDto> mockEventsList = new ArrayList<>();

        // Create an EventDto object
        EventDto e = new EventDto();
        e.setEventId("j777dd3h883=");
        e.setUserId("9e2a07ef-86ff-4814-bf01-92c6bc0a74ff");
        e.setUserPrinicipalName(userPrincipalName);

        // Add the EventDto object to the mockEventsList
        mockEventsList.add(e);

        // Mock the service response
        doThrow(new Exception()).when(batchService).getEventByUserPrincipalName(userPrincipalName);

        // Perform the request and validate the response
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/teams/events/" + userPrincipalName)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isInternalServerError());
    }
    
    
}
