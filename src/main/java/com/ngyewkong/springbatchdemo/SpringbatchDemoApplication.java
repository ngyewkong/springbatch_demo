package com.ngyewkong.springbatchdemo;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

// @EnableAsync annotation to allow for async behaviour
@EnableAsync
@EnableBatchProcessing
@ComponentScan("com.ngyewkong")
@SpringBootApplication
public class SpringbatchDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbatchDemoApplication.class, args);
	}

}
