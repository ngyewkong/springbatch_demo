package com.ngyewkong.springbatchdemo.service;

import com.ngyewkong.springbatchdemo.request.JobParamsRequest;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JobService {

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

    @Qualifier("thirdJob")
    @Autowired
    private Job thirdJob;

    // using @Async annotation here to make this startJob method async
    @Async
    public void startJob(String jobName, List<JobParamsRequest> jobParamsRequestList) {
        // job parameters setup
        // Map with <key, value> whereby value is JobParameter type
        // setting params with current time in millisec -> jobInstance will be unique every single time
        Map<String, JobParameter> params = new HashMap<>();
        params.put("currentTime", new JobParameter(System.currentTimeMillis()));
        // this will read the respective file from the filepath given in JobParameter value
        params.put("inputCsvFile", new JobParameter("/Users/ngyewkong/IdeaProjects/springbatch-demo/inputfiles/student.csv"));
        //params.put("outputCsvFile", new JobParameter("/Users/ngyewkong/IdeaProjects/springbatch-demo/outputfiles/students.csv"));
        //params.put("inputJsonFile", new JobParameter("inputfiles/student.json"));
        //params.put("outputJsonFile", new JobParameter("/Users/ngyewkong/IdeaProjects/springbatch-demo/outputfiles/students.json"));
        //params.put("inputXmlFile", new JobParameter("inputfiles/student.xml"));
        //params.put("outputXmlFile", new JobParameter("/Users/ngyewkong/IdeaProjects/springbatch-demo/outputfiles/students.xml"));
        params.put("jdbcFromDB", new JobParameter("jdbcDataFromMySQLDB"));
        //params.put("restapidata", new JobParameter("dataFromRestApi"));

        // use the jobParamsList that is passed in
        // add it into the params HashMap which store as key-value pairs
        // rmb jobParamsRequest.getParamValue returns a String but params has type JobParameter
        // initialize new JobParameter to recast to JobParameter type
        if (jobParamsRequestList != null) {
            jobParamsRequestList.stream()
                    .forEach(jobParamsRequest -> {
                        params.put(jobParamsRequest.getParamKey(),
                                new JobParameter(jobParamsRequest.getParamValue()));
                    });
        }

        // JobParameters take in a HashMap of keyvalue pair
        JobParameters jobParameters = new JobParameters(params);

        try {
            JobExecution jobExecution = null;
            // does a check with the name provided in JobBuilderFactory.get("Job Name")
            if (jobName.equals("First Job")) {
                // jobLauncher.run() returns JobExecution class which we can use to get JobId etc
                // jobLauncher takes in 2 arguments - jobObject & jobParameters
                jobExecution = jobLauncher.run(firstJob, jobParameters);
            } else if (jobName.equals("Second Job - Chunk")) {
                // jobLauncher takes in 2 arguments - jobObject & jobParameters
                jobExecution = jobLauncher.run(secondJob, jobParameters);
            } else if (jobName.equals("ItemReadersDemoJob")) {
                jobExecution = jobLauncher.run(thirdJob, jobParameters);
            }
            System.out.println("Job Instance ID = " + jobExecution.getJobId());
        } catch (Exception e) {
            System.out.println("Exception while starting Job");
        }


    }
}
