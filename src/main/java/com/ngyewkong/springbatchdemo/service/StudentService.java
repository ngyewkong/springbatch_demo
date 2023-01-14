package com.ngyewkong.springbatchdemo.service;

import com.ngyewkong.springbatchdemo.model.StudentResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class StudentService {

    // service to get students data from api call
    List<StudentResponse> studentsList;

    public List<StudentResponse> restCallGetStudents() {
        // use rest template to get students
        RestTemplate restTemplate = new RestTemplate();
        // restTemplate.getForObject return a list of the response object
        StudentResponse[] studentResponseArray =
                restTemplate.getForObject("http://localhost:8081/api/v1/students",
                        StudentResponse[].class);

        studentsList = new ArrayList<>();

        for (StudentResponse response: studentResponseArray) {
            studentsList.add(response);
        }

        return studentsList;
    }

    // set up method to read one student by one student
    // make sure it is returning one item
    // take in arguments from req
    public StudentResponse getStudent(long id, String reqHeader) {
        System.out.println("id = " + id + " argument passed in = " + reqHeader);
        // call the restCall service method when the list is null
        if (studentsList == null) {
            restCallGetStudents();
        }
        // return the first element when list is not empty
        if (studentsList != null && !studentsList.isEmpty()) {
            return studentsList.remove(0);
        }
        // return null when end of source
        return null;
    }
}
