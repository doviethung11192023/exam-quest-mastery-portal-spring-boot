package com.example.demo.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.ExamDetailDTO;
import com.example.demo.dto.StudentExamResultDTO;
import com.example.demo.repository.StudentResultRepository;

@Service
public class StudentResultService {
    
    @Autowired
    private StudentResultRepository studentResultRepository;

    public List<StudentExamResultDTO> getExamResults(String classId, String subjectId, String level, String teacherId) {
        System.out.println("Fetching exam results for classId: " + classId + ", subjectId: " + subjectId + ", level: " + level + ", teacherId: " + teacherId);
        List<Object[]> rows = studentResultRepository.getExamResultsByFilter(classId, subjectId, level, teacherId);
        // Log the number of rows fetched
        if (rows == null) {
            System.out.println("No rows fetched, returning empty list.");
            return new ArrayList<>();
        }
        System.out.println("Number of rows fetched: " + rows.size());
        if (rows.isEmpty()) {
            return new ArrayList<>();
        }
        // Create a map to hold the student results
        Map<String, StudentExamResultDTO> studentMap = new LinkedHashMap<>();
        // Iterate through the rows and populate the map
        // Each row contains the following columns:

        for (Object[] row : rows) {
        System.out.println("Row data: " + java.util.Arrays.toString(row));
        // Assuming the columns are in the following order:
        String studentId = (String) row[0];
        String studentName = (String) row[1];
        String examDate = row[2].toString();
        String attempt = row[3].toString();
        System.out.println("Student ID: " + studentId + ", Name: " + studentName + ", Exam Date: " + examDate + ", Attempt: " + attempt);
        // Check if the student already exists in the map
        StudentExamResultDTO dto = studentMap.computeIfAbsent(studentId, id -> {
            StudentExamResultDTO s = new StudentExamResultDTO();
            s.setStudentId(id);
            s.setStudentName(studentName);
            s.setExamDate(examDate);
            s.setAttempt(attempt);
            s.setDetails(new ArrayList<>());
            return s;
        });

        dto.getDetails().add(new ExamDetailDTO(
            row[4].toString(), // questionNumber
            row[5].toString(), // content
            row[6].toString(), row[7].toString(), row[8].toString(), row[9].toString(), // A-D
            row[10].toString(), // studentAnswer
            row[11].toString()  // correctAnswer
        ));
    }

    return new ArrayList<>(studentMap.values());
}


}
