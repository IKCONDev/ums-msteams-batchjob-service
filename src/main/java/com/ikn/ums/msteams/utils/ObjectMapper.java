package com.ikn.ums.msteams.utils;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

@Component
public class ObjectMapper {
	
	public ModelMapper modelMapper;
	
	public ObjectMapper() {
		this.modelMapper = new ModelMapper();
		//set a default configuration
		this.modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
	}

}
