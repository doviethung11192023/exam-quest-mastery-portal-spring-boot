package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SinhvienDTO {
    private String maSV;
    private String ho;
    private String ten;
    private String hoTen;
    private LocalDate ngaySinh;
    private String diaChi;
    private String maLop;
    private String tenLop;
}