package com.ngyewkong.springbatchdemo.processor;

import com.ngyewkong.springbatchdemo.model.StudentJdbc;
import com.ngyewkong.springbatchdemo.model.StudentJson;
import com.ngyewkong.springbatchdemo.model.StudentXml;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class JdbcToXmlItemProcessor implements ItemProcessor<StudentJdbc, StudentXml> {
    @Override
    public StudentXml process(StudentJdbc studentJdbc) throws Exception {
        System.out.println("Inside Jdbc to Xml Item Processor");
        StudentXml studentXml = new StudentXml();

        // remap the fields from studentJdbc to studentXml
        studentXml.setId(studentJdbc.getId());
        studentXml.setFirstName(studentJdbc.getFirstName());
        studentXml.setLastName(studentJdbc.getLastName());
        studentXml.setEmail(studentJdbc.getEmail());

        return studentXml;
    }
}
