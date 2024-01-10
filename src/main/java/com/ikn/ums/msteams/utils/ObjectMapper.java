package com.ikn.ums.msteams.utils;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

@Component
public class ObjectMapper {
	
	public static final ModelMapper modelMapper;
	
	static {
		modelMapper = new ModelMapper();
		//set a default configuration
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
	}

}
