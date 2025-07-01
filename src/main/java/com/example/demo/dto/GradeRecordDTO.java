package com.example.demo.dto;

public class GradeRecordDTO {
    private String studentId;
    private String lastName;
    private String firstName;
    private Double score;
    private String gradeLetter;

    // Constructors
    public GradeRecordDTO() {
    }

    public GradeRecordDTO(String studentId, String lastName, String firstName, Double score, String gradeLetter) {
        this.studentId = studentId;
        this.lastName = lastName;
        this.firstName = firstName;
        this.score = score;
        this.gradeLetter = gradeLetter;
    }

    // Getters & setters
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getGradeLetter() {
        return gradeLetter;
    }

    public void setGradeLetter(String gradeLetter) {
        this.gradeLetter = gradeLetter;
    }
}
