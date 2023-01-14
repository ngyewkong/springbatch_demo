package com.ngyewkong.springbatchdemo.processor;

import com.ngyewkong.springbatchdemo.model.StudentJdbc;
import com.ngyewkong.springbatchdemo.model.StudentJson;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

// using item processor to change the model class from reader to writer
@Component
public class JdbcToJsonItemProcessor implements ItemProcessor<StudentJdbc, StudentJson> {
    @Override
    public StudentJson process(StudentJdbc studentJdbc) throws Exception {
        System.out.println("Inside Jdbc to Json Item Processor");
        StudentJson studentJson = new StudentJson();

        // remap the fields from studentJdbc to studentJson
        // Student Json gt ignore property for firstName
        studentJson.setId(studentJdbc.getId());
        studentJson.setLastName(studentJdbc.getLastName());
        studentJson.setEmail(studentJdbc.getEmail());

        return studentJson;
    }
}
