package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BangDiemId implements Serializable {

    @Column(name = "MASV", length = 8)
    private String maSV;

    @Column(name = "MAMH", length = 5)
    private String maMH;

    @Column(name = "LAN")
    private Short lan;

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        BangDiemId that = (BangDiemId) o;

        if (!maSV.equals(that.maSV))
            return false;
        if (!maMH.equals(that.maMH))
            return false;
        return lan.equals(that.lan);
    }

    @Override
    public int hashCode() {
        int result = maSV.hashCode();
        result = 31 * result + maMH.hashCode();
        result = 31 * result + lan.hashCode();
        return result;
    }
}