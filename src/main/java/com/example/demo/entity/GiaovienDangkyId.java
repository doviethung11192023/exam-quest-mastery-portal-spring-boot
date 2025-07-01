package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GiaovienDangkyId implements java.io.Serializable {

    @Column(name = "MALOP", length = 8)
    private String maLop;

    @Column(name = "MAMH", length = 5)
    private String maMH;

    @Column(name = "LAN")
    private Short lan;
}
