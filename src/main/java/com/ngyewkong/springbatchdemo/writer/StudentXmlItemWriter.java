package com.ngyewkong.springbatchdemo.writer;

import com.ngyewkong.springbatchdemo.model.StudentXml;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StudentXmlItemWriter implements ItemWriter<StudentXml> {

    @Override
    public void write(List<? extends StudentXml> students) throws Exception {
        System.out.println("Inside XML Item Writer");
        students.stream().forEach(System.out::println);
    }
}
