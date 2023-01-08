package com.ngyewkong.springbatchdemo.writer;

import com.ngyewkong.springbatchdemo.model.StudentCsv;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StudentCsvItemWriter implements ItemWriter<StudentCsv> {
    @Override
    public void write(List<? extends StudentCsv> students) throws Exception {
        System.out.println("Inside CSV Item Writer");
        students.stream().forEach(System.out::println);
    }
}
