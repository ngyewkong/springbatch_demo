package com.ngyewkong.springbatchdemo.config;

import com.ngyewkong.springbatchdemo.listener.FirstJobListener;
import com.ngyewkong.springbatchdemo.listener.FirstStepListener;
import com.ngyewkong.springbatchdemo.model.StudentCsv;
import com.ngyewkong.springbatchdemo.processor.FirstItemProcessor;
import com.ngyewkong.springbatchdemo.reader.FirstItemReader;
import com.ngyewkong.springbatchdemo.service.SecondTasklet;
import com.ngyewkong.springbatchdemo.writer.FirstItemWriter;
import com.ngyewkong.springbatchdemo.writer.StudentCsvItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import java.io.File;

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

    // autowired the itemReader, itemProcessor, itemWriter used in chunk oriented step
    @Autowired
    private FirstItemReader firstItemReader;

    @Autowired
    private FirstItemProcessor firstItemProcessor;

    @Autowired
    private FirstItemWriter firstItemWriter;

    // sample job using tasklet
    // .incrementer()  to generate diff job instance id on each run of the springboot app
    // new RunIdIncrementer() generates the running id on each run
    // it implements the JobParametersIncrementer class
    // making each run having an unique job instance id starting from 1
    // .start(step()) -> first step
    // .next(anotherstep()) -> subsequent steps
    // .listener() -> to use custom JobListener that was implemented
    // .build() -> build the whole job from jobBuilderFactory

    // comment out the bean annotation to prevent running of first job which is tasklet
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

    // secondJob for chunk oriented step
    // can use a combination of chunk-oriented & tasklet step in the same job
    @Bean
    public Job secondJob() {
        return jobBuilderFactory.get("Second Job - Chunk")
                .incrementer(new RunIdIncrementer())
                .start(firstChunkStep())
                .next(secondStep())
                .build();
    }

    // chunk step need to define chunk size (how many records to process at one go
    // .<InputType, Output Type>chunk(chunkSize)
    // .reader(Reader).processor(Processor).writer(Writer).build() set the chunk step

    // itemProcessor is optional if there is no need to do processing between read and write
    // itemReader & itemWriter are mandatory for chunk-oriented step
    public Step firstChunkStep() {
        return stepBuilderFactory.get("First Chunk Step")
                .<Integer, Long>chunk(3)
                .reader(firstItemReader)
                .processor(firstItemProcessor)
                .writer(firstItemWriter)
                .build();
    }

    // set up third job for item readers section
    // autowired the custom csv item reader & writer
    // add the thirdJob to the JobService to use jobLauncher to run
    @Autowired
    private StudentCsvItemWriter studentCsvItemWriter;

    @Bean
    public Job thirdJob() {
        return jobBuilderFactory.get("ItemReadersDemoJob")
                .incrementer(new RunIdIncrementer())
                .start(secondChunkStep())
                .build();
    }

    public Step secondChunkStep() {
        return stepBuilderFactory.get("Item Readers Demo Step")
                .<StudentCsv, StudentCsv>chunk(4)
                .reader(flatFileItemReader())
                //.processor()
                .writer(studentCsvItemWriter)
                .build();
    }

    public FlatFileItemReader<StudentCsv> flatFileItemReader() {
        FlatFileItemReader<StudentCsv> flatFileItemReader =
                new FlatFileItemReader<StudentCsv>();

        // set the file path to read from
        flatFileItemReader.setResource(new FileSystemResource(
                new File("inputfiles/student.csv")));

        // initialize the lineMapper, lineTokenizer, fieldSetMapper
        DefaultLineMapper<StudentCsv> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        BeanWrapperFieldSetMapper<StudentCsv> fieldSetMapper = new BeanWrapperFieldSetMapper<>();

        // set the delimiter, names, target class
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setNames("ID", "First Name", "Last Name", "Email");
        fieldSetMapper.setTargetType(StudentCsv.class);

        // set it onto the initalized lineMapper
        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        flatFileItemReader.setLineMapper(lineMapper);

        // skip the column header in csv file which is the first line
        flatFileItemReader.setLinesToSkip(1);

        return flatFileItemReader;
    }

}
