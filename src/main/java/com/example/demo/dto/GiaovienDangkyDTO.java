package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GiaovienDangkyDTO {
    private String maLop;
    private String tenLop;
    private String maMH;
    private String tenMH;
    private Short lan;
    private String magv;
    private String tenGiangVien;
    private String trinhDo;
    private LocalDateTime ngayThi;
    private Short soCauThi;
    private Short thoiGian;
}