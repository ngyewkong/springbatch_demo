package com.ngyewkong.springbatchdemo.writer;

import com.ngyewkong.springbatchdemo.model.StudentJdbc;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StudentJdbcItemWriter implements ItemWriter<StudentJdbc> {
    @Override
    public void write(List<? extends StudentJdbc> students) throws Exception {
        System.out.println("Inside JDBC Item Writer");
        students.stream().forEach(System.out::println);
    }
}
