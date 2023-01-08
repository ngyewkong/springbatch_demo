package com.ngyewkong.springbatchdemo.writer;

import com.ngyewkong.springbatchdemo.model.StudentJson;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StudentJsonItemWriter implements ItemWriter<StudentJson> {
    @Override
    public void write(List<? extends StudentJson> students) throws Exception {
        System.out.println("Inside JSON Item Writer");
        students.stream().forEach(System.out::println);
    }
}
