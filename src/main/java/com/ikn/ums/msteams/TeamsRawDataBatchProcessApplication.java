package com.ikn.ums.msteams;

import java.time.LocalDateTime;
import java.util.Date;
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
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
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
//@EnableScheduling
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
                
            	//get cron details from db, however only 1 cron will be present
            	List<CronDetails> dbCronList = cronRepository.findAll();
            	String cronTime = null;
            	if(dbCronList.size() > 0) {
            		cronTime = dbCronList.get(0).getCronTime();
            	}
            	//insert a cron into db if no cron is present in db
            	CronDetails savedCron = null;
            	CronTrigger trigger = null;
            	if(cronTime != "" && cronTime != null) {
            		trigger = new CronTrigger(cronTime);
             	}else {
             		CronDetails defaultCron = new CronDetails();
            		defaultCron.setCronTime("0 */5 * * * *");
            		savedCron = cronRepository.save(defaultCron);
            		log.info(" A default cron time is added by the task scheduler for batch processing ");
            		cronTime = savedCron.getCronTime();
            		trigger = new CronTrigger(cronTime);
            	}
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
