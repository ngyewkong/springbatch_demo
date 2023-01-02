package com.ngyewkong.springbatchdemo.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/job")
public class JobController {

    // need to autowire joblauncher object, jobs in configuration
    @Autowired
    private JobLauncher jobLauncher;

    // use @Qualifier to prevent ambiguity since there are multiple Jobs Bean
    // takes the name of the class firstJob() -> "firstJob"
    // without @Qualifier spring will not be able to perform dependency injection
    @Qualifier("firstJob")
    @Autowired
    private Job firstJob;

    @Qualifier("secondJob")
    @Autowired
    private Job secondJob;

    // GetMapping with route /api/job/start/jobName
    // {jobName} is being accessed using @PathVariable annotation jobName will be populated by {jobName}
    @GetMapping("/start/{jobName}")
    public String startJob(@PathVariable String jobName) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {

        // job parameters setup
        // Map with <key, value> whereby value is JobParameter type
        // setting params with current time in millisec -> jobInstance will be unique every single time
        Map<String, JobParameter> params = new HashMap<>();
        params.put("currentTime", new JobParameter(System.currentTimeMillis()));

        // JobParameters take in a HashMap of keyvalue pair
        JobParameters jobParameters = new JobParameters(params);

        // does a check with the name provided in JobBuilderFactory.get("Job Name")
        if (jobName.equals("First Job")) {
            // jobLauncher takes in 2 arguments - jobObject & jobParameters
            jobLauncher.run(firstJob, jobParameters);
        } else if (jobName.equals("Second Job - Chunk")) {
            // jobLauncher takes in 2 arguments - jobObject & jobParameters
            jobLauncher.run(secondJob, jobParameters);
        }

        return "Job Started...";
    }

}
