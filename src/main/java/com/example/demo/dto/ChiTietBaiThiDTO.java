package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietBaiThiDTO {
    private Integer cauHoi;
    private String noiDung;
    private String a;
    private String b;
    private String c;
    private String d;
    private String traLoi;
    private String dapAn;
    private Boolean isCorrect;

    // Additional fields for display purposes
    private String cauHoiFormatted; // Example: "Câu 1: ..."
    private String trinhDo; // A, B, or C
}