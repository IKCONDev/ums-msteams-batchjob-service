package com.ikn.ums.msteams.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentVO {

	private Long departmentId;
	private String departmentName;
	private String departmentAddress;
	private String departmentCode;
}
