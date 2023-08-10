package com.ikn.ums.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class BatchDetailsDto {
	
	private Integer id;
	private LocalDateTime  startDateTime;
	private LocalDateTime  endDateTime;
	private LocalDateTime  lastSuccessfullExecutionDateTime;
	private String status;

}
