package com.ngyewkong.springbatchdemo.controller;

import com.ngyewkong.springbatchdemo.request.JobParamsRequest;
import com.ngyewkong.springbatchdemo.service.JobService;
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
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/job")
public class JobController {

    // autowired the JobService class
    @Autowired
    private JobService jobService;

    // GetMapping with route /api/job/start/jobName
    // {jobName} is being accessed using @PathVariable annotation jobName will be populated by {jobName}
    // @RequestBody will pass the json body into the List<JobParamsRequest>
    @GetMapping("/start/{jobName}")
    public String startJob(@PathVariable String jobName,
                           @RequestBody List<JobParamsRequest> jobParamsList) throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {

        // call the JobService instance and the startJob() method
        // takes in the jobParamsList that is passed in from @RequestBody annotation
        jobService.startJob(jobName, jobParamsList);

        return "Job Started...";
    }

}
