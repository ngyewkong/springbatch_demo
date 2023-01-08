package com.ngyewkong.springbatchdemo.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

// to use @XmlRootElement need to add maven dependencies for > java8
@XmlRootElement(name = "student")
public class StudentXml {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // to read xml that has diff name mapping
    // eg <f_n> -> firstName
    // use @XmlElement(name="f_n") annotation on the get method
    @XmlElement(name = "f_n")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

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
        return "StudentXml{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
