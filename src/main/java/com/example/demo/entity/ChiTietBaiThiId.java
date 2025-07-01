package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietBaiThiId implements Serializable {

    @Column(name = "MASV")
    private String maSV;

    @Column(name = "MAMH")
    private String maMH;

    @Column(name = "LAN")
    private Integer lan;

    @Column(name = "CAUHOI")
    private Integer cauHoi;
}