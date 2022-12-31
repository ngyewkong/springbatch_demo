package com.ngyewkong.springbatchdemo.listener;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

// intercept the step for logging purposes
@Component
public class FirstStepListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        System.out.println("Before Step: " + stepExecution.getStepName());
        System.out.println("Job Execution Context: " + stepExecution.getJobExecution().getExecutionContext());
        System.out.println("Step Execution Context: " + stepExecution.getExecutionContext());

        // simulate having step execution context key-value pair
        stepExecution.getExecutionContext().put("testStepKey", "testStepValue");
    }

    // afterStep return an ExitStatus, afterJob return void
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        System.out.println("After Step: " + stepExecution.getStepName());
        System.out.println("Job Execution Context: " + stepExecution.getJobExecution().getExecutionContext());
        System.out.println("Step Execution Context: " + stepExecution.getExecutionContext());
        return null;
    }
}
