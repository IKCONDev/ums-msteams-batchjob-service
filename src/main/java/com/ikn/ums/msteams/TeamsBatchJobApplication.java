package com.ikn.ums.msteams;

import java.time.LocalDateTime;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.web.client.RestTemplate;

import com.ikn.ums.msteams.controller.TeamsBatchJobRestController;
import com.ikn.ums.msteams.repo.CronRepository;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
//@EnableScheduling
@ComponentScan(basePackages = "com.ikn.ums")
@Slf4j
@EnableDiscoveryClient
public class TeamsBatchJobApplication extends SpringBootServletInitializer implements SchedulingConfigurer {
	
	@Autowired
	private TeamsBatchJobRestController batchJobController;
	
	@Autowired
	private CronRepository cronRepository;
	
	@Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(new Runnable() {
            @Override
            public void run() {
            	//run batch processing
            	log.info("Batch processing started at "+LocalDateTime.now());
            	ResponseEntity<?> response = batchJobController.meetingDataBatchProcessing();
    			System.out.println("Status "+response.getStatusCodeValue()+" Response "+response.getBody());
    			log.info("Status "+response.getStatusCodeValue()+" Response "+response.getBody());
    			log.info("batch processing ended at : "+LocalDateTime.now());
            }
        }, new Trigger() {
            @Override
            public Date nextExecutionTime(TriggerContext triggerContext) {
                
            	//get cron time from db
              	String cronTime = cronRepository.getCronTime(1).getCronTime();
                CronTrigger trigger = new CronTrigger(cronTime);
                System.out.println("Next Cron Time : "+cronTime);
                log.info("Next Cron Time : "+cronTime);
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
	
	@Bean
	@LoadBalanced
	public RestTemplate createLoadBalancedRestTemplate() {
		return new RestTemplate();
	}

}
