package com.example.demo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.GradeRecordDTO;
import com.example.demo.repository.BangDiemRepository;

@Service
public class BangDiemService {
@Autowired
    private BangDiemRepository bangDiemRepository;

   
    public List<GradeRecordDTO> getGradeRecords(String classId, String subjectId, int examAttempt) {
        List<Object[]> results = bangDiemRepository.findGradeRecords(classId, subjectId, examAttempt);
        return results.stream().map(obj -> new GradeRecordDTO(
                (String) obj[0],   // studentId
                (String) obj[1],   // lastName
                (String) obj[2],   // firstName
                ((Number) obj[3]).doubleValue(), // score
                (String) obj[4]    // gradeLetter
        )).collect(Collectors.toList());
    }
}
