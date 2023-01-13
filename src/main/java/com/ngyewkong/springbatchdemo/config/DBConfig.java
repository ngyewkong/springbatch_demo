package com.ngyewkong.springbatchdemo.config;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DBConfig {
    // setting up the DataSources beans in a separate class to prevent
    // "The dependencies of some of the beans in the application context form a cycle"
    // for multi-datasources cannot just use @Autowired

    // setting the DataSourceProperties to prevent error arising from InitializeDataSourceBuilder
    // set your own DataSourceProperties override the default DataSourceBuilder
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSourceProperties batchMetaDataSourceProperties() {
        return new DataSourceProperties();
    }

    // setting the datasource that is used to store spring batch metadata
    // use @Primary annotation
    @Bean("batchMetaDataSource")
    @Primary
    public DataSource batchMetaDataSource() {
        return batchMetaDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.anotherdatasource")
    public DataSourceProperties anotherDataSourceProperties() {
        return new DataSourceProperties();
    }
    // actual table for data
    @Bean("dataTableDataSource")
    public DataSource anotherDataSource() {
        return anotherDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }


}
