package com.ikn.ums;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

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
import com.ikn.ums.dto.BatchDetailsDto;
import com.ikn.ums.repo.EventRepository;
import com.ikn.ums.repo.UserProfileRepository;
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
	
	@MockBean 
	private UserProfileRepository userProfileRepo;
	
	@Test
	public void test_meetingDataBatchProcessing_Success() throws Exception {
	    BatchDetailsDto batchDetailsDto = new BatchDetailsDto();
	    batchDetailsDto.setId(1);
	    batchDetailsDto.setStartDateTime(LocalDateTime.now());
	    batchDetailsDto.setStatus("COMPLETED");
	    
	    // Mock the behavior of teamsBatchService
	    when(batchService.getLatestBatchProcessingRecordDetails()).thenReturn(batchDetailsDto);
	    doNothing().when(batchService).performBatchProcessing(batchDetailsDto);
	    
	    // Perform the request and verify the response
	    mockMvc.perform(MockMvcRequestBuilders
	            .get("/api/teams/batch-process")
	            .accept(MediaType.APPLICATION_JSON))
	            .andDo(print())
	            .andExpect(status().isOk())
	            .andExpect(MockMvcResultMatchers.content().string("Batch processing successfull"));
	}
    
    @Test
    public void test_meetingDataBatchProcessing_Fail() throws Exception {
    	
    	BatchDetailsDto batchDetailsDto = new BatchDetailsDto();
    	batchDetailsDto.setId(1);
    	batchDetailsDto.setStartDateTime(LocalDateTime.now());
    	
        // Mock the behavior of teamsBatchService
        doThrow(new Exception()).when(batchService).performBatchProcessing(batchDetailsDto);
        
        // Perform the request and verify the response 
        // Expecting a 500 Internal Server Error for the failed scenario
        mockMvc.perform(MockMvcRequestBuilders
            .get("/api/teams/batch-process")
            .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(MockMvcResultMatchers.content().string("Error while batch processing, Check server logs for full details")); 
    }
}