package com.ikn.ums.entity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "location_tab")
public class Location {
	
	@Id
	@SequenceGenerator(name = "location_gen", initialValue = 1, allocationSize = 1)
	@GeneratedValue(generator = "location_gen")
	private Integer id;
	
	private String displayName;
	
	private String locationType;
	
	private String uniquerId;
	
	private String uniqueIdType;
	
}
