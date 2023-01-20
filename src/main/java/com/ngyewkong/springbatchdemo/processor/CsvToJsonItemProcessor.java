package com.ngyewkong.springbatchdemo.processor;

import com.ngyewkong.springbatchdemo.model.StudentCsv;
import com.ngyewkong.springbatchdemo.model.StudentJson;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class CsvToJsonItemProcessor implements ItemProcessor<StudentCsv, StudentJson> {
    @Override
    public StudentJson process(StudentCsv studentCsv) throws Exception {
        System.out.println("Inside Csv to Json Item Processor");
        StudentJson studentJson = new StudentJson();

        // simulate a nullPointer Exception during processing
        if(studentCsv.getId() == 6) {
            throw new NullPointerException();
        }

        // remap the fields from studentJdbc to studentJson
        // Student Json gt ignore property for firstName
        studentJson.setId(studentCsv.getId());
        studentJson.setLastName(studentCsv.getLastName());
        studentJson.setEmail(studentCsv.getEmail());

        return studentJson;
    }
}
