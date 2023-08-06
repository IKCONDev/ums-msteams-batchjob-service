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
@Table(name = "recurrence_tab")
public class Recurrence {
	
	@Id
	@SequenceGenerator(name = "recurrence_gen", initialValue = 1, allocationSize = 1)
	@GeneratedValue(generator = "recurrence_gen")
	private Integer id;
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "pattern_fk_id", referencedColumnName = "id", unique = true,nullable = true)
	private Pattern pattern;
	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "range_fk_id", referencedColumnName = "id", unique = true, nullable = true)
	private Range range;


}
