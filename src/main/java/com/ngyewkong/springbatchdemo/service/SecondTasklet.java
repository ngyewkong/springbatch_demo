package com.ngyewkong.springbatchdemo.service;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Service;

// class is implementing the Tasklet interface
@Service
public class SecondTasklet implements Tasklet {
    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
        System.out.println("This is second tasklet step");

        // to simulate using the jobExecutionContext key-value pair in Step
        // chunkContext.getStepContextt().getJobExecutionContext()
        System.out.println(chunkContext.getStepContext().getJobExecutionContext());
        return RepeatStatus.FINISHED;
    }
}
