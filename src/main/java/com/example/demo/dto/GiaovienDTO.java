package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GiaovienDTO {
    private String magv;
    private String ho;
    private String ten;
    private String hoTen;
    private String soDienThoai;
    private String diaChi;
    private Boolean trangThai;
    private Boolean hasAccount;
}