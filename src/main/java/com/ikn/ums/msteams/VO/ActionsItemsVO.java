package com.ikn.ums.msteams.VO;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActionsItemsVO {
	
	
	private Integer id;
	
	private Integer eventid;
	
	private String actionTitle;
	
	private String Description;
		
    private String actionPriority;
	
	private String actionStatus;
	
	private LocalDateTime startDate;
	
	private LocalDateTime endDate;

	

}
