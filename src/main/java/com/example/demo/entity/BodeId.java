package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Class đại diện cho khóa chính tổng hợp của entity Bode
 * Bao gồm mã môn học (MAMH) và số thứ tự câu hỏi (CAUHOI)
 */
@Embeddable
//@Data
@NoArgsConstructor
//@AllArgsConstructor
public class BodeId implements Serializable {

    @Column(name = "MAMH", length = 5)
    private String maMH;

    @Column(name = "CAUHOI", insertable = false, updatable = false)
    private Integer cauHoi;
    
    public BodeId(String maMH) {
        this.maMH = maMH;
    }
    public BodeId(String maMH, Integer cauHoi) {
        this.maMH = maMH;
        this.cauHoi = cauHoi;
    }   
    // Getters và setters
    public String getMaMH() {
        return maMH;
    }

    public void setMaMH(String maMH) {
        this.maMH = maMH;
    }

    public Integer getCauHoi() {
        return cauHoi;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        BodeId bodeId = (BodeId) o;

        if (!maMH.equals(bodeId.maMH))
            return false;
        return cauHoi.equals(bodeId.cauHoi);
    }

    @Override
    public int hashCode() {
        int result = maMH.hashCode();
        result = 31 * result + cauHoi.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "BodeId{" +
                "maMH='" + maMH + '\'' +
                ", cauHoi=" + cauHoi +
                '}';
    }
}