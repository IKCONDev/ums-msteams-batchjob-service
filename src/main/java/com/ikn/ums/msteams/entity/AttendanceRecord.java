package com.ikn.ums.msteams.entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@Table(name = "attendancerecord_sourcedata_tab")
public class AttendanceRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Integer id;
	@Column(name = "attendeeEmailAddress")
	private String emailAddress;
	@Column(name = "totalAttendanceInSeconds")
	private int totalAttendanceInSeconds;
	@Column(name = "attendeeMeetingRole")
	private String meetingRole;
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JoinColumn(name = "attendance_record_id", referencedColumnName = "id")
	private List<AttendanceInterval> attendanceIntervals;
}
