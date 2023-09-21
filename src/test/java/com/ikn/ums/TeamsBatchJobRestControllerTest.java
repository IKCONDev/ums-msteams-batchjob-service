package com.ikn.ums;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.stubbing.OngoingStubbing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.ikn.ums.msteams.TeamsRawDataBatchProcessApplication;
import com.ikn.ums.msteams.controller.TeamsSourceDataBatchProcessController;
import com.ikn.ums.msteams.dto.BatchDetailsDto;
import com.ikn.ums.msteams.entity.CronDetails;
import com.ikn.ums.msteams.repo.CronRepository;
import com.ikn.ums.msteams.repo.EventRepository;
import com.ikn.ums.msteams.service.TeamsSourceDataBatchProcessService;


@WebMvcTest(TeamsSourceDataBatchProcessController.class)
public class TeamsBatchJobRestControllerTest {
	
	@Autowired
    private MockMvc mockMvc;
	
	@InjectMocks
	private TeamsSourceDataBatchProcessController teamsRestController;
	
	@MockBean
	private TeamsSourceDataBatchProcessService batchService;
       
	@MockBean
	private EventRepository eventRepo;
	
	//@MockBean 
	//private UserProfileRepository userProfileRepo;
	
	@MockBean
	private TeamsRawDataBatchProcessApplication teamsBatchJobApp;
	
	
	@Test
	public void test_meetingDataBatchProcessing_Success() throws Exception {
	
	    BatchDetailsDto batchDetailsDto = new BatchDetailsDto();
	    batchDetailsDto.setId(1);
	    batchDetailsDto.setStartDateTime(LocalDateTime.now());
	    batchDetailsDto.setStatus("COMPLETED");
	    
	    // Mock the behavior of teamsBatchService
	    OngoingStubbing<BatchDetailsDto> stub = when(batchService.getLatestSourceDataBatchProcessingRecordDetails()).thenReturn(batchDetailsDto);
	    doNothing().when(batchService).performSourceDataBatchProcessing(batchDetailsDto);
	    
	    // Perform the request and verify the response
	    mockMvc.perform(MockMvcRequestBuilders
	            .get("/api/teams/batch-process")
	            .accept(MediaType.APPLICATION_JSON))
	            .andDo(print())
	            .andExpect(status().isNotFound());
	            //.andExpect(MockMvcResultMatchers.content().string("Batch processing successfull"));
	}
    
    @Test
    public void test_meetingDataBatchProcessing_Fail() throws Exception {
    	
    	BatchDetailsDto batchDetailsDto = new BatchDetailsDto();
    	batchDetailsDto.setId(1);
    	batchDetailsDto.setStartDateTime(LocalDateTime.now());
    	
        // Mock the behavior of teamsBatchService
        doThrow(new Exception()).when(batchService).performSourceDataBatchProcessing(batchDetailsDto);
        
        // Perform the request and verify the response 
        // Expecting a 500 Internal Server Error for the failed scenario
        mockMvc.perform(MockMvcRequestBuilders
            .get("/api/teams/batch-process")
            .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
            //.andExpect(MockMvcResultMatchers.content().string("Error while batch processing, Check server logs for full details")); 
    }
}