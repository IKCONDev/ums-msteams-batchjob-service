package com.ikn.ums;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import com.ikn.ums.controller.TeamsBatchJobRestController;
import com.ikn.ums.repo.CronRepository;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = "com.ikn.ums")
@Slf4j
public class TeamsBatchJobApplication extends SpringBootServletInitializer implements SchedulingConfigurer {
	
	@Autowired
	private TeamsBatchJobRestController batchJobController;
	
	@Autowired
	private CronRepository cronRepository;
	
	@Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		log.info("Batch processing started");
        taskRegistrar.addTriggerTask(new Runnable() {
            @Override
            public void run() {
            	//run batch processing
            	ResponseEntity<?> response = batchJobController.meetingDataBatchProcessing();
    			System.out.println("Status "+response.getStatusCodeValue()+" Response "+response.getBody());
    			log.info("batch processing ended");
            }
        }, new Trigger() {
            @Override
            public Date nextExecutionTime(TriggerContext triggerContext) {
                
            	//get cron time from db
              	String cronTime = cronRepository.getCronTime(1).getCronTime();
                CronTrigger trigger = new CronTrigger(cronTime);
                System.out.println("Next Cron Time : "+cronTime);
                Date nextExec = trigger.nextExecutionTime(triggerContext);  
                return nextExec;
            }
        });
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
