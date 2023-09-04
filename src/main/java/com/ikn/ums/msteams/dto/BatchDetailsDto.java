package com.ikn.ums.msteams.dto;

import java.time.LocalDateTime;

import com.ikn.ums.msteams.entity.UserProfile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchDetailsDto {
	
	private Integer id;
	private LocalDateTime  startDateTime;
	private LocalDateTime  endDateTime;
	private LocalDateTime  lastSuccessfullExecutionDateTime;
	private String status;

}
