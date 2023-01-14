package com.ngyewkong.springbatchdemo.writer;

import com.ngyewkong.springbatchdemo.model.StudentResponse;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StudentResponseItemWriter implements ItemWriter<StudentResponse> {
    @Override
    public void write(List<? extends StudentResponse> students) throws Exception {
        System.out.println("Inside Rest Response Item Writer");
        students.stream().forEach(System.out::println);
    }
}
