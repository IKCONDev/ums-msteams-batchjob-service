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

import com.ikn.ums.msteams.controller.TeamsSourceDataBatchProcessController;
import com.ikn.ums.msteams.repo.CronRepository;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = "com.ikn.ums.msteams")
@Slf4j
@EnableDiscoveryClient
public class TeamsRawDataBatchProcessApplication extends SpringBootServletInitializer implements SchedulingConfigurer {
	
	@Autowired
	private TeamsSourceDataBatchProcessController batchJobController;
	
	@Autowired
	private CronRepository cronRepository;
	
	@Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		log.info("TeamsRawDataBatchProcessApplication.configureTasks() Entered");
        taskRegistrar.addTriggerTask(new Runnable() {
            @Override
            public void run() {
            	//run batch processing
            	log.info("Batch processing scheduler started at "+LocalDateTime.now());
            	ResponseEntity<?> response = batchJobController.rawDataBatchProcessing();
    			log.info("Status "+response.getStatusCodeValue()+" Response "+response.getBody());
    			log.info("batch processing scheduler stopped at : "+LocalDateTime.now());
            }
        }, new Trigger() {
            @Override
            public Date nextExecutionTime(TriggerContext triggerContext) {
                
            	//get cron time from db
              	String cronTime = cronRepository.getCronTime(1).getCronTime();
                CronTrigger trigger = new CronTrigger(cronTime);
                log.info("The scheduler is scheduled for next cron time : "+cronTime);
                Date nextExec = trigger.nextExecutionTime(triggerContext);  
                return nextExec;
            }
        });
    }
	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		// TODO Auto-generated method stub
		return builder.sources(TeamsRawDataBatchProcessApplication.class);
	}

	public static void main(String[] args) {
		System.out.println("TeamsRawDataBatchProcessApplication.main()");
		SpringApplication.run(TeamsRawDataBatchProcessApplication.class, args);
	}
	
	@Bean
	@LoadBalanced
	public RestTemplate createLoadBalancedRestTemplate() {
		log.info("Rest template bean created");
		return new RestTemplate();
	}

}
