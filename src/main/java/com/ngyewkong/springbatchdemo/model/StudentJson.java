package com.ngyewkong.springbatchdemo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// if we want to read partially
// need to use @JsonIgnoreProperties(ignoreUnknown = true) annotation
// and remove the variable mapping in the model class
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentJson {

    // need to make sure the variable name in model class
    // match with the keyName in json
    // if not -> ParseException
    // or use @JsonProperty() annotation to match the key
    private Long id;

    // eg do not want the firstName being read
    // comment out the variable getter & setter and toString
    // private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private String email;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

//    public String getFirstName() {
//        return firstName;
//    }
//
//    public void setFirstName(String firstName) {
//        this.firstName = firstName;
//    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "StudentJson{" +
                "id=" + id +
//                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
