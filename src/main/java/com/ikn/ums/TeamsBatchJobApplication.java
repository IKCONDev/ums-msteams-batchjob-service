package com.ikn.ums;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.ikn.ums.controller.TeamsBatchJobRestController;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = "com.ikn.ums")
public class TeamsBatchJobApplication extends SpringBootServletInitializer {
	
	@Autowired
	private TeamsBatchJobRestController batchJobController;
	
	//run every one hour
	 @Scheduled(cron = "0 0 */2 * * *")
	public void performBatchProcessing() {
		ResponseEntity<?> response = batchJobController.meetingDataBatchProcessing();
		System.out.println("Status "+response.getStatusCodeValue()+" Response "+response.getBody());
	}
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		// TODO Auto-generated method stub
		return builder.sources(TeamsBatchJobApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(TeamsBatchJobApplication.class, args);
	}

}
