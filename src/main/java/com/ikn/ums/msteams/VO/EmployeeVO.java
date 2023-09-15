package com.ikn.ums.msteams.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeVO {
	
		private Integer id;

		private String teamsUserId;

		private String firstName;

		private String lastName;

		private String email;

		private String designation;

		private Long departmentId;
		
		private DepartmentVO department;

}
