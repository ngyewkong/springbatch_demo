package com.ngyewkong.springbatchdemo.listener;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

// to intercept and listen for a particular job
// for logging or other purposes
// implements the JobExecutionListener interface from spring batch
// override two methods - beforeJob & afterJob
@Component
public class FirstJobListener implements JobExecutionListener {

    // before job execution
    @Override
    public void beforeJob(JobExecution jobExecution) {
        System.out.println("Before Job: " + jobExecution.getJobInstance().getJobName());
        System.out.println("Job Parameters: " + jobExecution.getJobParameters());
        System.out.println("Job Level Execution Context: " + jobExecution.getExecutionContext());

        // putting key-value pair into job execution context before job execute
        // afterJob will have the key-value pair in jobExecutionContext
        jobExecution.getExecutionContext().put("testKey", "randomValue");
    }

    // after job execution
    @Override
    public void afterJob(JobExecution jobExecution) {
        System.out.println("After Job: " + jobExecution.getJobInstance().getJobName());
        System.out.println("Job Parameters: " + jobExecution.getJobParameters());
        System.out.println("Job Level Execution Context: " + jobExecution.getExecutionContext());
    }
}
