package com.ngyewkong.springbatchdemo.config;

import com.ngyewkong.springbatchdemo.listener.FirstJobListener;
import com.ngyewkong.springbatchdemo.listener.FirstStepListener;
import com.ngyewkong.springbatchdemo.listener.SkipListener;
import com.ngyewkong.springbatchdemo.model.*;
import com.ngyewkong.springbatchdemo.processor.*;
import com.ngyewkong.springbatchdemo.reader.FirstItemReader;
import com.ngyewkong.springbatchdemo.service.SecondTasklet;
import com.ngyewkong.springbatchdemo.service.StudentService;
import com.ngyewkong.springbatchdemo.writer.*;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.skip.AlwaysSkipItemSkipPolicy;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.adapter.ItemReaderAdapter;
import org.springframework.batch.item.adapter.ItemWriterAdapter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.ItemPreparedStatementSetter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.*;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.Writer;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

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

    @Autowired
    private StudentResponseItemWriter studentResponseItemWriter;

    @Autowired
    private CsvToJsonItemProcessor csvToJsonItemProcessor;

    // autowried the skip listener
    @Autowired
    private SkipListener skipListener;

    @Bean
    public Job thirdJob() {
        return jobBuilderFactory.get("ItemReadersDemoJob")
                .incrementer(new RunIdIncrementer())
                .start(secondChunkStep())
                .build();
    }

    public Step secondChunkStep() {
        return stepBuilderFactory.get("Item Readers Demo Step")
                .<StudentCsv, StudentJson>chunk(4)
                //.<StudentJson, StudentJson>chunk(4)
                //.<StudentXml, StudentXml>chunk(4)
                //.<StudentJdbc, StudentJdbc>chunk(4)
                //.<StudentResponse, StudentResponse>chunk(4)
                //.<StudentJdbc, StudentJson>chunk(4)
                //.<StudentJdbc, StudentXml>chunk(4)
                //.<StudentCsv, StudentJdbc>chunk(4)
                // pass null as value will be read in from the jobParameters
                .reader(flatFileItemReader(null))
                //.reader(jsonItemReader(null))
                //.reader(staxEventItemReader(null))
                // using the jdbc reader to get from db for itemwriter egs
                //.reader(jdbcCursorItemReader())
                //.reader(itemReaderAdapter())
                .processor(csvToJsonItemProcessor)
                //.processor(jdbcToJsonItemProcessor)
                //.processor(jdbcToXmlItemProcessor)
                //.processor(csvToJdbcItemProcessor)
                //.writer(flatFileItemWriter(null))
                .writer(jsonFileItemWriter(null))
                //.writer(staxEventItemWriter(null))
                //.writer(jdbcBatchItemWriter())
                //.writer(itemWriterAdapter())
                //.writer(studentCsvItemWriter())
                //.writer(studentJsonItemWriter)
                //.writer(studentXmlItemWriter)
                //.writer(studentJdbcItemWriter)
                //.writer(studentResponseItemWriter)
                // for fault tolerance
                // .faultTolerant()
                // .skip(Throwable.class)
                // .skipLimit() how many bad records to skip
                // use Integer.Max_Value for unknown bad record qty
                // can also use skipPolicy(new AlwaysSkipItemSkipPolicy())
                .faultTolerant()
                //.skip(FlatFileParseException.class)
                // for multi exception handling
                .skip(Throwable.class)
                //.skipLimit(Integer.MAX_VALUE)
                .skipPolicy(new AlwaysSkipItemSkipPolicy())
                .listener(skipListener)
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

        // set it onto the initialized lineMapper
        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        flatFileItemReader.setLineMapper(lineMapper);

        // skip the column header in csv file which is the first line
        flatFileItemReader.setLinesToSkip(1);

        return flatFileItemReader;
    }

    // Csv Item Writer
    @Bean
    @StepScope
    public FlatFileItemWriter<StudentJdbc> flatFileItemWriter(
            @Value("#{jobParameters['outputCsvFile']}") FileSystemResource fileSystemResource
    ) {
        FlatFileItemWriter<StudentJdbc> flatFileItemWriter = new FlatFileItemWriter<>();
        flatFileItemWriter.setResource(fileSystemResource);
        // set the csv header columns
        flatFileItemWriter.setHeaderCallback(new FlatFileHeaderCallback() {
            @Override
            public void writeHeader(Writer writer) throws IOException {
                writer.write("Id,First Name,Last Name,Email");
            }
        });

        // set the line aggregator for each record
        DelimitedLineAggregator<StudentJdbc> delimitedLineAggregator = new DelimitedLineAggregator<>();
        BeanWrapperFieldExtractor<StudentJdbc> beanWrapperFieldExtractor = new BeanWrapperFieldExtractor<>();
        // setNames to use String Array that matches the model class variables name
        beanWrapperFieldExtractor.setNames(new String[] {
                "id", "firstName", "lastName", "email"
        });

        // set the field extractor for delimited line aggregator
        delimitedLineAggregator.setDelimiter("|");
        delimitedLineAggregator.setFieldExtractor(beanWrapperFieldExtractor);
        flatFileItemWriter.setLineAggregator(delimitedLineAggregator);

        // set the footer or the last line of the csv writer
        flatFileItemWriter.setFooterCallback(new FlatFileFooterCallback() {
            @Override
            public void writeFooter(Writer writer) throws IOException {
                writer.write("Created at " + new Date());
            }
        });

        return flatFileItemWriter;
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

    // Jdbc to Json Item Processor
    @Autowired
    private JdbcToJsonItemProcessor jdbcToJsonItemProcessor;

    // Json Item Writer
    @Bean
    @StepScope
    public JsonFileItemWriter<StudentJson> jsonFileItemWriter(
            @Value("#{jobParameters['outputJsonFile']}") FileSystemResource fileSystemResource
    ) {
        // constructor takes in 2 arguments - resource and JsonObjectMarshaller
        // using JacksonJsonObjectMarshaller to map jdbc data to json object
        JsonFileItemWriter<StudentJson> jsonFileItemWriter =
                new JsonFileItemWriter<>(fileSystemResource,
                        new JacksonJsonObjectMarshaller<>());

        return jsonFileItemWriter;
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

    // Jdbc to Xml Item Processor
    @Autowired
    private JdbcToXmlItemProcessor jdbcToXmlItemProcessor;

    // XML Item Writer
    @Bean
    @StepScope
    public StaxEventItemWriter<StudentXml> staxEventItemWriter(
            @Value("#{jobParameters['outputXmlFile']}") FileSystemResource fileSystemResource
    ) {
        StaxEventItemWriter<StudentXml> staxEventItemWriter = new StaxEventItemWriter<>();
        staxEventItemWriter.setResource(fileSystemResource);

        // setRootTagName() set the root <> to students
        staxEventItemWriter.setRootTagName("students");
        // set the individual element tag in the root tag using the studentXml.class
        // via Jaxb2Marshaller to map from jdbc to xml
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setClassesToBeBound(StudentXml.class);

        // set the marshaller to the item writer
        staxEventItemWriter.setMarshaller(jaxb2Marshaller);

        return staxEventItemWriter;
    }

    // autowired the datasource from application.properties
    // for one datasource it will take the spring.datasource properties by default
    // use @Qualifier with ref to the @Bean("name") to autowired correctly
    @Autowired
    @Qualifier("batchMetaDataSource")
    DataSource batchMetaDataSource;

    @Autowired
    @Qualifier("dataTableDataSource")
    DataSource actualDataSource;

    // Jdbc Item Reader
    public JdbcCursorItemReader<StudentJdbc> jdbcCursorItemReader() {
        JdbcCursorItemReader<StudentJdbc> jdbcCursorItemReader = new JdbcCursorItemReader<>();
        jdbcCursorItemReader.setDataSource(actualDataSource);
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

    // Csv to Jdbc Item Processor
    @Autowired
    private CsvToJdbcItemProcessor csvToJdbcItemProcessor;

    // Jdbc Item Writer
    @Bean
    public JdbcBatchItemWriter<StudentJdbc> jdbcBatchItemWriter() {
        JdbcBatchItemWriter<StudentJdbc> jdbcBatchItemWriter = new JdbcBatchItemWriter<>();

        // set the datasource to write to -> actualDataSource
        jdbcBatchItemWriter.setDataSource(actualDataSource);
        // set the SQL statement to insert the data into datasource
        // INSERT INTO table_name(table_col1, table_col2, ...)
        // VALUES (:id, ...) -> follows the model class attributes
//        jdbcBatchItemWriter.setSql(
//                "INSERT INTO student(id, firstName, last_name, email)" +
//                        "VALUES (:id, :firstName, :lastName, :email)"
//        );
//
//        // set the SQL Statement to follow StudentJdbc class
//        jdbcBatchItemWriter.setItemSqlParameterSourceProvider(
//                new BeanPropertyItemSqlParameterSourceProvider<StudentJdbc>()
//        );

        // 2nd method to load sql statement via Prepared Statement
        jdbcBatchItemWriter.setSql(
                "INSERT INTO student(id, firstName, last_name, email)" +
                        "VALUES (?,?,?,?)"
        );

        // setItemPreparedStatementSetter()
        // setValues(ps.setLong(col_index)) -> col_index starts from 1
        jdbcBatchItemWriter.setItemPreparedStatementSetter(
                (studentJdbc, preparedStatement) -> {
                    preparedStatement.setLong(1, studentJdbc.getId());
                    preparedStatement.setString(2, studentJdbc.getFirstName());
                    preparedStatement.setString(3, studentJdbc.getLastName());
                    preparedStatement.setString(4, studentJdbc.getEmail());
                }
        );

        return jdbcBatchItemWriter;
    }

    // autowired StudentService
    @Autowired
    private StudentService studentService;

    // rest api datasource item reader adapter
    public ItemReaderAdapter<StudentResponse> itemReaderAdapter() {
        ItemReaderAdapter<StudentResponse> itemReaderAdapter = new ItemReaderAdapter<>();

        // invoke the method in JobService -> restCallGetStudents()
        // via .setTargetObject(service.class)
        // via .setTargetMethod()
        // but cannot directly use the method defined in StudentService
        // no logic to manage the reading of records by row
        // no logic to manage return null when end of source
        itemReaderAdapter.setTargetObject(studentService);
        // this will get the getStudent() method from studentService
        itemReaderAdapter.setTargetMethod("getStudent");
        // add arguments
        itemReaderAdapter.setArguments(new Object[] {
                1L, "Add Arguments"
        });

        return itemReaderAdapter;
    }

    // rest api item writer
    public ItemWriterAdapter<StudentCsv> itemWriterAdapter() {
        ItemWriterAdapter<StudentCsv> itemWriterAdapter = new ItemWriterAdapter<>();

        // set the service in TargetObject
        // set the method in Student Service
        // do not need SetArguments unlike ItemReaderAdapter as we are getting from the item reader
        // in this case csv item reader
        itemWriterAdapter.setTargetObject(studentService);
        itemWriterAdapter.setTargetMethod("restCallCreateStudent");

        return itemWriterAdapter;
    }

}
