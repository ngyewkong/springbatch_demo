package com.ngyewkong.springbatchdemo.service;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

// every job need its own scheduler
// @Service annotation
@Service
public class SecondJobScheduler {

    // autowired the jobLauncer, Job as per normal rest call to manually run job
    @Autowired
    private JobLauncher jobLauncher;

    @Qualifier("secondJob")
    @Autowired
    private Job secondJob;

    // @Scheduled(cron exp) to create the schedule based on cron
    // cron in spring is 6 characters
    // go google the respective cron exp for what you need
    // 0 0/1 * 1/1 * ? -> runs every minute
    @Scheduled(cron = "0 0/1 * 1/1 * ?")
    public void secondJobStarter() {
        // job parameters setup
        // Map with <key, value> whereby value is JobParameter type
        // setting params with current time in millisec -> jobInstance will be unique every single time
        Map<String, JobParameter> params = new HashMap<>();
        params.put("currentTime", new JobParameter(System.currentTimeMillis()));

        // JobParameters take in a HashMap of keyvalue pair
        JobParameters jobParameters = new JobParameters(params);

        // only need jobLauncher.run()
        // no more checks to see if endpoint is hit as it is currently scheduled at 1min interval
        try {
            // jobLauncher.run() returns JobExecution class which we can use to get JobId etc
            // jobLauncher takes in 2 arguments - jobObject & jobParameters
            JobExecution jobExecution = jobLauncher.run(secondJob, jobParameters);
            System.out.println("Job Instance ID = " + jobExecution.getJobId());
        } catch (Exception e) {
            System.out.println("Exception while starting Job");
        }


    }
}
