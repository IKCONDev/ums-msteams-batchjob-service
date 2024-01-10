package com.ikn.ums.msteams;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.web.client.RestTemplate;

import com.ikn.ums.msteams.controller.TeamsSourceDataBatchProcessController;
import com.ikn.ums.msteams.entity.CronDetails;
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
	
	@Autowired
	private Environment environment;
	
	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
	    log.info("configureTasks() Entered"); 
	    taskRegistrar.addTriggerTask(() -> {
	        // Run batch processing
	        log.info("Batch processing scheduler started at " + LocalDateTime.now());
	        ResponseEntity<?> response = batchJobController.rawDataBatchProcessing();
	        log.info("Status " + response.getStatusCodeValue() + " Response " + response.getBody());
	        log.info("Batch processing scheduler stopped at : " + LocalDateTime.now());
	    }, triggerContext -> {
	        // Get cron details from db, however only 1 cron will be present
	        List<CronDetails> dbCronList = cronRepository.findAll();
	        String cronTime = dbCronList.isEmpty() ? null : dbCronList.get(0).getCronTime();

	        // Insert a cron into db if no cron is present in db
	        CronDetails savedCron = null;
	        CronTrigger trigger = null;

	        if (cronTime != null && !cronTime.isEmpty()) {
	            trigger = new CronTrigger(cronTime);
	        } else {
	            CronDetails defaultCron = new CronDetails();
	            defaultCron.setCronTime(environment.getProperty("batch.process.default.crontime"));
	            defaultCron.setHour(environment.getProperty("batch.process.default.crontime.hour"));
	            savedCron = cronRepository.save(defaultCron);
	            log.info("Since there is no cron expression found in db, A default cron time is added by the task scheduler for batch processing :"
	                    + savedCron.getCronTime());

	            cronTime = savedCron.getCronTime();
	            trigger = new CronTrigger(cronTime);
	        }

	        log.info("The scheduler is scheduled for the next cron time : " + cronTime);
	        return trigger.nextExecutionTime(triggerContext);
	    });
	}

	
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		log.info("configure() entered");
		return builder.sources(TeamsRawDataBatchProcessApplication.class);
	}

	public static void main(String[] args) {
		log.info("main() entered");
		SpringApplication.run(TeamsRawDataBatchProcessApplication.class, args);
	}
	
	@Bean
	@LoadBalanced
	public RestTemplate createLoadBalancedRestTemplate() {
		log.info("Rest template bean created");
		return new RestTemplate();
	}

}
