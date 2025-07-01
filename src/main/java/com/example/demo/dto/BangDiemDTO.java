package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BangDiemDTO {
    private String maSV;
    private String hoTenSV;
    private String maMH;
    private String tenMH;
    private Short lan;
    private LocalDate ngayThi;
    private Float diem;
    private List<ChiTietBaiThiDTO> chiTietBaiThi;
}