package com.ngyewkong.springbatchdemo.processor;

import com.ngyewkong.springbatchdemo.model.StudentCsv;
import com.ngyewkong.springbatchdemo.model.StudentJdbc;
import com.ngyewkong.springbatchdemo.model.StudentJson;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class CsvToJdbcItemProcessor implements ItemProcessor<StudentCsv, StudentJdbc> {
    @Override
    public StudentJdbc process(StudentCsv studentCsv) throws Exception {
        System.out.println("Inside Csv to Jdbc Item Processor");
        StudentJdbc studentJdbc = new StudentJdbc();

        // remap the fields from studentCsv to studentJdbc
        studentJdbc.setId(studentCsv.getId());
        studentJdbc.setFirstName(studentCsv.getFirstName());
        studentJdbc.setLastName(studentCsv.getLastName());
        studentJdbc.setEmail(studentCsv.getEmail());

        return studentJdbc;
    }
}
