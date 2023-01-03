package com.ngyewkong.springbatchdemo.controller;

import com.ngyewkong.springbatchdemo.request.JobParamsRequest;
import com.ngyewkong.springbatchdemo.service.JobService;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job")
public class JobController {

    // autowired the JobService class
    @Autowired
    JobService jobService;

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

    // Stop Job
    // need to use JobOperator object to stop
    // .stop() method takes in job executionId
    @Autowired
    private JobOperator jobOperator;

    @GetMapping("/stop/{jobExecutionId}")
    public String stopJob(@PathVariable long jobExecutionId) throws NoSuchJobExecutionException, JobExecutionNotRunningException {
        jobOperator.stop(jobExecutionId);

        return "Job Stopped...";
    }

}
