package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BodeDTO {
    // Thông tin câu hỏi
    private String maMH; // Mã môn học
    private String tenMH; // Tên môn học (bổ sung)
    private Integer cauHoi; // Số thứ tự câu hỏi
    private String trinhDo; // Trình độ (A, B, C)
    private String noiDung; // Nội dung câu hỏi

    // Các câu trả lời
    private String a; // Đáp án A
    private String b; // Đáp án B
    private String c; // Đáp án C
    private String d; // Đáp án D
    private String dapAn; // Đáp án đúng (A, B, C, D)

    // Thông tin giáo viên
    private String magv; // Mã giáo viên tạo câu hỏi
    private String tenGiangVien; // Tên giảng viên tạo câu hỏi (bổ sung)
}