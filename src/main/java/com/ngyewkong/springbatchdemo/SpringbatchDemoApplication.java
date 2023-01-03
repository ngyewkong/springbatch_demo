package com.ngyewkong.springbatchdemo;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

// @EnableAsync annotation to allow for async behaviour
// @EnableScheduling annotation to use Spring Scheduler to schedule Jobs
@EnableScheduling
@EnableAsync
@EnableBatchProcessing
@ComponentScan("com.ngyewkong")
@SpringBootApplication
public class SpringbatchDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbatchDemoApplication.class, args);
	}

}
