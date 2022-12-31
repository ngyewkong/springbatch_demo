package com.ngyewkong.springbatchdemo.config;

import com.ngyewkong.springbatchdemo.listener.FirstJobListener;
import com.ngyewkong.springbatchdemo.listener.FirstStepListener;
import com.ngyewkong.springbatchdemo.service.SecondTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// important to use configuration annotation
@Configuration
public class SampleJob {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    // implementing second tasklet and its logic in service layer
    @Autowired
    private SecondTasklet secondTasklet;

    // JobListener for firstJob
    @Autowired
    private FirstJobListener firstJobListener;

    // StepListener for firstStep
    @Autowired
    private FirstStepListener firstStepListener;

    // sample job using tasklet
    // .incrementer()  to generate diff job instance id on each run of the springboot app
    // new RunIdIncrementer() generates the running id on each run
    // it implements the JobParametersIncrementer class
    // making each run having an unique job instance id starting from 1
    // .start(step()) -> first step
    // .next(anotherstep()) -> subsequent steps
    // .listener() -> to use custom JobListener that was implemented
    // .build() -> build the whole job from jobBuilderFactory
    @Bean
    public Job firstJob() {
        return jobBuilderFactory.get("First Job")
                .incrementer(new RunIdIncrementer())
                .start(firstStep())
                .next(secondStep())
                .listener(firstJobListener)
                .build();
    }

    // first step using Tasklet
    // .listener() -> to use custom stepListener that was implemented
    private Step firstStep() {
        return stepBuilderFactory.get("First Step")
                .tasklet(firstTask())
                .listener(firstStepListener)
                .build();
    }

    // second step
    // using secondTasklet which is autowired from SecondTasklet in service layer
    private Step secondStep() {
        return stepBuilderFactory.get("Second Step")
                .tasklet(secondTasklet)
                .build();
    }

    private Tasklet firstTask() {
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
                System.out.println("This is first tasklet step");

                // using the step execution context that stored existing key-value pair in step
                // access in tasklet via chunkContext
                // .getStepContext().getStepExecutionContext() returns a HashMap of the key value pair
                System.out.println("Step Execution Context Key: testStepKey & Value: " + chunkContext.getStepContext().getStepExecutionContext().get("testStepKey"));
                return RepeatStatus.FINISHED;
            }
        };
    }

//    private Tasklet secondTask() {
//        return new Tasklet() {
//            @Override
//            public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {
//                System.out.println("This is second tasklet step");
//                return RepeatStatus.FINISHED;
//            }
//        };
//    }

}
