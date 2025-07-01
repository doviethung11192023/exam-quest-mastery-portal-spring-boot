package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "Bode")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bode {

    @EmbeddedId
    private BodeId id;

    @MapsId("maMH")
    @ManyToOne
    @JoinColumn(name = "MAMH")
    private Monhoc monhoc;

    @Column(name = "TRINHDO", length = 1)
    private String trinhDo;

    @Column(name = "NOIDUNG", length = 200)
    private String noiDung;

    @Column(name = "A", length = 50)
    private String a;

    @Column(name = "B", length = 50)
    private String b;

    @Column(name = "C", length = 50)
    private String c;

    @Column(name = "D", length = 50)
    private String d;

    @Column(name = "DAP_AN", length = 1)
    private String dapAn;

    @ManyToOne
    @JoinColumn(name = "MAGV")
    private Giaovien giaovien;

    @OneToMany(mappedBy = "bode")
    private List<ChiTietBaiThi> danhSachChiTietBaiThi;

    public void setMaMH(String maMH) {
        if (this.id == null) {
            this.id = new BodeId(maMH);
        }
        this.id.setMaMH(maMH);
    }
    @PrePersist
    @PreUpdate
    public void validateData() {
        // Validate NOIDUNG
        if (dapAn != null && !"A".equals(dapAn) && !"B".equals(dapAn)
                && !"C".equals(dapAn) && !"D".equals(dapAn)) {
            throw new IllegalArgumentException("DAP_AN must be A, B, C or D");
        }
        //validate TRINHDO
        if (trinhDo != null && !"A".equals(trinhDo) && !"B".equals(trinhDo)
                && !"C".equals(trinhDo)) {
            throw new IllegalArgumentException("TRINHDO must be A, B or C");
        }
    }

   
}