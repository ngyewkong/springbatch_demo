package com.ngyewkong.springbatchdemo.config;

import com.ngyewkong.springbatchdemo.listener.FirstJobListener;
import com.ngyewkong.springbatchdemo.listener.FirstStepListener;
import com.ngyewkong.springbatchdemo.model.StudentCsv;
import com.ngyewkong.springbatchdemo.model.StudentJdbc;
import com.ngyewkong.springbatchdemo.model.StudentJson;
import com.ngyewkong.springbatchdemo.model.StudentXml;
import com.ngyewkong.springbatchdemo.processor.FirstItemProcessor;
import com.ngyewkong.springbatchdemo.reader.FirstItemReader;
import com.ngyewkong.springbatchdemo.service.SecondTasklet;
import com.ngyewkong.springbatchdemo.writer.*;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.sql.DataSource;
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

    @Autowired
    private StudentJsonItemWriter studentJsonItemWriter;

    @Autowired
    private StudentXmlItemWriter studentXmlItemWriter;

    @Autowired
    private StudentJdbcItemWriter studentJdbcItemWriter;

    @Bean
    public Job thirdJob() {
        return jobBuilderFactory.get("ItemReadersDemoJob")
                .incrementer(new RunIdIncrementer())
                .start(secondChunkStep())
                .build();
    }

    public Step secondChunkStep() {
        return stepBuilderFactory.get("Item Readers Demo Step")
                //.<StudentCsv, StudentCsv>chunk(4)
                //// pass null as value will be read in from the jobParameters
                //.reader(flatFileItemReader(null))
                //.<StudentJson, StudentJson>chunk(4)
                //.reader(jsonItemReader(null))
                //.writer(studentJsonItemWriter)
                //.<StudentXml, StudentXml>chunk(4)
                //.reader(staxEventItemReader(null))
                //.writer(studentXmlItemWriter)
                .<StudentJdbc, StudentJdbc>chunk(4)
                .reader(jdbcCursorItemReader())
                .writer(studentJdbcItemWriter)
                //.processor()
                //.writer(studentCsvItemWriter)
                .build();
    }

    // setting FlatFileItemReader to read file based on key-value pair passed from jobParameters
    // @Value("#{jobParameters['keyName']}") FileSystemResource fileSystemResource
    // need @Bean & @StepScope annotation for the FlatFileItemReader to read the filepath from prog argument
    // need to be in context -> @Bean
    // need to specify this reader is within the scope of the step
    @Bean
    @StepScope
    public FlatFileItemReader<StudentCsv> flatFileItemReader(
            @Value("#{jobParameters['inputCsvFile']}") FileSystemResource fileSystemResource) {
        FlatFileItemReader<StudentCsv> flatFileItemReader =
                new FlatFileItemReader<StudentCsv>();

        // set the file path to read from
        // can use ClassPathResource as well
        flatFileItemReader.setResource(fileSystemResource);

        // initialize the lineMapper, lineTokenizer, fieldSetMapper
        DefaultLineMapper<StudentCsv> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        BeanWrapperFieldSetMapper<StudentCsv> fieldSetMapper = new BeanWrapperFieldSetMapper<>();

        // set the delimiter, names, target class
        // can be comma -> , or pipe -> | or ... depending on the flat file separator used
        lineTokenizer.setDelimiter(",");
        // setNames will change the order of the header being passed
        // swapping lastname with email and check the println
        // the column headers qty must match with the setNames strings qty that are passed in
        // csv has 4 columns but setNames 3 strings -> raise FlatFileParseException
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

    // Json Item Reader
    // same annotation as flat file item reader for using file system resource
    // Jackson lib to handle json
    @Bean
    @StepScope
    public JsonItemReader<StudentJson> jsonItemReader(
            @Value("#{jobParameters['inputJsonFile']}") FileSystemResource fileSystemResource) {
        JsonItemReader<StudentJson> jsonItemReader = new JsonItemReader<>();
        jsonItemReader.setResource(fileSystemResource);

        // .setJsonObjectReader(new JacksonJsonObjectReader<>(model.class))
        // to use jackson lib
        jsonItemReader.setJsonObjectReader(
                new JacksonJsonObjectReader<>(StudentJson.class)
        );

        // set the max item count to read
        // .setMaxItemCount(number of items)
        jsonItemReader.setMaxItemCount(7);

        // set top items to skip
        // .setCurrentItemCount(2) skip first 2 items start from 3
        jsonItemReader.setCurrentItemCount(2);

        return jsonItemReader;
    }

    // Xml Item Reader
    // Stax -> Streaming API for XML provided by Spring Batch
    @Bean
    @StepScope
    public StaxEventItemReader<StudentXml> staxEventItemReader(
            @Value("#{jobParameters['inputXmlFile']}") FileSystemResource fileSystemResource) {
        StaxEventItemReader<StudentXml> staxEventItemReader =
                new StaxEventItemReader<>();
        staxEventItemReader.setResource(fileSystemResource);

        // .setFragmentRootElementName("student") -> follow xml
        staxEventItemReader.setFragmentRootElementName("student");

        // set the Jaxb2Marshaller with model class
        // .setClassesToBeBound()
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setClassesToBeBound(StudentXml.class);
        staxEventItemReader.setUnmarshaller(jaxb2Marshaller);

        return staxEventItemReader;
    }

    // autowired the datasource from application.properties
    // for one datasource it will take the spring.datasource properties by default
    @Autowired
    private DataSource dataSource;

    // Jdbc Item Reader
    public JdbcCursorItemReader<StudentJdbc> jdbcCursorItemReader() {
        JdbcCursorItemReader<StudentJdbc> jdbcCursorItemReader = new JdbcCursorItemReader<>();
        jdbcCursorItemReader.setDataSource(dataSource);
        // set the sql query
        // must use aliases to match db col name with model variable name
        // if sql statement do not match the model class -> missing fields will be in null
        jdbcCursorItemReader.setSql(
                //"SELECT firstName, last_Name, email FROM student"
                "SELECT id, firstName, last_name AS lastName, email FROM student"
        );

        // using beanPropertyRowMapper to set the row mapper using model class
        BeanPropertyRowMapper<StudentJdbc> beanPropertyRowMapper = new BeanPropertyRowMapper<>();
        beanPropertyRowMapper.setMappedClass(StudentJdbc.class);
        jdbcCursorItemReader.setRowMapper(beanPropertyRowMapper);

        // skip top n rows
        jdbcCursorItemReader.setCurrentItemCount(2); // start from 3
        // read max rows
        jdbcCursorItemReader.setMaxItemCount(7); // read until 7th row

        return jdbcCursorItemReader;
    }

}
