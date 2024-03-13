package com.ikn.ums.msteams.entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "attendancereport_sourcedata_tab")
public class AttendanceReport {
	
	@javax.persistence.Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	@Column(name = "attendanceReportId")
	private String attendanceReportId;
	@Column(name = "totalParticipantCount")
	private String totalParticipantCount;
	@Column(name = "meetingStartDateTime")
	private String meetingStartDateTime;
	@Column(name = "meetingEndDateTime")
	private String meetingEndDateTime;
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "attendanceReportId",referencedColumnName = "id")
	private List<AttendanceRecord> attendanceRecords;

}
