package com.ikn.ums.dto;

import java.time.ZonedDateTime;
import lombok.Data;

@Data
public class BatchDetailsDto {
	
	private Integer id;
	private ZonedDateTime  startDateTime;
	private ZonedDateTime  endDateTime;
	private ZonedDateTime  lastExecutionDateTime;
	private ZonedDateTime  lastSuccessfullExecutionDateTime;
	private String status;

}
