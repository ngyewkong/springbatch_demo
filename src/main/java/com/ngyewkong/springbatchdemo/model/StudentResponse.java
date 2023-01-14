package com.ngyewkong.springbatchdemo.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class StudentResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;

    @Override
    public String toString() {
        return "StudentResponse{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
